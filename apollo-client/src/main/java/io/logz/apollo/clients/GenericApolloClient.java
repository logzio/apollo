package io.logz.apollo.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.exceptions.*;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.RestResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

class GenericApolloClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Logger logger = LoggerFactory.getLogger(GenericApolloClient.class);

    private final OkHttpClient client;
    private final String userName;
    private final String plainPassword;
    private final String hostname;
    private final int port;
    private final String protocol;
    private final Optional<String> prefix;
    private final ObjectMapper mapper;
    private String token;

    private enum HTTP_METHOD {
        GET,
        POST,
        PUT,
        DELETE
    }

    GenericApolloClient(String userName, String plainPassword, String protocol, String hostname, int port, Optional<String> prefix) {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        this.userName = userName;
        this.plainPassword = plainPassword;
        this.hostname = hostname;
        this.port = port;
        this.protocol = protocol;
        this.prefix = prefix;
        this.mapper = new ObjectMapper();
    }

    void login() throws ApolloCouldNotLoginException {

        try {
            RestResponse response = post("/_login", generateLoginJson());

            if (!isLoginSuccessful(response)) {
                throw new ApolloCouldNotLoginException("Could not login user " + userName + " with password " + plainPassword);
            }

            token = getTokenFromResponse(response);

        } catch (IOException | ApolloClientException e) {
            throw new ApolloCouldNotLoginException("Could not login user due to unexpected error!", e);
        }
    }

    RestResponse get(String url) throws IOException {
        Request request = new Request.Builder().url(getFullUrlWithToken(url)).build();
        Response response = client.newCall(request).execute();
        return new RestResponse(response.code(), response.body().string());
    }

    RestResponse delete(String url) throws IOException {
        Request request = new Request.Builder().url(getFullUrlWithToken(url)).delete().build();
        Response response = client.newCall(request).execute();
        return new RestResponse(response.code(), response.body().string());
    }

    RestResponse post(String url, String json) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(getFullUrlWithToken(url)).post(requestBody).build();
        Response response = client.newCall(request).execute();
        return new RestResponse(response.code(), response.body().string());
    }

    RestResponse put(String url, String json) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(getFullUrlWithToken(url)).put(requestBody).build();
        Response response = client.newCall(request).execute();
        return new RestResponse(response.code(), response.body().string());
    }

    String getUserName() {
        return userName;
    }

    <T> T getResult(String url, TypeReference<T> responseType) throws ApolloClientException {
        return runAndGetResult(url, Optional.empty(), responseType, HTTP_METHOD.GET);
    }

    <T> T postAndGetResult(String url, String body, TypeReference<T> responseType) throws ApolloClientException {
        return runAndGetResult(url, Optional.of(body), responseType, HTTP_METHOD.POST);
    }

    <T> T putAndGetResult(String url, String body, TypeReference<T> responseType) throws ApolloClientException {
        return runAndGetResult(url, Optional.of(body), responseType, HTTP_METHOD.PUT);
    }

    <T> T deleteAndGetResult(String url, String body, TypeReference<T> responseType) throws ApolloClientException {
        return runAndGetResult(url, Optional.of(body), responseType, HTTP_METHOD.DELETE);
    }

    private <T> T runAndGetResult(String url, Optional<String> body, TypeReference<T> responseType, HTTP_METHOD httpMethod) throws ApolloClientException {
        try {
            RestResponse restResponse;

            if (body.isPresent()) {
                switch (httpMethod) {
                    case POST:
                        restResponse = post(url, body.get());
                        break;
                    case PUT:
                        restResponse = put(url, body.get());
                        break;
                    default:
                        throw new ApolloClientException("Unsupported method!");

                }

                switch (restResponse.getCode()) {
                    case 200:
                        if (httpMethod.equals(HTTP_METHOD.PUT))
                            break;
                        else
                            throw new ApolloClientException("Got http 200 on POST, something did not work");
                    case 201:
                        break;
                    case 401:
                        throw new ApolloNotAuthenticatedException();
                    case 403:
                        throw new ApolloNotAuthorizedException();
                    case 406:
                        throw new ApolloBlockedException();
                    default:
                        throw new ApolloClientException("Got HTTP return code " + restResponse.getCode() + " with text: " + restResponse.getBody());
                }

            } else {
                switch (httpMethod) {
                    case GET:
                        restResponse = get(url);
                        break;
                    case DELETE:
                        restResponse = delete(url);
                        break;
                    default:
                        throw new ApolloClientException("Unsupported method!");
                }

                switch (restResponse.getCode()) {
                    case 200:
                        break;
                    case 401:
                        throw new ApolloNotAuthenticatedException();
                    case 403:
                        throw new ApolloNotAuthorizedException();
                    case 406:
                        throw new ApolloBlockedException();
                    default:
                        throw new ApolloClientException("Got HTTP return code " + restResponse.getCode() + " with text: " + restResponse.getBody());
                }
            }
            return mapper.readValue(restResponse.getBody(), responseType);

        } catch (IOException e) {
            throw new ApolloClientException("Could not reach Apollo API!", e);
        }
    }

    private String getFullUrlWithToken(String url) {
        String urlPrefix = prefix.map(s -> "/" + s).orElse("");
        String tokenPrefix = url.contains("?") ? "&" : "?";
        String tokenPostfix = StringUtils.isNotBlank(token) ? "_token=" + token : "";
        return protocol + "://" + hostname + ":" + port + urlPrefix + url + tokenPrefix + tokenPostfix;
    }

    private String generateLoginJson() {
        return Common.generateJson("username", userName, "password", plainPassword);
    }

    private String getTokenFromResponse(RestResponse restResponse) throws ApolloClientException {
        try {
            return mapper.readTree(restResponse.getBody()).get("token").asText();
        } catch (Exception e) {
            throw new ApolloClientException("Could not get token from response!", e);
        }
    }

    private boolean isLoginSuccessful(RestResponse restResponse) throws ApolloClientException {
        try {
            return mapper.readTree(restResponse.getBody()).get("success").asBoolean();
        } catch (Exception e) {
            throw new ApolloClientException("Could not determine if login succeeded", e);
        }
    }
}
