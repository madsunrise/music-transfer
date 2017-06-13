package com.rv150.musictransfer.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.rv150.musictransfer.R
import com.rv150.musictransfer.activity.SendActivity
import com.rv150.musictransfer.adapter.MusicListAdapter
import com.rv150.musictransfer.model.Song
import kotterknife.bindView
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.*

class MusicListFragment : BoundFragment(), View.OnClickListener {
    private val musicListGenerator: Observable.OnSubscribe<Array<Song>> = Observable.OnSubscribe<Array<Song>> { subscriber ->
        val permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // TODO Show explanation message if needed
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_READ_EXT_STORAGE)
            subscriber.onCompleted()
        }

        val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA), null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC")

        if (cursor == null) {
            Log.e(TAG, "Cursor is null!")
            throw RuntimeException("Cursor is null!")
        }

        val songs = ArrayList<Song>()
        while (cursor.moveToNext()) {
            val title = cursor.getString(0)
            val path = cursor.getString(1)
            val size = getSizeFromPath(path)
            songs.add(Song(title, path, size))
        }
        cursor.close()
        val songArray = songs.toTypedArray()
        subscriber.onNext(songArray)
        subscriber.onCompleted()
    }


    val recyclerView by bindView<RecyclerView>(R.id.music_recycler_view)
    private var adapter: MusicListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater?.inflate(R.layout.music_list_fragment, container, false)
        setupRecyclerView()
        updateList()
        return root
    }

    private fun setupRecyclerView() {
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = llm
        recyclerView.setHasFixedSize(true)
        recyclerView.setOnClickListener(this)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)
        adapter = MusicListAdapter(ArrayList<Song>(), this)
        recyclerView.adapter = adapter
    }

    override fun onClick(v: View) {
        val itemPosition = recyclerView.getChildLayoutPosition(v)
        val song = adapter!!.getSongs()[itemPosition]
        if (song.size == 0L) {
            Toast.makeText(context, R.string.file_not_found_or_corrupted, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(context, SendActivity::class.java)
        intent.putExtra(Song::class.java.simpleName, song)
        startActivity(intent)
    }

    private fun updateList() {
        Observable.create<Array<Song>>(musicListGenerator)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ songs -> adapter!!.setData(Arrays.asList(*songs)) }
                ) { e -> Log.e(TAG, "EXCEPTION!" + e) }
    }

    private fun getSizeFromPath(path: String): Long {
        val file = File(path)
        return file.length()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_READ_EXT_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateList()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {

        private val REQUEST_READ_EXT_STORAGE = 0
        private val TAG = MusicListFragment::class.java.simpleName
    }
}
