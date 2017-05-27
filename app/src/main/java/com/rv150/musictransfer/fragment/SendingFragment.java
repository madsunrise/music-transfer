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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.network.WebSocketSendClient;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ivan on 27.05.17.
 */

public class SendingFragment extends Fragment implements WebSocketSendClient.SenderCallback {

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.status)
    TextView status;

    @BindView(R.id.menu_button)
    Button menuButton;


    private WebSocketSendClient webSocketSendClient;

    {
        try {
            webSocketSendClient = WebSocketSendClient.getInstance();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to create instance of webSocketSendClient! " + ex.getMessage());
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sending_fragment, container, false);
        ButterKnife.bind(this, view);
        webSocketSendClient.setCallback(this);
        return view;
    }


    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected(boolean byServer) {

    }




    @Override
    public void onSendingStarted() {
        status.setText(R.string.sending);
    }

    @Override
    public void onProgressChanged(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    public void onSendingFinished() {
        status.setText(R.string.sending_has_finished);
        status.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        menuButton.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketSendClient.setCallback(null);
        webSocketSendClient.disconnect();
    }

    @Override
    public void onError(int errorCode) {

    }

    @OnClick(R.id.menu_button)
    void backToMenu() {
        getActivity().finish();
    }

    private static final String TAG = SendingFragment.class.getSimpleName();
}

