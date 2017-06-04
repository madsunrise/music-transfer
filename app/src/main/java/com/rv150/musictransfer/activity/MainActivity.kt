package com.rv150.musictransfer.activity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.rv150.musictransfer.R
import com.rv150.musictransfer.fragment.DownloadFragment
import com.rv150.musictransfer.fragment.MusicListFragment
import com.rv150.musictransfer.fragment.UploadPrepareFragment
import com.rv150.musictransfer.utils.Config
import kotterknife.bindView

class MainActivity : AppCompatActivity(), UploadPrepareFragment.Callback {

    val navigation by bindView<BottomNavigationView>(R.id.navigation)

    private var currentFragment: Fragment? = null
    private val mOnNavigationItemSelectedListener = { item: MenuItem ->
        when (item.itemId) {
            R.id.navigation_home -> {
                changeFragment(MusicListFragment(), false)
                true
            }
            R.id.navigation_dashboard -> {
                if (currentFragment !is UploadPrepareFragment) {
                    changeFragment(UploadPrepareFragment(), false)
                }
                true
            }
            R.id.navigation_notifications -> true
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (savedInstanceState != null) {
            currentFragment = supportFragmentManager
                    .getFragment(savedInstanceState, Config.MAIN_ACTIVITY_FRAGMENT_TAG)
        } else {
            changeFragment(MusicListFragment(), false)
        }
    }

    private fun changeFragment(fragment: Fragment, addToBackStack: Boolean) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, Config.MAIN_ACTIVITY_FRAGMENT_TAG)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commitAllowingStateLoss()
        currentFragment = fragment
    }

    override fun onReceivingStarted() {
        changeFragment(DownloadFragment(), false)
    }
}
