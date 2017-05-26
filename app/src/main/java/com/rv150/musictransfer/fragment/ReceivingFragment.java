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
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocketException;
import com.rv150.musictransfer.R;
import com.rv150.musictransfer.network.WebSocketClient;
import com.rv150.musictransfer.utils.UiThread;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rv150.musictransfer.network.WebSocketClient.CONNECTION_ERROR;

/**
 * Created by ivan on 26.05.17.
 */

public class ReceivingFragment extends Fragment implements WebSocketClient.Callback {

    @BindView(R.id.status)
    TextView status;

    @BindView(R.id.your_id)
    TextView yourId;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.retry)
    Button retryBtn;

    private Long id;

    private WebSocketClient webSocketClient;

    {
        try {
            webSocketClient = WebSocketClient.getInstance();
        }
        catch (IOException ex) {
            Log.e(TAG, "Failed to create instance of webSocketClient! " + ex.getMessage());
        }
    }

    private final Executor networkExecutor = Executors.newSingleThreadExecutor();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.receiving_fragment, container, false);
        ButterKnife.bind(this, view);
        webSocketClient.setCallback(this);
        connectToWebsocket();
        return view;
    }


    @Override
    public void onIdRegistered(long id) {
        this.id = id;
        progressBar.setVisibility(View.GONE);
        status.setVisibility(View.VISIBLE);
        status.setText(R.string.connection_established);
        status.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        yourId.setVisibility(View.VISIBLE);
        yourId.setText(String.format(getString(R.string.your_id_is), id));
        retryBtn.setVisibility(View.GONE);
    }

    @Override
    public void onSendRequest(String fileName) {

    }

    @Override
    public void onFileReceived() {

    }

    @Override
    public void onError(int errorCode) {
        switch (errorCode) {
            case CONNECTION_ERROR: {
                progressBar.setVisibility(View.GONE);
                status.setVisibility(View.VISIBLE);
                status.setText(R.string.no_connection);
                status.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                Toast.makeText(getContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
                yourId.setVisibility(View.INVISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
                break;
            }
        }
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
                    Log.e(TAG, "Failed to create websocket! " + ex.getMessage());
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
        webSocketClient.disconnect();
    }

    private static final String TAG = ReceivingFragment.class.getSimpleName();
}
