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

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(path);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private Song(Parcel in) {
        title = in.readString();
        path = in.readString();
    }


    // In the vast majority of cases you can simply return 0 for this.
    // There are cases where you need to use the constant `CONTENTS_FILE_DESCRIPTOR`
    // But this is out of scope of this tutorial
    @Override
    public int describeContents() {
        return 0;
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<Song> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Song> CREATOR
            = new Parcelable.Creator<Song>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
