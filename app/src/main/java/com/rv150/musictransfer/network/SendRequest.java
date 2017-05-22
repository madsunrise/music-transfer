package com.rv150.musictransfer.network;

/**
 * Created by ivan on 15.05.17.
 */

public class SendRequest {
    private final String fileName;
    private final String receiverId;

    public SendRequest(String receiverId, String fileName) {
        this.fileName = fileName;
        this.receiverId = receiverId;
    }
}
