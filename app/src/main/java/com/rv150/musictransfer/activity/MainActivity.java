package com.rv150.musictransfer.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.fragment.MusicListFragment;
import com.rv150.musictransfer.network.WebSocketClient;

public class MainActivity extends AppCompatActivity {

    private final MusicListFragment musicListFragment = new MusicListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        changeFragment(musicListFragment, false);
    }


    private void changeFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                return true;
            case R.id.navigation_dashboard:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        WebSocketClient.getInstance(getApplicationContext());
                    }
                }).start();
                return true;
            case R.id.navigation_notifications:
                return true;
        }
        return false;
    };
}
