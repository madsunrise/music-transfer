package com.rv150.musictransfer.model;

/**
 * Created by ivan on 09.05.17.
 */

public class Song {
    private final String title;
    private final long size; // in bytes

    public Song(String title, long size) {
        this.title = title;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public long getSize() {
        return size;
    }
}
