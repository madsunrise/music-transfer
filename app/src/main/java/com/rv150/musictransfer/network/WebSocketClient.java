package com.rv150.musictransfer.network;


import android.util.Log;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



import okio.ByteString;



/**
 * Created by ivan on 10.05.17.
 */

public class WebSocketClient extends WebSocketAdapter {

    private WebSocket webSocket;
    private static final String SERVER_URL = "ws://192.168.1.50:8088/v1/binary";
    private final Gson gson = new Gson();
    private int id = -1;

    private WebSocketClient() {
//        OkHttpClient client = new OkHttpClient.Builder()
//                .build();
//
//        Request request = new Request.Builder()
//                .url(SERVER_URL)
//                .build();
//        webSocket = client.newWebSocket(request, this);
        try {
            webSocket = new WebSocketFactory().createSocket(SERVER_URL, 5000).addListener(this).connect();
        }
        catch (IOException | WebSocketException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private static final WebSocketClient instance = new WebSocketClient();

    public static WebSocketClient getInstance() {
        return instance;
    }


//    @Override
//    public void onOpen(okhttp3.WebSocket webSocket, Response response) {
//        Log.d(TAG, "OnOpen!");
//    }
//
//    @Override
//    public void onMessage(okhttp3.WebSocket webSocket, String text) {
//        Log.d(TAG, text);
//    }
//
//    @Override
//    public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
//        Log.d(TAG, "Binary msg! Length " + bytes.size());
//    }
//
//    @Override
//    public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
//        Log.d(TAG, "OnClosing!");
//    }
//
//    @Override
//    public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
//        Log.d(TAG, "OnClosed!");
//    }
//
//    @Override
//    public void onFailure(okhttp3.WebSocket webSocket, Throwable t, Response response) {
//        super.onFailure(webSocket, t, response);
//    }

    public void onsTextMessage(WebSocket websocket, String text) throws Exception {
        Message message = gson.fromJson(text, Message.class);
        switch (message.getType()) {
            case Message.INITIALIZE_USER:
                this.id = Integer.valueOf(message.getData());
                break;
            default:
                throw new UnsupportedOperationException("No handlers?");
        }
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    private static final String TAG = WebSocketClient.class.getSimpleName();
}
