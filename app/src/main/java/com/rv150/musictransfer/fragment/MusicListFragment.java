package com.rv150.musictransfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.adapter.MusicListAdapter;
import com.rv150.musictransfer.model.Song;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ivan on 09.05.17.
 */

public class MusicListFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.music_recycler_view)
    RecyclerView recyclerView;

    private MusicListAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_list_fragment, container, false);
        ButterKnife.bind(this, view);
        setUpRecyclerView();
        return view;
    }



    private void setUpRecyclerView() {
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);
        Song item = adapter.getSongs().get(itemPosition);

    }


    private static final String TAG = MusicListFragment.class.getSimpleName();
}
