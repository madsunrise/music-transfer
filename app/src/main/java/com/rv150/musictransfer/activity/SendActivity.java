package com.rv150.musictransfer.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.rv150.musictransfer.R;
import com.rv150.musictransfer.fragment.PrepareSendingFragment;
import com.rv150.musictransfer.fragment.SendingFragment;

public class SendActivity extends AppCompatActivity implements PrepareSendingFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        if (savedInstanceState == null) {
            PrepareSendingFragment prepareSendingFragment = new PrepareSendingFragment();
            prepareSendingFragment.setArguments(getIntent().getExtras());
            changeFragment(prepareSendingFragment, false);
        }
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

    @Override
    public void onSendingStarted() {
        changeFragment(new SendingFragment(), false);
    }

}
