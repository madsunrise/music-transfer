package com.rv150.musictransfer.network

class Message(val type: String, val data: String) {
    companion object {
        val INITIALIZE_USER = "GettingID"
        val RECEIVER_ID = "receiver_id"
        val SENDING_FINISHED = "sending_finished"
        val REQUEST_SEND = "requesting_send"
        val ANSWER_ON_REQUEST = "answer_request"
        val ERROR = "error"
        val RECEIVER_FOUND = "receiver_found"
        val RECEIVER_NOT_FOUND = "receiver_not_found"
        val ALLOW_TRANSFERRING = "allow_transferring"
    }
}

