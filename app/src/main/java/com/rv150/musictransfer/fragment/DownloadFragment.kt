package com.rv150.musictransfer.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
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

class DownloadFragment : BoundFragment(), WebSocketDownloadClient.ReceiverCallback {
    val progressBar by bindView<ProgressBar>(R.id.progress_bar)
    val status by bindView<TextView>(R.id.status)
    private var webSocketClient: WebSocketDownloadClient = WebSocketDownloadClient.instance

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater?.inflate(R.layout.receiving_fragment, container, false)
        webSocketClient.setCallback(this)
        return root
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
        webSocketClient.setCallback(null)
        super.onDestroyView()
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
}