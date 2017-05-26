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
import com.rv150.musictransfer.network.WebSocketClient;
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

public class PrepareSendingFragment extends Fragment implements WebSocketClient.SenderCallback {

    @BindView(R.id.sending_info)
    TextView info;

    @BindView(R.id.receiver_id)
    EditText receiverCode;

    @BindView(R.id.send)
    Button sendBtn;

    private Song song;

    private Executor networkExecutor = Executors.newSingleThreadExecutor();

    private WebSocketClient webSocketClient;

    private Callback activity;

    {
        try {
            webSocketClient = WebSocketClient.getInstance();
        }
        catch (IOException ex) {
            Log.e(TAG, "Failed to create instance of webSocketClient! " + ex.getMessage());
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
        webSocketClient.setSenderCallback(this);
        connectWebSocket();
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

        activity.onSendingStarted();
        networkExecutor.execute(() -> webSocketClient.registerSongForTransferring(song, code));
    }


    private void connectWebSocket() {
        networkExecutor.execute(() -> {
            try {
                webSocketClient.connect();
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
    public void onTransferringAllowed() {

    }

    @Override
    public void onError(int errorCode) {

    }

    @Override
    public void onConnected() {
        sendBtn.setEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketClient.setSenderCallback(null);
    }

    @Override
    public void onProgressChanged(int progress) {

    }


    public interface Callback {
        void onSendingStarted(); // Сменить фрагмент
    }

    private static final String TAG = PrepareSendingFragment.class.getSimpleName();
}
