package com.rv150.musictransfer.model;

/**
 * Created by ivan on 09.05.17.
 */

public class Song {
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
}
