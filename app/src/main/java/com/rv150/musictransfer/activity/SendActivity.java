package com.rv150.musictransfer.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.fragment.PrepareSendingFragment;

/**
 * Created by ivan on 10.05.17.
 */

public class SendActivity extends AppCompatActivity {

    private PrepareSendingFragment prepareSendingFragment = new PrepareSendingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        prepareSendingFragment.setArguments(getIntent().getExtras());
        changeFragment(prepareSendingFragment, false);
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
}
