package com.rv150.musictransfer.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
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
import com.rv150.musictransfer.adapter.MusicListAdapter;
import com.rv150.musictransfer.model.Song;
import com.rv150.musictransfer.network.ProgressRequestBody;
import com.rv150.musictransfer.network.RetrofitClient;
import com.rv150.musictransfer.network.WebSocketClient;
import com.rv150.musictransfer.utils.UiThread;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by ivan on 09.05.17.
 */

public class MusicListFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.music_recycler_view)
    RecyclerView recyclerView;

    private MusicListAdapter adapter;

    private static final int REQUEST_READ_EXT_STORAGE = 0;

    private final RetrofitClient retrofitClient = RetrofitClient.retrofit.create(RetrofitClient.class);

    private final WebSocketClient webSocketClient = WebSocketClient.getInstance();


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
    }



    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);
        Song item = adapter.getSongs().get(itemPosition);
        webSocketClient.sendMessage(item.getTitle());

//        String descriptionString = "hello, this is description speaking";
//        RequestBody description =
//                RequestBody.create(
//                        MediaType.parse("multipart/form-data"), descriptionString);
//
//        ProgressRequestBody fileBody = new ProgressRequestBody(file, this);
//        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), fileBody);
//
//        Call<JsonObdject> request = retrofitClient.uploadFile(filePart);
    }

    private void updateList() {
        Song[] songs = getMusicList();
        adapter = new MusicListAdapter(Arrays.asList(songs), this);
        recyclerView.swapAdapter(adapter, false);
    }

    private Song[] getMusicList() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        // TODO Show explanation message if needed
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXT_STORAGE);
            return new Song[0];
        }

        final Cursor cursor = getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.DISPLAY_NAME,
                                MediaStore.Audio.Media.DATA}, null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        if (cursor == null) {
            Log.e(TAG, "Cursor is null!");
            return new Song[0];
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
        return songs;
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
