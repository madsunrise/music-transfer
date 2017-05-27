package com.rv150.musictransfer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocketException;
import com.rv150.musictransfer.R;
import com.rv150.musictransfer.network.WebSocketReceiveClient;
import com.rv150.musictransfer.utils.UiThread;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by ivan on 26.05.17.
 */

public class PrepareReceivingFragment extends Fragment implements WebSocketReceiveClient.PrepareCallback {

    @BindView(R.id.status)
    TextView status;

    @BindView(R.id.your_id)
    TextView yourId;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.retry)
    Button retryBtn;

    private Long id;

    private WebSocketReceiveClient webSocketClient;

    {
        try {
            webSocketClient = WebSocketReceiveClient.getInstance();
        }
        catch (IOException ex) {
            Log.e(TAG, "Failed to create instance of webSocketClient! " + ex.getMessage());
        }
    }

    private final Executor networkExecutor = Executors.newSingleThreadExecutor();

    private Callback activity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.prepare_receiving_fragment, container, false);
        ButterKnife.bind(this, view);
        webSocketClient.setCallback(this);
        connectToWebsocket();
        return view;
    }


    @Override
    public void onIdRegistered(long id) {
        this.id = id;
        yourId.setVisibility(View.VISIBLE);
        yourId.setText(String.format(getString(R.string.your_id_is), id));
    }

    @Override
    public void onFileSendingRequest(String fileName) {
        new AlertDialog.Builder(getContext())
                .setMessage(String.format(getString(R.string.do_you_want_to_accept_new_file), fileName))
                .setPositiveButton(R.string.yes, (d, w) -> sendAnswerOnRequest(true))
                .setNegativeButton(R.string.no, (d, w) -> sendAnswerOnRequest(false))
                .show();
    }



    private void sendAnswerOnRequest(boolean accept) {
        networkExecutor.execute(() -> webSocketClient.sendAnswerOnRequest(accept));
        if (accept) {
            activity.onReceivingStarted();
        }
    }

    @Override
    public void onConnected() {
        progressBar.setVisibility(View.GONE);
        status.setVisibility(View.VISIBLE);
        status.setText(R.string.connection_established);
        status.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        retryBtn.setVisibility(View.GONE);
    }

    @Override
    public void onDisconnected(boolean byServer) {
        if (!byServer) {
            progressBar.setVisibility(View.GONE);
            status.setVisibility(View.VISIBLE);
            status.setText(R.string.no_connection);
            status.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            Toast.makeText(getContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
            yourId.setVisibility(View.INVISIBLE);
            retryBtn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onError(int errorCode) {

    }

    @OnClick(R.id.retry)
    void connectToWebsocket() {
        networkExecutor.execute(() ->  {
            try {
                webSocketClient.connect();
            }
            catch (WebSocketException | IOException ex) {
                UiThread.run(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Failed to connect to websocket! " + ex.getMessage());
                    status.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(),
                            R.string.connection_error, Toast.LENGTH_SHORT).show();
                    status.setText(R.string.no_connection);
                    status.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    retryBtn.setVisibility(View.VISIBLE);
                });
            }});
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketClient.setCallback(null);
    }

    public interface Callback {
        void onReceivingStarted();
    }

    private static final String TAG = PrepareReceivingFragment.class.getSimpleName();
}
