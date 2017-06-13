package com.rv150.musictransfer.network

import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.neovisionaries.ws.client.*
import com.rv150.musictransfer.utils.Config.BUFFER_SIZE
import com.rv150.musictransfer.utils.Config.WEBSOCKET_URL
import com.rv150.musictransfer.utils.UiThread
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class WebSocketDownloadClient private constructor() : WebSocketAdapter() {
    private var webSocket: WebSocket? = null
    private var outputStream: BufferedOutputStream? = null
    private var currentFileName: String? = null
    private var currentFileSize: Long = 0
    private var prepareCallback: PrepareCallback? = null
    private var receiverCallback: ReceiverCallback? = null
    private var iteration = 1

    @Throws(Exception::class)
    override fun onBinaryMessage(websocket: WebSocket?, binary: ByteArray?) {
        if (currentFileName == null) {
            Log.e(TAG, "Getting binary data without filename registration!")
            return
        }
        if (outputStream == null) {
            val dir = Environment.getExternalStorageDirectory()
            val file = File(dir, currentFileName!!)
            if (!file.createNewFile()) {
                Log.e(TAG, "Failed to create new file! Permissions?")
                receiverCallback!!.onError(FILE_CREATION_ERROR)
                return
            }
            val os = FileOutputStream(file)
            outputStream = BufferedOutputStream(os)
            Log.d(TAG, "Output stream was initialized")
        }
        outputStream!!.write(binary!!)
        val percentage = ((iteration * BUFFER_SIZE * 100).toDouble() / currentFileSize).toInt()
        Log.d(TAG, "Receiving file: $percentage%")
        UiThread.run {
            if (receiverCallback != null) {
                receiverCallback!!.onProgress(percentage)
            }
        }
        iteration++
    }

    @Throws(Exception::class)
    override fun onTextMessage(websocket: WebSocket?, text: String?) {
        val message = gson.fromJson(text, Message::class.java)
        when (message.type) {
            Message.INITIALIZE_USER -> {
                val id = java.lang.Long.valueOf(message.data)!!
                Log.d(TAG, "Getting ID = " + id)
                prepareCallback!!.onIdRegister(id)
            }


            Message.SENDING_FINISHED -> {
                Log.d(TAG, "FINISHED Receiving file!")
                UiThread.run {
                    if (receiverCallback != null) {
                        receiverCallback!!.onFileReceivingFinished()
                    }
                }
                outputStream!!.flush()
                outputStream!!.close()
            }

            Message.REQUEST_SEND -> {
                val request = gson.fromJson(message.data, SendRequest::class.java)
                currentFileName = request.fileName
                currentFileSize = request.fileSize
                Log.d(TAG, "Getted request on sending $currentFileName ($currentFileSize bytes)")
                UiThread.run {
                    if (prepareCallback != null) {
                        prepareCallback!!.onFileUploadRequest(currentFileName ?: "")
                    }
                }
            }

            else -> {
            }
        }
    }

    fun sendAnswerOnRequest(answer: Boolean) {
        Log.d(TAG, "Sending answer in request: " + answer.toString())
        val message = Message(Message.ANSWER_ON_REQUEST, answer.toString())
        webSocket!!.sendText(gson.toJson(message))
    }

    @Throws(WebSocketException::class, IOException::class)
    fun connect() {
        webSocket = WebSocketFactory()
                .createSocket(WEBSOCKET_URL, 5000)
                .addListener(this)
        webSocket!!.connect()
        Log.d(TAG, "Connected!")
    }

    fun disconnect() {
        webSocket!!.disconnect()
        Log.d(TAG, "Disconnected!")
    }

    val isConnected: Boolean
        get() = webSocket!!.isOpen

    @Throws(Exception::class)
    override fun onConnected(websocket: WebSocket?, headers: Map<String, List<String>>?) {
        super.onConnected(websocket, headers)
        UiThread.run {
            if (prepareCallback != null) {
                prepareCallback!!.onConnect()
            }
            if (receiverCallback != null) {
                receiverCallback!!.onConnect()
            }
        }
    }

    @Throws(Exception::class)
    override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
        Log.d(TAG, "Disconnected! By server - " + closedByServer)
        UiThread.run {
            if (prepareCallback != null) {
                prepareCallback!!.onDisconnect(closedByServer)
            }
            if (receiverCallback != null) {
                receiverCallback!!.onDisconnect(closedByServer)
            }
        }
    }

    @Throws(Exception::class)
    override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
        super.onConnectError(websocket, exception)
        Log.d(TAG, "Connection ERROR!!! Reason: " + exception!!.message)
    }

    // Привязан должен быть только один коллбек в один момент времени
    fun setCallback(callback: CommonCallback?) {
        if (callback is PrepareCallback) {
            prepareCallback = callback
            receiverCallback = null
        } else {
            receiverCallback = callback as ReceiverCallback?
            prepareCallback = null
        }
    }

    interface CommonCallback {
        fun onConnect()

        fun onDisconnect(byServer: Boolean)

        fun onError(errorCode: Int)
    }


    interface PrepareCallback : CommonCallback {
        fun onIdRegister(id: Long)

        fun onFileUploadRequest(fileName: String)
    }

    interface ReceiverCallback : CommonCallback {
        fun onProgress(percent: Int)

        fun onFileReceivingFinished()
    }

    companion object {
        val FILE_CREATION_ERROR = 0
        private val TAG = WebSocketDownloadClient::class.java.simpleName
        val instance = WebSocketDownloadClient()
        private val gson = Gson()
    }
}

