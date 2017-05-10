package com.rv150.musictransfer.network;


import android.util.Log;

import com.google.gson.Gson;

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
    private static final String SERVER_URL = "ws://192.168.1.46:8088/v1/ws";
    private final Gson gson = new Gson();
    private int id = -1;

    private WebSocketClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();
        webSocket = client.newWebSocket(request, this);
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

    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Message message = gson.fromJson(text, Message.class);
        switch (message.getType()) {
            case Message.INITIALIZE_USER:
                this.id = Integer.valueOf(message.getData());
                break;
            default:
                throw new UnsupportedOperationException("No handlers?");
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println("MESSAGE bytes: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {

    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }

    private static final String TAG = WebSocketClient.class.getSimpleName();
}
