package com.tg.osip;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.launcher_and_registration.SplashFragment;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create new fragment and transaction
        SplashFragment newFragment = new SplashFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
