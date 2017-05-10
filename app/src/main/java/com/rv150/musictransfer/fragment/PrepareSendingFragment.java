package com.rv150.musictransfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.model.Song;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        return view;
    }
}
