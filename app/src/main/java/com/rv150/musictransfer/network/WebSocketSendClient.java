package com.rv150.musictransfer.network;


import android.util.Log;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.rv150.musictransfer.model.Song;
import com.rv150.musictransfer.utils.UiThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.rv150.musictransfer.network.Message.ALLOW_TRANSFERRING;
import static com.rv150.musictransfer.network.Message.ERROR;
import static com.rv150.musictransfer.network.Message.RECEIVER_FOUND;
import static com.rv150.musictransfer.network.Message.RECEIVER_ID;
import static com.rv150.musictransfer.network.Message.RECEIVER_NOT_FOUND;
import static com.rv150.musictransfer.network.Message.SENDING_FINISHED;
import static com.rv150.musictransfer.utils.Config.WEBSOCKET_URL;


/**
 * Created by ivan on 10.05.17.
 */

public class WebSocketSendClient extends WebSocketAdapter {


    private WebSocket webSocket;

    private final Gson gson = new Gson();
    private Song songForTransfer;

    private WebSocketSendClient() {

    }

    private static WebSocketSendClient instance = new WebSocketSendClient();

    public static WebSocketSendClient getInstance() throws IOException {
        return instance;
    }


    public interface CommonCallback {
        void onConnected();
        void onDisconnected(boolean byServer);
        void onError(int errorCode);
    }

    public interface PrepareCallback extends CommonCallback {
        void onReceiverFound(boolean found);
    }

    public interface SenderCallback extends CommonCallback {
        void onSendingStarted();
        void onProgressChanged(int progress);
        void onSendingFinished();
    }

    private PrepareCallback prepareCallback;
    private SenderCallback senderCallback;


    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        Message message = gson.fromJson(text, Message.class);
        switch (message.getType()) {

            case RECEIVER_FOUND: {
                Log.d(TAG, "Receiver was found, waiting for confirmation...");
                UiThread.run(() -> {
                    if (prepareCallback != null) {
                        prepareCallback.onReceiverFound(true);
                    }
                });
                break;
            }


            case ALLOW_TRANSFERRING: {
                // TODO May be some sleep for 100-200 ms?
                Log.d(TAG, "Transfer has been approved, start sending!");
                UiThread.run(() -> {
                    if (senderCallback != null) {
                        senderCallback.onSendingStarted();
                    }
                });
                sendFile();
                break;
            }

            case ERROR: {
                Log.d(TAG, "Error! " + message.getData());
                switch (message.getData()) {
                    case RECEIVER_NOT_FOUND: {
                        UiThread.run(() -> {
                            if (prepareCallback != null) {
                                prepareCallback.onReceiverFound(false);
                            }
                        });
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            default:
                break;
        }
    }




    public void registerSongForTransferring(Song song, String receiverId) {
        Log.d(TAG, "Registered song " + song.getTitle() + " for transferring to " + receiverId);
        SendRequest request = new SendRequest(receiverId, song.getTitle());
        Message message = new Message(RECEIVER_ID, gson.toJson(request));
        webSocket.sendText(gson.toJson(message));
        this.songForTransfer = song;
    }




    private void sendFile() {
        if (songForTransfer == null) {
            Log.e(TAG, "Error sending file - songForTransfer is null!");
            return;
        }
        int BUFFER_SIZE = 16 * 1024;
        File file = new File(songForTransfer.getPath());
        try {
            InputStream is = new FileInputStream(file);
            byte[] chunk = new byte[BUFFER_SIZE];

            long totalSize = file.length();
            int chunkLen, i = 1;
            while ((chunkLen = is.read(chunk, 0, BUFFER_SIZE)) != -1) {
                byte[] sended = Arrays.copyOfRange(chunk, 0, chunkLen);
                webSocket.sendBinary(sended);
                int percentage = (int)((double)(i * BUFFER_SIZE * 100) / totalSize);
                UiThread.run(() -> {
                    if (senderCallback != null) {
                        senderCallback.onProgressChanged(percentage);
                    }
                });
                Log.d(TAG, "Sended " + percentage + "%");
                i++;
            }
            sendFinishSignal();
            UiThread.run(() -> {
                if (senderCallback != null) {
                    senderCallback.onSendingFinished();
                }
            });
            Log.d(TAG, "Transferring has been finished");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
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
        Log.d(TAG, "Disconnect method called!");
    }

    public boolean isConnected() {
        return webSocket != null && webSocket.isOpen();
    }



    private void sendFinishSignal() {
        try {
            Message message = new Message(SENDING_FINISHED, "ok");
            String json = gson.toJson(message);
            webSocket.sendText(json);
            Log.d(TAG, "Sending finish signal!");
        }
        catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }


    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        UiThread.run(() -> {
            if (senderCallback != null) {
                senderCallback.onConnected();
            }
        });
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        Log.d(TAG, "Disconnected! By server - " + closedByServer);
        UiThread.run(() -> {
            if (senderCallback != null) {
                senderCallback.onDisconnected(closedByServer);
            }
        });
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        Log.d(TAG, "Connection ERROR!!! Reason: " + exception.getMessage());
    }

    public void setCallback(CommonCallback callback) {
        if (callback instanceof PrepareCallback) {
            prepareCallback = (PrepareCallback) callback;
            senderCallback = null;
        }
        else {
            prepareCallback = null;
            senderCallback = (SenderCallback) callback;
        }
    }

    private static final String TAG = WebSocketSendClient.class.getSimpleName();
}
