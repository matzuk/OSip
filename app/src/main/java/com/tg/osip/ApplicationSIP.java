package com.tg.osip;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.tg.osip.tdclient.TGModule;
import com.tg.osip.utils.log.Logger;

import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

/**
 * @author e.matsyuk
 */
public class ApplicationSIP extends Application {

    // not correct member init, but any call will after ApplicationSIP init
    public static volatile Context applicationContext;

    @NonNull
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Logger.registerLogger(this);

        appComponent = prepareApplicationComponent().build();
        appComponent.inject(this);

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

    @NonNull
    protected DaggerAppComponent.Builder prepareApplicationComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(getApplicationContext()))
                .tGModule(new TGModule());
    }

    public boolean isDebugMode() {
        return (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    @NonNull
    public AppComponent applicationComponent() {
        return appComponent;
    }

    @NonNull
    public static ApplicationSIP get() {
        return (ApplicationSIP) applicationContext.getApplicationContext();
    }

}
