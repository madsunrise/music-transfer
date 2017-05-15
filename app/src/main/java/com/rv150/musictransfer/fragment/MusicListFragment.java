package com.rv150.musictransfer.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.activity.SendActivity;
import com.rv150.musictransfer.adapter.MusicListAdapter;
import com.rv150.musictransfer.model.Song;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ivan on 09.05.17.
 */

public class MusicListFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.music_recycler_view)
    RecyclerView recyclerView;

    private MusicListAdapter adapter;

    private static final int REQUEST_READ_EXT_STORAGE = 0;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_list_fragment, container, false);
        ButterKnife.bind(this, view);
        setUpRecyclerView();
        updateList();
        return view;
    }



    private void setUpRecyclerView() {
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.setOnClickListener(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new MusicListAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }



    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);
        Song item = adapter.getSongs().get(itemPosition);
        Intent intent = new Intent(getContext(), SendActivity.class);
        intent.putExtra(Song.class.getSimpleName(), item);
        startActivity(intent);
    }

    private void updateList() {
        getMusicList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> adapter.addSong(s),
                        e -> e.printStackTrace(),
                        () -> System.out.println("Completed")
                );
    }

    private Observable<Song> getMusicList()  {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // TODO Show explanation message if needed
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_READ_EXT_STORAGE);
            return Observable.from(new Song[0]);
        }

        final Cursor cursor = getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.DISPLAY_NAME,
                                MediaStore.Audio.Media.DATA}, null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        if (cursor == null) {
            Log.e(TAG, "Cursor is null!");
            throw new RuntimeException("Cursor is null!");
        }

        int count = cursor.getCount();
        Song[] songs = new Song[count];
        int i = 0;
        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String path = cursor.getString(1);
            songs[i++] = new Song(title, path);
        }
        cursor.close();
        return Observable.from(songs);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXT_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateList();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private static final String TAG = MusicListFragment.class.getSimpleName();
}
