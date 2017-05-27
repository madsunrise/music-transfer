package com.rv150.musictransfer.network;

/**
 * Created by ivan on 15.05.17.
 */

public class SendRequest {
    private String fileName;
    private long fileSize;
    private String receiverId;

    public SendRequest() {
    }

    public SendRequest(String fileName, long fileSize, String receiverId) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.receiverId = receiverId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getReceiverId() {
        return receiverId;
    }
}
