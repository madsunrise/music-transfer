package com.rv150.musictransfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.model.Song;
import com.rv150.musictransfer.network.WebSocketClient;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ivan on 27.05.17.
 */

public class SendingFragment extends Fragment implements WebSocketClient.SenderCallback {

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.status)
    TextView status;

    private Executor networkExecutor = Executors.newSingleThreadExecutor();

    private WebSocketClient webSocketClient;

    {
        try {
            webSocketClient = WebSocketClient.getInstance();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to create instance of webSocketClient! " + ex.getMessage());
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sending_fragment, container, false);
        ButterKnife.bind(this, view);
        webSocketClient.setSenderCallback(this);
        return view;
    }


    @Override
    public void onTransferringAllowed() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onProgressChanged(int progress) {
        if (progress == 0) {
            status.setText(R.string.sending);
            return;
        }

        progressBar.setProgress(progress);

        if (progress == 100) {
            status.setText(R.string.sending_has_finished);
            status.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }
    }

    @Override
    public void onError(int errorCode) {

    }

    private static final String TAG = SendingFragment.class.getSimpleName();
}

