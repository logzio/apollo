package io.logz.apollo.clients;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocketListener;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class ApolloWebSocketClient {

    private final URI uri;
    private final WebSocketListener webSocketListener;
    private final OkHttpClient client;
    private final CookieManager cookieManager;

    public ApolloWebSocketClient(String protocol, String hostname, int port, WebSocketListener webSocketListener) throws URISyntaxException {

        uri = new URI(protocol + "://" + hostname + ":" + port);
        this.webSocketListener = webSocketListener;
        cookieManager =  new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
    }

    public void setToken(String token) {
        cookieManager.getCookieStore().add(uri, new HttpCookie("_token", token));
    }

    public void start() {
        Request request = new Request.Builder()
                .url(uri.toString())
                .build();

        client.newWebSocket(request, webSocketListener);
    }

    public void shutdown() {
        client.dispatcher().executorService().shutdown();
    }


}