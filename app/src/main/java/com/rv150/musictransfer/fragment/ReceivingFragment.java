package com.rv150.musictransfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.network.WebSocketReceiveClient;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.rv150.musictransfer.network.WebSocketReceiveClient.FILE_CREATION_ERROR;

public class ReceivingFragment extends Fragment implements WebSocketReceiveClient.ReceiverCallback {
    private static final String TAG = ReceivingFragment.class.getSimpleName();
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.status)
    TextView status;
    private WebSocketReceiveClient webSocketClient;

    {
        try {
            webSocketClient = WebSocketReceiveClient.getInstance();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to create instance of webSocketClient! " + ex.getMessage());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.receiving_fragment, container, false);
        ButterKnife.bind(this, view);
        webSocketClient.setCallback(this);
        return view;
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected(boolean byServer) {

    }

    @Override
    public void onFileReceivingFinished() {
        status.setText(R.string.file_has_been_received);
        status.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketClient.setCallback(null);
    }

    @Override
    public void onError(int errorCode) {
        switch (errorCode) {
            case FILE_CREATION_ERROR: {
                Toast.makeText(getContext(), R.string.failed_to_create_file, Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onProgressChanged(int percentage) {
        progressBar.setProgress(percentage);
    }

}