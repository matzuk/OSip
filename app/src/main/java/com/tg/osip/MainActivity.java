package com.tg.osip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.utils.AndroidUtils;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TGProxy.getInstance().getClientInstance().send(new TdApi.GetAuthState(), authGetResultHandler);

    }

    private Client.ResultHandler authGetResultHandler = new Client.ResultHandler() {
        @Override
        public void onResult(TdApi.TLObject object) {
            Logger.debug("auth:", object.toString());
        }
    };
}
