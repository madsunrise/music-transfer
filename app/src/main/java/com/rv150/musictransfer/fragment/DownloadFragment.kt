package com.rv150.musictransfer.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.rv150.musictransfer.R
import com.rv150.musictransfer.network.WebSocketDownloadClient
import com.rv150.musictransfer.network.WebSocketDownloadClient.Companion.FILE_CREATION_ERROR
import kotterknife.bindView
import java.io.IOException

class DownloadFragment : Fragment(), WebSocketDownloadClient.ReceiverCallback {
    val progressBar by bindView<ProgressBar>(R.id.progress_bar)
    val status by bindView<TextView>(R.id.status)
    private var webSocketClient: WebSocketDownloadClient? = null

    init {
        try {
            webSocketClient = WebSocketDownloadClient.instance
        } catch (ex: IOException) {
            Log.e(TAG, "Failed to create instance of webSocketClient! " + ex.message)
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.receiving_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        webSocketClient!!.setCallback(this)
    }

    override fun onConnect() {

    }

    override fun onDisconnect(byServer: Boolean) {

    }

    override fun onFileReceivingFinished() {
        status.setText(R.string.file_has_been_received)
        status.setTextColor(ContextCompat.getColor(context, R.color.green))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocketClient!!.setCallback(null)
    }

    override fun onError(errorCode: Int) {
        when (errorCode) {
            FILE_CREATION_ERROR -> {
                Toast.makeText(context, R.string.failed_to_create_file, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onProgress(percent: Int) {
        progressBar.progress = percent
    }

    companion object {
        private val TAG = DownloadFragment::class.java.simpleName
    }

}