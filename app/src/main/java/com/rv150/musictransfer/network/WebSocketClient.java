package com.rv150.musictransfer.network;


import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.rv150.musictransfer.R;
import com.rv150.musictransfer.utils.UiThread;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

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
    private int id = -1;

    private static final int RC_DIRECTORY_PICKER_FILE = 0;

    private WebSocketClient(Context activity) {
        try {
            this.activity = activity;
            webSocket = new WebSocketFactory()
                    .createSocket(SERVER_URL, 5000)
                    .addListener(this)
                    .connect();
        }
        catch (IOException | WebSocketException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private Context activity;
    private static WebSocketClient instance;

    public static synchronized WebSocketClient getInstance(Context activity) {
        if (instance == null) {
            instance = new WebSocketClient(activity);
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
                UiThread.run(() -> Toast.makeText(activity, R.string.internal_error, Toast.LENGTH_SHORT).show());
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
                this.id = Integer.valueOf(message.getData());
                Log.d(TAG, "Getting ID = " + this.id);
                UiThread.run(() ->
                        Toast.makeText(activity, "Your ID is " + id, Toast.LENGTH_LONG).show()
                );
                break;


            case SENDING_FINISHED:
                Log.d(TAG, "FINISHED Receiving file!");
                UiThread.run(() ->
                        Toast.makeText(activity, R.string.downloading_has_finished, Toast.LENGTH_SHORT).show()
                );
                outputStream.flush();
                outputStream.close();
                break;

            case REQUEST_SEND:
                currentFileName = message.getData();
                Log.d(TAG, "Getted request on sending " + currentFileName);
                UiThread.run(() ->
                        new AlertDialog.Builder(activity)
                                .setMessage(String.format(activity.getString(R.string.do_you_want_to_accept_new_file), currentFileName))
                                .setPositiveButton(R.string.yes, (d, w) -> sendAnswerOnRequest(true))
                                .setNegativeButton(R.string.no, (d, w) -> sendAnswerOnRequest(false))
                                .show()
                );

                break;


            case ERROR:
                Log.d(TAG, "Error! " + message.getData());
                switch (message.getData()) {
                    case RECEIVER_NOT_FOUND: {
                        Toast.makeText(activity, R.string.receiver_with_this_id_not_found, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                break;

            case ALLOW_TRANSFERRING:
                Log.d(TAG, "Transfer has been approved, sending!");
                sendFile();
                break;

            default:
                throw new UnsupportedOperationException("No handlers?");
        }
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



    private void showFilePicker() {
        final Intent chooserIntent = new Intent(activity, DirectoryChooserActivity.class);
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("New folder")
                .allowReadOnlyDirectory(true)
                .allowNewDirectoryNameModification(true)
                .build();
        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
       // activity.startActivityForResult(chooserIntent, RC_DIRECTORY_PICKER_FILE);
    }


    public WebSocket getWebSocket() {
        return webSocket;
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


    private static final String TAG = WebSocketClient.class.getSimpleName();
}
