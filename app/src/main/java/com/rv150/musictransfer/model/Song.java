package com.rv150.musictransfer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ivan on 09.05.17.
 */

public class Song implements Parcelable {
    private final String title;
    private final String path;

    public Song(String title, String path) {
        this.title = title;
        this.path = path;
    }

    private Song(Parcel in) {
        title = in.readString();
        path = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(path);
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
}
