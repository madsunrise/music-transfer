package com.rv150.musictransfer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in.readString(), in.readString(), in.readLong());
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    public final String title;
    public final String path;
    public final long size;

    public Song(String title, String path, long size) {
        this.title = title;
        this.path = path;
        this.size = size;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(path);
        out.writeLong(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
