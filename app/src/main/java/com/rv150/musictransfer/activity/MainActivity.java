package com.rv150.musictransfer.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.fragment.MusicListFragment;
import com.rv150.musictransfer.fragment.PrepareReceivingFragment;
import com.rv150.musictransfer.fragment.ReceivingFragment;
import com.rv150.musictransfer.utils.Config;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PrepareReceivingFragment.Callback {

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    private Fragment currentFragment;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                changeFragment(new MusicListFragment(), false);
                return true;
            case R.id.navigation_dashboard:
                if (!(currentFragment instanceof PrepareReceivingFragment)) {
                    changeFragment(new PrepareReceivingFragment(), false);
                }
                return true;
            case R.id.navigation_notifications:
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState != null) {
            currentFragment = getSupportFragmentManager()
                    .getFragment(savedInstanceState, Config.MAIN_ACTIVITY_FRAGMENT_TAG);
        } else {
            changeFragment(new MusicListFragment(), false);
        }
    }

    private void changeFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment, Config.MAIN_ACTIVITY_FRAGMENT_TAG);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();
        currentFragment = fragment;
    }

    @Override
    public void onReceivingStarted() {
        changeFragment(new ReceivingFragment(), false);
    }
}
