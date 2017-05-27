package com.rv150.musictransfer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ivan on 09.05.17.
 */

public class Song implements Parcelable {
    private final String title;
    private final String path;
    private final long size;

    public Song(String title, String path, long size) {
        this.title = title;
        this.path = path;
        this.size = size;
    }

    private Song(Parcel in) {
        title = in.readString();
        path = in.readString();
        size = in.readLong();
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

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }
}
