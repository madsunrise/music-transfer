package com.rv150.musictransfer.network;

public class Message {
    public static final String INITIALIZE_USER = "GettingID";
    public static final String RECEIVER_ID = "receiver_id";
    public static final String SENDING_FINISHED = "sending_finished";
    public static final String REQUEST_SEND = "requesting_send";
    public static final String ANSWER_ON_REQUEST = "answer_request";
    public static final String ERROR = "error";
    public static final String RECEIVER_FOUND = "receiver_found";
    public static final String RECEIVER_NOT_FOUND = "receiver_not_found";
    public static final String ALLOW_TRANSFERRING = "allow_transferring";
    private final String type;
    private final String data;

    public Message(String type, String data) {
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

