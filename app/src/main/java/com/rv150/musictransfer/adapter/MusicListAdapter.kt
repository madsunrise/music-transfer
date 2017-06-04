package com.rv150.musictransfer.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rv150.musictransfer.R
import com.rv150.musictransfer.fragment.MusicListFragment
import com.rv150.musictransfer.model.Song

class MusicListAdapter(private var songs: List<Song>, private val feedFragment: MusicListFragment) : RecyclerView.Adapter<MusicListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.music_list_item, parent, false)
        itemView.setOnClickListener(feedFragment)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val service = songs[position]
        holder.title.text = service.title
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun setData(songs: List<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun getSongs(): List<Song> {
        return songs
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.item_title) as TextView
    }
}


