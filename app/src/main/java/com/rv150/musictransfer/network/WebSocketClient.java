package com.rv150.musictransfer.network;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.rv150.musictransfer.utils.UiThread;

import java.io.IOException;

import static com.rv150.musictransfer.network.Message.SENDING_FINISHED;


/**
 * Created by ivan on 10.05.17.
 */

public class WebSocketClient extends WebSocketAdapter {

    private WebSocket webSocket;
    private static final String SERVER_URL = "ws://192.168.1.36:8088/v1/ws";
    private final Gson gson = new Gson();
    private int id = -1;

    private WebSocketClient(Context context) {
        try {
            this.context = context;
            webSocket = new WebSocketFactory()
                    .createSocket(SERVER_URL, 5000)
                    .addListener(this)
                    .connect();
        }
        catch (IOException | WebSocketException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private Context context;
    private static WebSocketClient instance;

    public static synchronized WebSocketClient getInstance(Context context) {
        if (instance == null) {
            instance = new WebSocketClient(context);
        }
        return instance;
    }

    int totalBytes = 0;

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        Log.d(TAG, "Binary message");
        totalBytes += binary.length;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        Message message = gson.fromJson(text, Message.class);
        switch (message.getType()) {
            case Message.INITIALIZE_USER:
                this.id = Integer.valueOf(message.getData());
                Log.d(TAG, "Getting ID = " + this.id);
                UiThread.run(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Your ID is " + id, Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case Message.SENDING_FINISHED:
                Log.d(TAG, "FINISHED! Total bytes = " + totalBytes);
                UiThread.run(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Downloading has finished, total bytes = " + totalBytes, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                throw new UnsupportedOperationException("No handlers?");
        }
    }





    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void sendFinishSignal() {
        try {
            Message message = new Message(SENDING_FINISHED, "ok");
            String json = new Gson().toJson(message);
            webSocket.sendText(json);
            Log.d(TAG, "Sending finish signal!");
        }
        catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private static final String TAG = WebSocketClient.class.getSimpleName();
}
