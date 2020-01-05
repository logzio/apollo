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

    private OkHttpClient client;

    public ApolloWebSocketClient(String token, String protocol, String hostname, int port, WebSocketListener webSocketListener) throws URISyntaxException {

        String uri = protocol + "://" + hostname + ":" + port;

        CookieManager cookieManager =  new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        cookieManager.getCookieStore().add(new URI(uri), new HttpCookie("_token", token));

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();

        Request request = new Request.Builder()
                .url(uri)
                .build();

        client.newWebSocket(request, webSocketListener);
    }

    public void shutdown() {
        client.dispatcher().executorService().shutdown();
    }


}