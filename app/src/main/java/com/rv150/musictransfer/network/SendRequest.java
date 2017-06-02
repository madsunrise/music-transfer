package com.rv150.musictransfer.network;

public class SendRequest {
    public final String fileName;
    public final long fileSize;
    public final String receiverId;

    public SendRequest(String fileName, long fileSize, String receiverId) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.receiverId = receiverId;
    }
}
