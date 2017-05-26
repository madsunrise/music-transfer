package com.rv150.musictransfer.network;


import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.rv150.musictransfer.utils.UiThread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static com.rv150.musictransfer.network.Message.ALLOW_TRANSFERRING;
import static com.rv150.musictransfer.network.Message.ANSWER_ON_REQUEST;
import static com.rv150.musictransfer.network.Message.ERROR;
import static com.rv150.musictransfer.network.Message.INITIALIZE_USER;
import static com.rv150.musictransfer.network.Message.RECEIVER_NOT_FOUND;
import static com.rv150.musictransfer.network.Message.REQUEST_SEND;
import static com.rv150.musictransfer.network.Message.SENDING_FINISHED;


/**
 * Created by ivan on 10.05.17.
 */

public class WebSocketClient extends WebSocketAdapter {

    private WebSocket webSocket;
    private static final String SERVER_URL = "ws://212.109.192.197:8088/v1/ws";
    private final Gson gson = new Gson();

    private Callback callback;

    public static final int CONNECTION_ERROR = 0;

    private WebSocketClient() throws IOException {
        webSocket = new WebSocketFactory()
                .createSocket(SERVER_URL, 5000)
                .addListener(this);
    }

    private static WebSocketClient instance;

    public static synchronized WebSocketClient getInstance() throws IOException {
        if (instance == null) {
            instance = new WebSocketClient();
        }
        return instance;
    }

    private BufferedOutputStream outputStream = null;
    private String currentFileName = null;

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
                //UiThread.run(() -> Toast.makeText(activity, R.string.internal_error, Toast.LENGTH_SHORT).show());
                return;
            }
            OutputStream os = new FileOutputStream(file);
            outputStream = new BufferedOutputStream(os);
            Log.d(TAG, "Output stream was initialized");
        }
        outputStream.write(binary);
    }


    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        Message message = gson.fromJson(text, Message.class);
        switch (message.getType()) {
            case INITIALIZE_USER:
                long id = Long.valueOf(message.getData());
                Log.d(TAG, "Getting ID = " + id);
                UiThread.run(() -> {
                    if (callback != null) {
                        callback.onIdRegistered(id);
                    }
                });
                break;


            case SENDING_FINISHED:
                Log.d(TAG, "FINISHED Receiving file!");
//                UiThread.run(() ->
//                        Toast.makeText(activity, R.string.downloading_has_finished, Toast.LENGTH_SHORT).show()
//                );
                outputStream.flush();
                outputStream.close();
                break;

            case REQUEST_SEND:
                currentFileName = message.getData();
                Log.d(TAG, "Getted request on sending " + currentFileName);
//                UiThread.run(() ->
//                        new AlertDialog.Builder(activity)
//                                .setMessage(String.format(activity.getString(R.string.do_you_want_to_accept_new_file), currentFileName))
//                                .setPositiveButton(R.string.yes, (d, w) -> sendAnswerOnRequest(true))
//                                .setNegativeButton(R.string.no, (d, w) -> sendAnswerOnRequest(false))
//                                .show()
//                );

                break;


            case ERROR:
                Log.d(TAG, "Error! " + message.getData());
                switch (message.getData()) {
                    case RECEIVER_NOT_FOUND: {
//                        UiThread.run(() ->
//                                Toast.makeText(activity, R.string.receiver_with_this_id_not_found, Toast.LENGTH_SHORT).show()
//                        );
                        break;
                    }
                }
                break;

            case ALLOW_TRANSFERRING:
                Log.d(TAG, "Transfer has been approved, sending!");
//                UiThread.run(() ->
//                        Toast.makeText(activity, R.string.start_sending, Toast.LENGTH_SHORT).show()
//                );
                sendFile();
                break;

            default:
                throw new UnsupportedOperationException("No handlers?");
        }
    }


    public interface Callback {
        void onIdRegistered(long id);
        void onSendRequest(String fileName);
        void onFileReceived();
        void onError(int errorCode);
    }


    private String path;
    public void registerFileToSend(String path) {
        this.path = path;
    }

    private void sendFile() {
        if (path == null) {
            Log.e(TAG, "Error sending file - path is null!");
        }
        int BUFFER_SIZE = 16 * 1024;
        File file = new File(path);
        try {

            InputStream is = new FileInputStream(file);
            byte[] chunk = new byte[BUFFER_SIZE];

            int chunkLen;
            while ((chunkLen = is.read(chunk, 0, BUFFER_SIZE)) != -1) {
                byte[] sended = Arrays.copyOfRange(chunk, 0, chunkLen);
                webSocket.sendBinary(sended);
            }

            sendFinishSignal();
            Log.d(TAG, "Transferring has been finished");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void sendAnswerOnRequest(boolean answer) {
        Log.d(TAG, "Sending answer in request: " + String.valueOf(answer));
        Message message = new Message(ANSWER_ON_REQUEST, String.valueOf(answer));
        webSocket.sendText(new Gson().toJson(message));
    }


    public void connect() throws WebSocketException {
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

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void sendFinishSignal() {
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


    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        UiThread.run(() -> {
                if (callback != null) {
                    callback.onError(CONNECTION_ERROR);
                }
            }
        );
    }

    private static final String TAG = WebSocketClient.class.getSimpleName();
}
