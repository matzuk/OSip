package com.tg.osip.tdclient;

import android.util.Log;

import com.tg.osip.utils.AndroidUtils;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Proxy-singleton for {@link org.drinkless.td.libcore.telegram.TG TG}
 *
 * @author e.matsyuk
 */
public class TGProxy {

    private static volatile TGProxy instance;

    /**
     * this method is necessary, because setDir and setUpdatesHandler for TG in first calling
     * @return TG.getClientInstance()
     */
    public static TGProxy getInstance() {
        if (instance == null) {
            synchronized (TGProxy.class) {
                if (instance == null) {
                    instance = new TGProxy();
                }
            }
        }
        return instance;
    }

    private TGProxy() {
        TG.setDir(AndroidUtils.getCacheDirPath());
        TG.setUpdatesHandler(updatesHandler);
    }

    public Client getClientInstance() {
        return TG.getClientInstance();
    }

    // temp func
    private Client.ResultHandler updatesHandler = new Client.ResultHandler() {
        @Override
        public void onResult(TdApi.TLObject object) {

        }
    };

}
