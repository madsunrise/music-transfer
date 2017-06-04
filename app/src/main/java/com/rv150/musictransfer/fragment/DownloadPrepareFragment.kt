package com.rv150.musictransfer.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.neovisionaries.ws.client.WebSocketException
import com.rv150.musictransfer.R
import com.rv150.musictransfer.model.Song
import com.rv150.musictransfer.network.WebSocketUploadClient
import com.rv150.musictransfer.utils.UiThread
import kotterknife.bindView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

class DownloadPrepareFragment : Fragment(), WebSocketUploadClient.PrepareCallback, ZXingScannerView.ResultHandler {

    val info by bindView<TextView>(R.id.sending_info)
    val receiverCode by bindView<EditText>(R.id.receiver_id)
    val fl by bindView<FrameLayout>(R.id.fl)
    val sendBtn by bindView<Button>(R.id.send)
    val progressBar by bindView<ProgressBar>(R.id.progress_bar)
    private var isCameraEnabled = false
    private var song: Song? = null
    private val networkExecutor = Executors.newSingleThreadExecutor()
    private var webSocketUploadClient: WebSocketUploadClient? = null
    private var activity: Callback? = null
    private var mScannerView: ZXingScannerView? = null

    init {
        try {
            webSocketUploadClient = WebSocketUploadClient.instance
        } catch (ex: IOException) {
            Log.e(TAG, "Failed to create instance of webSocketUploadClient! " + ex.message)
        }

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as Callback?
    }

    private fun checkAccess(): Boolean {
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAccess() {
        when {
            !checkAccess() -> requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initQR()
            } else {
                Toast.makeText(getActivity(), R.string.need_permission, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initQR() {
        mScannerView = ZXingScannerView(getActivity())
        val formats = ArrayList<BarcodeFormat>()
        formats.add(BarcodeFormat.QR_CODE)
        mScannerView!!.setFormats(formats)
        fl.addView(mScannerView)
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera()
        isCameraEnabled = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        return inflater?.inflate(R.layout.prepare_sending_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated")
        val bundle = arguments
        song = Song(bundle.getString("title"), bundle.getString("path"), bundle.getLong("size"))
        if (song != null) {
            info.text = String.format(getString(R.string.sending_songname), song!!.title)
        }
        webSocketUploadClient?.setCallback(this)
        setOnTextChangedListener()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (song != null && outState != null) {
            outState.putString("title", song!!.title)
            outState.putString("path", song!!.path)
            outState.putLong("size", song!!.size)
        }
    }


    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        if (checkAccess()) {
            initQR()
        } else {
            requestAccess()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isCameraEnabled) {
            mScannerView!!.stopCamera()
        }
    }

    override fun handleResult(rawResult: Result) {
        if (!isCameraEnabled) {
            return
        }
        val text = rawResult.text
        if (text.length == 4 && text.matches("\\d\\d\\d\\d".toRegex())) {
            if (text != receiverCode.text.toString()) {
                receiverCode.setText(text)
                send()
            } else {
                mScannerView!!.resumeCameraPreview(this)
            }
        } else {
            mScannerView!!.resumeCameraPreview(this)
            Toast.makeText(getActivity(), "QR code format exception", Toast.LENGTH_SHORT).show()
        }
    }

    //@OnClick(R.id.send)
    fun send() {
        val code = receiverCode.text.toString()
        if (!webSocketUploadClient!!.isConnected) {
            connectWebSocket()
        }
        setUiEnabled(false)
        networkExecutor.execute { webSocketUploadClient!!.registerSongForTransferring(song!!, code) }
    }

    private fun connectWebSocket() {
        networkExecutor.execute {
            try {
                webSocketUploadClient!!.connect()
            } catch (ex: WebSocketException) {
                UiThread.run {
                    Log.e(TAG, "Failed to connect to websocket! " + ex.message)
                    Toast.makeText(context,
                            R.string.connection_error, Toast.LENGTH_SHORT).show()
                }
            } catch (ex: IOException) {
                UiThread.run {
                    Log.e(TAG, "Failed to connect to websocket! " + ex.message)
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onConnected() {

    }

    override fun onDisconnected(byServer: Boolean) {
        //TODO if network disappears
    }

    override fun onReceiverFound(found: Boolean) {
        if (found) {
            activity!!.onSendingStarted()
            return
        }
        Toast.makeText(context, R.string.receiver_with_this_id_not_found, Toast.LENGTH_SHORT).show()
        setUiEnabled(true)
        if (isCameraEnabled) {
            mScannerView!!.resumeCameraPreview(this)
        }
    }

    override fun onError(errorCode: Int) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocketUploadClient!!.setCallback(null)
    }

    private fun setUiEnabled(enabled: Boolean) {
        if (enabled) {
            sendBtn.isEnabled = true
            progressBar.visibility = View.INVISIBLE
        } else {
            sendBtn.isEnabled = false
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun setOnTextChangedListener() {
        receiverCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                sendBtn.isEnabled = s.length == resources.getInteger(R.integer.id_length)
            }
        })
    }

    interface Callback {
        fun onSendingStarted()  // Сменить фрагмент
    }

    companion object {

        private val REQUEST_CODE = 1232
        private val TAG = DownloadPrepareFragment::class.java.simpleName
    }
}
