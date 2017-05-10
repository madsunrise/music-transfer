package com.rv150.musictransfer.network;


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by ivan on 10.05.17.
 */

public class WebSocketClient extends WebSocketListener {

    private WebSocket webSocket;

    private WebSocketClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url("ws://192.168.1.46:8088/v1/ws")
                .build();
        webSocket = client.newWebSocket(request, this);



        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        //client.dispatcher().executorService().shutdown();
    }

    private static final WebSocketClient instance = new WebSocketClient();

    public static WebSocketClient getInstance() {
        return instance;
    }

    public void sendMessage(String payload) {
        webSocket.send(payload);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send("Hello...");
        webSocket.send("...World!");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("MESSAGE string: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println("MESSAGE bytes: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
        System.out.println("CLOSE: " + code + " " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }
}
