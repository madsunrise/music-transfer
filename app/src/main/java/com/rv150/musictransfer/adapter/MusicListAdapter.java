package com.rv150.musictransfer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.fragment.MusicListFragment;
import com.rv150.musictransfer.model.Song;

import java.util.Collections;
import java.util.List;

/**
 * Created by ivan on 09.05.17.
 */

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MyViewHolder> {

    private final List<Song> songs;
    private final MusicListFragment feedFragment;

    public MusicListAdapter(List<Song> songs, MusicListFragment fragment) {
        this.songs = Collections.unmodifiableList(songs);
        feedFragment = fragment;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_list_item, parent, false);
        itemView.setOnClickListener(feedFragment);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Song service = songs.get(position);
        holder.title.setText(service.getTitle());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.item_title);
        }
    }

    public List<Song> getSongs() {
        return songs;
    }
}


