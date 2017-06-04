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
import android.widget.Toast;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.activity.SendActivity;
import com.rv150.musictransfer.adapter.MusicListAdapter;
import com.rv150.musictransfer.model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MusicListFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_READ_EXT_STORAGE = 0;
    private static final String TAG = MusicListFragment.class.getSimpleName();
    private final Observable.OnSubscribe<Song[]> musicListGenerator = (subscriber) -> {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // TODO Show explanation message if needed
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_READ_EXT_STORAGE);
            subscriber.onCompleted();
        }

        final Cursor cursor = getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DATA}, null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        if (cursor == null) {
            Log.e(TAG, "Cursor is null!");
            throw new RuntimeException("Cursor is null!");
        }

        Song[] songs = new Song[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String path = cursor.getString(1);
            long size = getSizeFromPath(path);
            songs[i++] = new Song(title, path, size);
        }
        cursor.close();
        subscriber.onNext(songs);
        subscriber.onCompleted();
    };
    @BindView(R.id.music_recycler_view)
    RecyclerView recyclerView;
    private MusicListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "OnCreateView");
        View view = inflater.inflate(R.layout.music_list_fragment, container, false);
        ButterKnife.bind(this, view);
        setupRecyclerView();
        updateList();
        return view;
    }

    private void setupRecyclerView() {
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
        if (item.getSize() == 0) {
            Toast.makeText(getContext(), R.string.file_not_found_or_corrupted, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), SendActivity.class);
        intent.putExtra("title", item.component1());
        intent.putExtra("path", item.component2());
        intent.putExtra("size", item.component3());
        startActivity(intent);
    }

    private void updateList() {
        Observable.create(musicListGenerator)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
                .subscribe(songs -> adapter.setData(Arrays.asList(songs)),
                e -> Log.e(TAG, "EXCEPTION!" + e));
    }

    private long getSizeFromPath(String path) {
        File file = new File(path);
        return file.length();
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
}
