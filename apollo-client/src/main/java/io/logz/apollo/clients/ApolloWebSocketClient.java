package io.logz.apollo.clients;

import com.sun.istack.internal.Nullable;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ApolloWebSocketClient extends WebSocketListener {

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
//                .url("ws://echo.websocket.org")
                .build();

        client.newWebSocket(request, webSocketListener);
    }

    public void shutdown() {
        client.dispatcher().executorService().shutdown();
    }


//        private AtomicInteger openSessionCounter = new AtomicInteger(0);
//        private AtomicInteger failedSessionCounter = new AtomicInteger(0);
//
//    @Override
//    public void onOpen(WebSocket webSocket, Response response) {
//            openSessionCounter.incrementAndGet();
//        }
//
//    @Override public void onMessage(WebSocket webSocket, ByteString bytes) {
//        openSessionCounter.incrementAndGet();
//    }
//
//
//    @Override
//    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
//            failedSessionCounter.incrementAndGet();
//        }
//
//    @Override
//    public void onClosing(WebSocket webSocket, int code, String reason) {
//        failedSessionCounter.incrementAndGet();
//    }
//
//    @Override
//    public void onClosed(WebSocket webSocket, int code, String reason) {
//        failedSessionCounter.incrementAndGet();
//    }
//
//        public AtomicInteger getOpenSessionCounter() {
//            return openSessionCounter;
//        }
//
//        public AtomicInteger getFailedSessionCounter() {
//            return failedSessionCounter;
//        }

}