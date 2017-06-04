package com.rv150.musictransfer.network

data class SendRequest(val fileName: String, val fileSize: Long, val receiverId: String)
