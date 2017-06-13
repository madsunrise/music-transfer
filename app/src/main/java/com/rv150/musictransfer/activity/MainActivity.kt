package com.rv150.musictransfer.activity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.rv150.musictransfer.R
import com.rv150.musictransfer.fragment.MusicListFragment
import com.rv150.musictransfer.fragment.SettingsFragment
import com.rv150.musictransfer.fragment.UploadPrepareFragment
import com.rv150.musictransfer.utils.Config
import kotterknife.bindView

class MainActivity : AppCompatActivity(), UploadPrepareFragment.Callback {

    val navigation by bindView<BottomNavigationView>(R.id.navigation)

    private var currentButtonId: Int? = null
    val rel = mapOf<Int, () -> Fragment>(
            R.id.music_list to ::MusicListFragment,
            R.id.upload_prepare to ::UploadPrepareFragment,
            R.id.settings to ::SettingsFragment
    )

    private val mOnNavigationItemSelectedListener = { item: MenuItem ->
        when {
            item.itemId in rel && item.itemId != currentButtonId -> {
                changeFragment(item.itemId)
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        currentButtonId = savedInstanceState?.getInt(ID_KEY) ?: changeFragment(R.id.music_list)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (currentButtonId != null) {
            outState?.putInt(ID_KEY, currentButtonId!!)
        }
    }

    private fun changeFragment(id: Int): Int {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, rel[id]!!(), Config.MAIN_ACTIVITY_FRAGMENT_TAG)
                .commitAllowingStateLoss()
        currentButtonId = id
        return id
    }

    override fun onReceivingStarted() {
        changeFragment(R.id.upload_prepare)
    }

    companion object {
        val ID_KEY = "id_key"
    }
}
