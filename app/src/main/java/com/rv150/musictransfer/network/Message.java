package com.rv150.musictransfer.network;

/**
 * Created by ivan on 10.05.17.
 */

public class Message {
    private final String type;
    private final String data;

    public static final String INITIALIZE_USER = "GettingID";
    public static final String FILE = "File";

    Message(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}

