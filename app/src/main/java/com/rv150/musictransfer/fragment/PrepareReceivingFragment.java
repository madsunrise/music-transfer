package com.rv150.musictransfer.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.neovisionaries.ws.client.WebSocketException;
import com.rv150.musictransfer.R;
import com.rv150.musictransfer.network.WebSocketReceiveClient;
import com.rv150.musictransfer.utils.Config;
import com.rv150.musictransfer.utils.UiThread;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rv150.musictransfer.utils.Utils.encodeAsBitmap;


/**
 * Created by ivan on 26.05.17.
 */

public class PrepareReceivingFragment extends Fragment implements WebSocketReceiveClient.PrepareCallback {

    private static final String TAG = PrepareReceivingFragment.class.getSimpleName();
    private final Executor networkExecutor = Executors.newSingleThreadExecutor();
    @BindView(R.id.your_id)
    TextView yourId;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.qr)
    ImageView imageView;
    @BindView(R.id.retry)
    Button retryBtn;
    @BindView(R.id.no_connection)
    LinearLayout noConnection;
    @BindView(R.id.connection)
    ViewGroup connection;
    private Long id;
    private WebSocketReceiveClient webSocketClient;
    private Callback activity;

    {
        try {
            webSocketClient = WebSocketReceiveClient.getInstance();
        } catch (IOException ex) {
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
        View view = inflater.inflate(R.layout.prepare_receiving_fragment, container, false);
        ButterKnife.bind(this, view);
        webSocketClient.setCallback(this);
        if (savedInstanceState != null && savedInstanceState.containsKey(Config.ID_KEY)) {
            onConnected();
            onIdRegistered(savedInstanceState.getLong(Config.ID_KEY));
        } else {
            connectToWebsocket();
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Config.ID_KEY, id);
    }

    @Override
    public void onIdRegistered(long id) {
        this.id = id;
        connection.setVisibility(View.VISIBLE);
        yourId.setText(String.format(getString(R.string.your_id_is), id));
        try {
            Bitmap bitmap = encodeAsBitmap(Long.toString(id), (int) getResources().getDimension(R.dimen.qr));
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
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
        connection.setVisibility(View.VISIBLE);
        noConnection.setVisibility(View.GONE);
    }

    @Override
    public void onDisconnected(boolean byServer) {
        if (!byServer) {
            progressBar.setVisibility(View.GONE);
            noConnection.setVisibility(View.GONE);
            Toast.makeText(getContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(int errorCode) {

    }

    @OnClick(R.id.retry)
    void connectToWebsocket() {
        networkExecutor.execute(() -> {
            try {
                webSocketClient.connect();
            } catch (WebSocketException | IOException ex) {
                UiThread.run(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Failed to connect to websocket! " + ex.getMessage());
                    noConnection.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(),
                            R.string.connection_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketClient.setCallback(null);
    }

    public interface Callback {
        void onReceivingStarted();
    }
}
