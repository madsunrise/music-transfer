package com.rv150.musictransfer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocketException;
import com.rv150.musictransfer.R;
import com.rv150.musictransfer.model.Song;
import com.rv150.musictransfer.network.WebSocketSendClient;
import com.rv150.musictransfer.utils.UiThread;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by ivan on 10.05.17.
 */

public class PrepareSendingFragment extends Fragment implements WebSocketSendClient.PrepareCallback {

    @BindView(R.id.sending_info)
    TextView info;

    @BindView(R.id.receiver_id)
    EditText receiverCode;

    @BindView(R.id.send)
    Button sendBtn;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private Song song;

    private Executor networkExecutor = Executors.newSingleThreadExecutor();

    private WebSocketSendClient webSocketSendClient;

    private Callback activity;

    {
        try {
            webSocketSendClient = WebSocketSendClient.getInstance();
        }
        catch (IOException ex) {
            Log.e(TAG, "Failed to create instance of webSocketSendClient! " + ex.getMessage());
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.prepare_sending_fragment, container, false);
        ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        song = (Song) bundle.get(Song.class.getSimpleName());
        if (song != null) {
            info.setText(String.format(getString(R.string.sending_songname), song.getTitle()));
        }
        webSocketSendClient.setCallback(this);
        return view;
    }


    @OnClick(R.id.send)
    public void send() {
        final String code = receiverCode.getText().toString();
        int idLength = getResources().getInteger(R.integer.id_length);
        if (code.length() < idLength) {
            String msg = String.format(getString(R.string.id_must_consist_of_n_digits), idLength);
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!webSocketSendClient.isConnected()) {
            connectWebSocket();
        }
        setUiEnabled(false);
        networkExecutor.execute(() -> webSocketSendClient.registerSongForTransferring(song, code));
    }


    private void connectWebSocket() {
        networkExecutor.execute(() -> {
            try {
                webSocketSendClient.connect();
            } catch (WebSocketException | IOException ex) {
                UiThread.run(() -> {
                    Log.e(TAG, "Failed to connect to websocket! " + ex.getMessage());
                    Toast.makeText(getContext(),
                            R.string.connection_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    @Override
    public void onConnected() {

    }


    @Override
    public void onDisconnected(boolean byServer) {
        //TODO if network disappears
    }

    @Override
    public void onReceiverFound(boolean found) {
        if (found) {
            activity.onSendingStarted();
            return;
        }
        Toast.makeText(getContext(), R.string.receiver_with_this_id_not_found, Toast.LENGTH_SHORT).show();
        setUiEnabled(true);
    }

    @Override
    public void onError(int errorCode) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketSendClient.setCallback(null);
    }


    private void setUiEnabled(boolean enabled) {
        if (enabled) {
            sendBtn.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        }
        else {
            sendBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }
    }



    public interface Callback {
        void onSendingStarted(); // Сменить фрагмент
    }

    private static final String TAG = PrepareSendingFragment.class.getSimpleName();
}
