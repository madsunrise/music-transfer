package com.rv150.musictransfer.network;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.rv150.musictransfer.utils.UiThread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.rv150.musictransfer.network.Message.ANSWER_ON_REQUEST;
import static com.rv150.musictransfer.network.Message.INITIALIZE_USER;
import static com.rv150.musictransfer.network.Message.REQUEST_SEND;
import static com.rv150.musictransfer.network.Message.SENDING_FINISHED;
import static com.rv150.musictransfer.utils.Config.BUFFER_SIZE;
import static com.rv150.musictransfer.utils.Config.WEBSOCKET_URL;

/**
 * Created by ivan on 27.05.17.
 */

public class WebSocketReceiveClient extends WebSocketAdapter {

    private WebSocket webSocket;

    private final Gson gson = new Gson();

    public static final int FILE_CREATION_ERROR = 0;

    private WebSocketReceiveClient() {

    }

    private static WebSocketReceiveClient instance = new WebSocketReceiveClient();

    public static WebSocketReceiveClient getInstance() throws IOException {
        return instance;
    }

    private BufferedOutputStream outputStream = null;
    private String currentFileName = null;
    private long currentFileSize = 0;


    public interface CommonCallback {
        void onConnected();
        void onDisconnected(boolean byServer);
        void onError(int errorCode);
    }

    public interface PrepareCallback extends CommonCallback {
        void onIdRegistered(long id);
        void onFileSendingRequest(String fileName);
    }

    public interface ReceiverCallback extends CommonCallback {
        void onProgressChanged(int percentage);
        void onFileReceivingFinished();
    }


    private PrepareCallback prepareCallback;
    private ReceiverCallback receiverCallback;

    private int iteration = 1;


    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        if (currentFileName == null) {
            Log.e(TAG, "Getting binary data without filename registration!");
            return;
        }
        if (outputStream == null) {
            File dir = Environment.getExternalStorageDirectory();
            File file = new File(dir, currentFileName);
            if (!file.createNewFile()) {
                Log.e(TAG, "Failed to create new file! Permissions?");
                receiverCallback.onError(FILE_CREATION_ERROR);
                return;
            }
            OutputStream os = new FileOutputStream(file);
            outputStream = new BufferedOutputStream(os);
            Log.d(TAG, "Output stream was initialized");
        }
        outputStream.write(binary);
        final int percentage = (int)((double)(iteration * BUFFER_SIZE * 100) / currentFileSize);
        Log.d(TAG, "Receiving file: " + percentage + "%");
        UiThread.run(() -> {
            if (receiverCallback != null) {
                receiverCallback.onProgressChanged(percentage);
            }
        });
        iteration++;
    }


    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        Message message = gson.fromJson(text, Message.class);
        switch (message.getType()) {
            case INITIALIZE_USER:
                long id = Long.valueOf(message.getData());
                Log.d(TAG, "Getting ID = " + id);
                UiThread.run(() -> {
                    if (prepareCallback != null) {
                        prepareCallback.onIdRegistered(id);
                    }
                });
                break;


            case SENDING_FINISHED:
                Log.d(TAG, "FINISHED Receiving file!");
                UiThread.run(() -> {
                    if (receiverCallback != null) {
                        receiverCallback.onFileReceivingFinished();
                    }
                });
                outputStream.flush();
                outputStream.close();
                break;

            case REQUEST_SEND:
                SendRequest request = gson.fromJson(message.getData(), SendRequest.class);
                currentFileName = request.getFileName();
                currentFileSize = request.getFileSize();
                Log.d(TAG, "Getted request on sending " + currentFileName + " (" + currentFileSize + " bytes)");
                UiThread.run(() -> {
                    if (prepareCallback != null) {
                        prepareCallback.onFileSendingRequest(currentFileName);
                    }
                });
                break;

            default:
                break;
        }
    }




    public void sendAnswerOnRequest(boolean answer) {
        Log.d(TAG, "Sending answer in request: " + String.valueOf(answer));
        Message message = new Message(ANSWER_ON_REQUEST, String.valueOf(answer));
        webSocket.sendText(gson.toJson(message));
    }


    public void connect() throws WebSocketException, IOException {
        webSocket = new WebSocketFactory()
                .createSocket(WEBSOCKET_URL, 5000)
                .addListener(this);
        webSocket.connect();
        Log.d(TAG, "Connected!");
    }

    public void disconnect() {
        webSocket.disconnect();
        Log.d(TAG, "Disconnected!");
    }

    public boolean isConnected() {
        return webSocket.isOpen();
    }




    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        UiThread.run(() -> {
            if (prepareCallback != null) {
                prepareCallback.onConnected();
            }
            if (receiverCallback != null) {
                receiverCallback.onConnected();
            }
        });
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        Log.d(TAG, "Disconnected! By server - " + closedByServer);
        UiThread.run(() -> {
            if (prepareCallback != null) {
                prepareCallback.onDisconnected(closedByServer);
            }
            if (receiverCallback != null) {
                receiverCallback.onDisconnected(closedByServer);
            }
        });
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        Log.d(TAG, "Connection ERROR!!! Reason: " + exception.getMessage());
    }


    // Привязан должен быть только один коллбек в один момент времени
    public void setCallback(CommonCallback callback) {
        if (callback instanceof PrepareCallback) {
            prepareCallback = (PrepareCallback) callback;
            receiverCallback = null;
        }
        else {
            receiverCallback = (ReceiverCallback) callback;
            prepareCallback = null;
        }
    }

    private static final String TAG = WebSocketReceiveClient.class.getSimpleName();
}

