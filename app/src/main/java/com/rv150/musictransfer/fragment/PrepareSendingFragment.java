package com.rv150.musictransfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.rv150.musictransfer.R;
import com.rv150.musictransfer.model.Song;
import com.rv150.musictransfer.network.WebSocketClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okio.ByteString;

/**
 * Created by ivan on 10.05.17.
 */

public class PrepareSendingFragment extends Fragment {

    @BindView(R.id.sending_info)
    TextView info;

    private Song song;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.prepare_sending_fragment, container, false);
        ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        song = (Song) bundle.get(Song.class.getSimpleName());
        info.setText("Передача " + song.getTitle() + "...");
        new Thread(sendingSong).start();
        return view;
    }

  private Runnable sendingSong = new Runnable() {
      @Override
      public void run() {
          int BUFFER_SIZE = 1024;
          WebSocketClient webSocketClient = WebSocketClient.getInstance();
          String path = song.getPath();
          File file = new File(path);
          try {
              InputStream is = new FileInputStream(file);
              byte[] chunk = new byte[BUFFER_SIZE];

//              if (is.read(chunk) == -1) {
//                  return;
//              }
//
//              WebSocketFrame firstFrame = WebSocketFrame.createBinaryFrame(chunk)
//                      .setFin(false);
//
//              webSocketClient.getWebSocket().sendBinary(chunk, false);


              int chunkLen;
              while ((chunkLen = is.read(chunk)) != -1) {
                  webSocketClient.getWebSocket().sendBinary(chunk);
              }

          } catch (IOException ex) {
              Log.e(TAG, ex.getMessage());
          }
      }
  };

    private static final String TAG = PrepareSendingFragment.class.getSimpleName();
}
