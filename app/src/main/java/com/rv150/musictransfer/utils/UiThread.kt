package com.rv150.musictransfer.utils

import android.os.Handler
import android.os.Looper

object UiThread {
    private val HANDLER = Handler(Looper.getMainLooper())

    fun run(runnable: Runnable) {
        HANDLER.post(runnable)
    }
}