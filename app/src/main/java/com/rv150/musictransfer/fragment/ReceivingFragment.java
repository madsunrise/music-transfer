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

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.network.WebSocketReceiveClient;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ivan on 27.05.17.
 */

public class ReceivingFragment extends Fragment implements WebSocketReceiveClient.ReceiverCallback {
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
        Log.d(TAG, "OnCreateView!");
        webSocketClient.setReceiverCallback(this);
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
        webSocketClient.setReceiverCallback(null);
    }


    @Override
    public void onProgressChanged() {

    }

    private static final String TAG = ReceivingFragment.class.getSimpleName();

}