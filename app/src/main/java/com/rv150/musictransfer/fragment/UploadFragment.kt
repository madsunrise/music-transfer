package com.rv150.musictransfer.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.rv150.musictransfer.R
import com.rv150.musictransfer.network.WebSocketUploadClient
import kotterknife.bindView
import java.io.IOException

class UploadFragment : Fragment(), WebSocketUploadClient.SenderCallback {
    val progressBar by bindView<ProgressBar>(R.id.progress_bar)
    val status by bindView<TextView>(R.id.status)
    val menuButton by bindView<Button>(R.id.menu_button)
    private var webSocketUploadClient: WebSocketUploadClient? = null

    init {
        try {
            webSocketUploadClient = WebSocketUploadClient.instance
        } catch (ex: IOException) {
            Log.e(TAG, "Failed to create instance of webSocketUploadClient! " + ex.message)
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.upload_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        webSocketUploadClient?.setCallback(this)
    }

    override fun onConnected() {

    }

    override fun onDisconnected(byServer: Boolean) {

    }

    override fun onUploadStart() {
        status.setText(R.string.sending)
    }

    override fun onProgress(progress: Int) {
        progressBar.progress = progress
    }

    override fun onUploadFinish() {
        status.setText(R.string.sending_has_finished)
        status.setTextColor(ContextCompat.getColor(context, R.color.green))
        menuButton.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocketUploadClient?.setCallback(null)
        webSocketUploadClient?.disconnect()
    }

    override fun onError(errorCode: Int) {

    }

    //@OnClick(R.id.menu_button)
    internal fun backToMenu() {
        activity.finish()
    }

    companion object {

        private val TAG = UploadFragment::class.java.simpleName
    }
}

