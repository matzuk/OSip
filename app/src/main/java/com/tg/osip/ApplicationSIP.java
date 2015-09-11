package com.tg.osip;

import android.app.Application;
import android.content.Context;

import com.tg.osip.utils.log.Logger;

/**
 * @author e.matsyuk
 */
public class ApplicationSIP extends Application {

    // not correct member init, but any call will after ApplicationSIP init
    public static volatile Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Logger.registerLogger(this);
    }

}
