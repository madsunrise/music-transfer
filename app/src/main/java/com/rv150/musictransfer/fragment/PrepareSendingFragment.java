package com.rv150.musictransfer.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
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
import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * Created by ivan on 10.05.17.
 */

public class PrepareSendingFragment extends Fragment implements WebSocketSendClient.PrepareCallback, ZXingScannerView.ResultHandler {

    private static final int REQUEST_CODE = 1232;
    private static final String TAG = PrepareSendingFragment.class.getSimpleName();
    @BindView(R.id.sending_info)
    TextView info;
    @BindView(R.id.receiver_id)
    EditText receiverCode;
    @BindView(R.id.fl)
    FrameLayout fl;
    @BindView(R.id.send)
    Button sendBtn;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    private Song song;
    private Executor networkExecutor = Executors.newSingleThreadExecutor();
    private WebSocketSendClient webSocketSendClient;
    private Callback activity;
    private ZXingScannerView mScannerView;

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

    private boolean checkAccess() {
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAccess() {
        if (!checkAccess()) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
                mScannerView.startCamera();
            } else {
                Toast.makeText(getActivity(), R.string.need_permission, Toast.LENGTH_SHORT).show();
            }
        }
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
        setOnTextChangedListener();

        mScannerView = new ZXingScannerView(getActivity());
//        List<BarcodeFormat> formats = new ArrayList<>();
//        formats.add(BarcodeFormat.QR_CODE);
//        mScannerView.setFormats(formats);
        fl.addView(mScannerView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkAccess()) {
            mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            mScannerView.startCamera();          // Start camera on resume
        } else {
            requestAccess();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        receiverCode.setText(rawResult.getText());
        send();
        // mScannerView.resumeCameraPreview(this);
    }

    @OnClick(R.id.send)
    public void send() {
        final String code = receiverCode.getText().toString();
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

    private void setOnTextChangedListener() {
        receiverCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == getResources().getInteger(R.integer.id_length)) {
                    sendBtn.setEnabled(true);
                } else {
                    sendBtn.setEnabled(false);
                }
            }
        });
    }

    public interface Callback {
        void onSendingStarted(); // Сменить фрагмент
    }
}
