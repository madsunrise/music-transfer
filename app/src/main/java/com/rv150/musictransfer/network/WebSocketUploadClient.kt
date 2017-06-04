package com.rv150.musictransfer.network


import android.util.Log
import com.google.gson.Gson
import com.neovisionaries.ws.client.*
import com.rv150.musictransfer.model.Song
import com.rv150.musictransfer.utils.Config.BUFFER_SIZE
import com.rv150.musictransfer.utils.Config.WEBSOCKET_URL
import com.rv150.musictransfer.utils.UiThread
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class WebSocketUploadClient private constructor() : WebSocketAdapter() {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var songForTransfer: Song? = null
    private var prepareCallback: PrepareCallback? = null
    private var senderCallback: SenderCallback? = null

    @Throws(Exception::class)
    override fun onTextMessage(websocket: WebSocket?, text: String?) {
        val message = gson.fromJson(text, Message::class.java)
        when (message.type) {

            Message.RECEIVER_FOUND -> {
                Log.d(TAG, "Receiver was found, waiting for confirmation...")
                UiThread.run {
                    if (prepareCallback != null) {
                        prepareCallback!!.onReceiverFound(true)
                    }
                }
            }


            Message.ALLOW_TRANSFERRING -> {
                // TODO May be some sleep for 100-200 ms?
                Log.d(TAG, "Transfer has been approved, start sending!")
                UiThread.run {
                    if (senderCallback != null) {
                        senderCallback!!.onUploadStart()
                    }
                }
                sendFile()
            }

            Message.ERROR -> {
                Log.d(TAG, "Error! " + message.data)
                when (message.data) {
                    Message.RECEIVER_NOT_FOUND -> {
                        UiThread.run {
                            if (prepareCallback != null) {
                                prepareCallback!!.onReceiverFound(false)
                            }
                        }
                    }
                    else -> {
                    }
                }
            }

            else -> {
            }
        }
    }

    fun registerSongForTransferring(song: Song, receiverId: String) {
        Log.d(TAG, "Registered song " + song.title + " for transferring to " + receiverId)
        val request = SendRequest(song.title, song.size, receiverId)
        val message = Message(Message.RECEIVER_ID, gson.toJson(request))
        webSocket!!.sendText(gson.toJson(message))
        this.songForTransfer = song
    }

    private fun sendFile() {
        if (songForTransfer == null) {
            Log.e(TAG, "Error sending file - songForTransfer is null!")
            return
        }
        val file = File(songForTransfer!!.path)
        try {
            val `is` = FileInputStream(file)
            val chunk = ByteArray(BUFFER_SIZE)

            val totalSize = file.length()
            var chunkLen: Int
            var i = 1
            while (true) {
                chunkLen = `is`.read(chunk, 0, BUFFER_SIZE)
                if (chunkLen == -1) {
                    break;
                }
                val sended = Arrays.copyOfRange(chunk, 0, chunkLen)
                webSocket!!.sendBinary(sended)
                val percentage = ((i * BUFFER_SIZE * 100).toDouble() / totalSize).toInt()
                UiThread.run {
                    if (senderCallback != null) {
                        senderCallback!!.onProgress(percentage)
                    }
                }
                Log.d(TAG, "Sended $percentage%")
                i++
            }
            sendFinishSignal()
            UiThread.run {
                if (senderCallback != null) {
                    senderCallback!!.onUploadFinish()
                }
            }
            Log.d(TAG, "Transferring has been finished")
        } catch (ex: IOException) {
            Log.e(TAG, ex.message)
        }

    }

    @Throws(WebSocketException::class, IOException::class)
    fun connect() {
        webSocket = WebSocketFactory()
                .createSocket(WEBSOCKET_URL, 5000)
                .addListener(this)
                .setFrameQueueSize(1)
        webSocket!!.connect()
        Log.d(TAG, "Connected!")
    }

    fun disconnect() {
        webSocket!!.disconnect()
        Log.d(TAG, "Disconnect method called!")
    }

    val isConnected: Boolean
        get() = webSocket != null && webSocket!!.isOpen

    private fun sendFinishSignal() {
        try {
            val message = Message(Message.SENDING_FINISHED, "ok")
            val json = gson.toJson(message)
            webSocket!!.sendText(json)
            Log.d(TAG, "Sending finish signal!")
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }

    }

    @Throws(Exception::class)
    override fun onConnected(websocket: WebSocket?, headers: Map<String, List<String>>?) {
        super.onConnected(websocket, headers)
        UiThread.run {
            if (senderCallback != null) {
                senderCallback!!.onConnected()
            }
        }
    }

    @Throws(Exception::class)
    override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
        Log.d(TAG, "Disconnected! By server - " + closedByServer)
        UiThread.run {
            if (senderCallback != null) {
                senderCallback!!.onDisconnected(closedByServer)
            }
        }
    }

    @Throws(Exception::class)
    override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
        super.onConnectError(websocket, exception)
        Log.d(TAG, "Connection ERROR!!! Reason: " + exception!!.message)
    }

    fun setCallback(callback: CommonCallback?) {
        if (callback is PrepareCallback) {
            prepareCallback = callback
            senderCallback = null
        } else {
            prepareCallback = null
            senderCallback = callback as SenderCallback
        }
    }

    interface CommonCallback {
        fun onConnected()

        fun onDisconnected(byServer: Boolean)

        fun onError(errorCode: Int)
    }

    interface PrepareCallback : CommonCallback {
        fun onReceiverFound(found: Boolean)
    }

    interface SenderCallback : CommonCallback {
        fun onUploadStart()

        fun onProgress(progress: Int)

        fun onUploadFinish()
    }

    companion object {


        private val TAG = WebSocketUploadClient::class.java.simpleName
        val instance = WebSocketUploadClient()
    }
}
