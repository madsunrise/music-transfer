package com.rv150.musictransfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rv150.musictransfer.R;
import com.rv150.musictransfer.model.Song;
import com.rv150.musictransfer.network.Message;
import com.rv150.musictransfer.network.SendRequest;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rv150.musictransfer.network.Message.RECEIVER_ID;


/**
 * Created by ivan on 10.05.17.
 */

public class PrepareSendingFragment extends Fragment {

    @BindView(R.id.sending_info)
    TextView info;

    @BindView(R.id.receiver_id)
    EditText receiverCode;

    private Song song;

    private Executor executor = Executors.newSingleThreadExecutor();



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.prepare_sending_fragment, container, false);
        ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        song = (Song) bundle.get(Song.class.getSimpleName());
        if (song != null) {
            info.setText("Передача " + song.getTitle() + "...");
        }
        return view;
    }


    @OnClick(R.id.send)
    public void send() {
        final String code = receiverCode.getText().toString();
        executor.execute(() -> {
            //WebSocketClient webSocketClient = WebSocketClient.getInstance();
            Message message = new Message(RECEIVER_ID, new Gson().toJson(new SendRequest(code, song.getTitle())));
          //  webSocketClient.getWebSocket().sendText(new Gson().toJson(message));    // TODO NPE

            String path = song.getPath();
           // webSocketClient.registerFileToSend(path);
        });
    }



    private static final String TAG = PrepareSendingFragment.class.getSimpleName();
}
