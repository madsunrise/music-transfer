package com.rv150.musictransfer.model

import android.os.Parcel
import android.os.Parcelable

data class Song(val title: String, val path: String, val size: Long) : Parcelable {

    companion object {
        @JvmField @Suppress("unused")
        val CREATOR = object : Parcelable.Creator<Song> {
            override fun createFromParcel(source: Parcel?): Song {
                val parcel = source!!
                return Song(parcel.readString(), parcel.readString(), parcel.readLong())
            }

            override fun newArray(size: Int): Array<Song?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        val parcel = dest!!
        parcel.writeString(title)
        parcel.writeString(path)
        parcel.writeLong(size)
    }

    override fun describeContents() = 0
}
