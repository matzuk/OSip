package com.tg.osip.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.tg.osip.R;
import com.tg.osip.ui.main.MainFragment;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_login);
        startFragment(new MainFragment(), false);
    }

    private void startFragment(Fragment fragment, boolean withBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (withBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

}
