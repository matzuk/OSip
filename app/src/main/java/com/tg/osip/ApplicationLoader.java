package com.tg.osip;

import android.app.Application;
import android.content.Context;

/**
 * @author e.matsyuk
 */
public class ApplicationLoader extends Application {

    // not correct member init, but any call will after ApplicationLoader init
    public static volatile Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
    }

}
