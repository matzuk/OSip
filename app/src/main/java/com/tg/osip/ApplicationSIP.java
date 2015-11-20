package com.tg.osip;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.tg.osip.utils.log.Logger;

import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

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

        if (isDebugMode()) {
            // Determining unchecked exceptions in Subscribers
            RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
                @Override
                public void handleError(Throwable e) {
                    Log.w("Error", e);
                }
            });
        }
    }

    public boolean isDebugMode() {
        return (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

}
