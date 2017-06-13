package com.rv150.musictransfer.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.rv150.musictransfer.R
import com.rv150.musictransfer.fragment.DownloadPrepareFragment
import com.rv150.musictransfer.fragment.UploadFragment

class SendActivity : AppCompatActivity(), DownloadPrepareFragment.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sending)
        if (savedInstanceState == null) {
            val prepareSendingFragment = DownloadPrepareFragment()
            prepareSendingFragment.arguments = intent.extras
            changeFragment(prepareSendingFragment, false)
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    @SuppressLint("CommitTransaction")
    private fun changeFragment(fragment: Fragment, addToBackStack: Boolean): Unit =
            with(supportFragmentManager.beginTransaction()) {
                replace(R.id.container, fragment)
                if (addToBackStack) {
                    addToBackStack(null)
                }
                commitAllowingStateLoss()
            }


    override fun onSendingStarted() = changeFragment(UploadFragment(), false)
}
