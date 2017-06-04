package com.rv150.musictransfer.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.neovisionaries.ws.client.WebSocketException
import com.rv150.musictransfer.R
import com.rv150.musictransfer.network.WebSocketDownloadClient
import com.rv150.musictransfer.utils.Config
import com.rv150.musictransfer.utils.UiThread
import com.rv150.musictransfer.utils.Utils
import kotterknife.bindView
import java.io.IOException
import java.util.concurrent.Executors

class UploadPrepareFragment : Fragment(), WebSocketDownloadClient.PrepareCallback {
    private val networkExecutor = Executors.newSingleThreadExecutor()
    val yourId by bindView<TextView>(R.id.your_id)
    val progressBar by bindView<ProgressBar>(R.id.progress_bar)
    val imageView by bindView<ImageView>(R.id.qr)
    val retryBtn by bindView<Button>(R.id.retry)
    val noConnection by bindView<LinearLayout>(R.id.no_connection)
    val connection by bindView<ViewGroup>(R.id.connection)
    private var id: Long? = null
    private var webSocketClient: WebSocketDownloadClient? = null
    private var activity: Callback? = null

    init {
        webSocketClient = WebSocketDownloadClient.instance
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as Callback?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.prepare_receiving_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        webSocketClient!!.setCallback(this)
        if (savedInstanceState != null && savedInstanceState.containsKey(Config.ID_KEY)) {
            onConnect()
            onIdRegister(savedInstanceState.getLong(Config.ID_KEY))
        } else {
            connectToWebsocket()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putLong(Config.ID_KEY, id!!)
    }

    override fun onIdRegister(id: Long) {
        this.id = id
        connection.visibility = View.VISIBLE
        yourId.text = String.format(getString(R.string.your_id_is), id)
        val bitmap = Utils.encodeAsBitmap(java.lang.Long.toString(id), resources.getDimension(R.dimen.qr).toInt())
        imageView.setImageBitmap(bitmap)
    }

    override fun onFileUploadRequest(fileName: String) {
        AlertDialog.Builder(context)
                .setMessage(String.format(getString(R.string.do_you_want_to_accept_new_file), fileName))
                .setPositiveButton(R.string.yes) { d, w -> sendAnswerOnRequest(true) }
                .setNegativeButton(R.string.no) { d, w -> sendAnswerOnRequest(false) }
                .show()
    }

    private fun sendAnswerOnRequest(accept: Boolean) {
        networkExecutor.execute { webSocketClient!!.sendAnswerOnRequest(accept) }
        if (accept) {
            activity!!.onReceivingStarted()
        }
    }

    override fun onConnect() {
        progressBar.visibility = View.GONE
        connection.visibility = View.VISIBLE
        noConnection.visibility = View.GONE
    }

    override fun onDisconnect(byServer: Boolean) {
        if (!byServer) {
            progressBar.visibility = View.GONE
            noConnection.visibility = View.GONE
            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(errorCode: Int) {

    }


    internal fun connectToWebsocket() {
        networkExecutor.execute {
            try {
                webSocketClient!!.connect()
            } catch (ex: WebSocketException) {
                UiThread.run {
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Failed to connect to websocket! " + ex.message)
                    noConnection.visibility = View.VISIBLE
                    Toast.makeText(context,
                            R.string.connection_error, Toast.LENGTH_SHORT).show()
                }
            } catch (ex: IOException) {
                UiThread.run {
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Failed to connect to websocket! " + ex.message)
                    noConnection.visibility = View.VISIBLE
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocketClient!!.setCallback(null)
    }

    interface Callback {
        fun onReceivingStarted()
    }

    companion object {

        private val TAG = UploadPrepareFragment::class.java.simpleName
    }
}
