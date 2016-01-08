package com.tg.osip;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tg.osip.business.media.MediaManager;
import com.tg.osip.business.PersistentInfo;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module for all application
 * @author e.matsyuk
 */
@Module
public class AppModule {

    private Context appContext;

    public AppModule(@NonNull Context context) {
        appContext = context;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return appContext;
    }

    @Provides
    @Singleton
    public PersistentInfo providePersistentInfo() {
        return new PersistentInfo();
    }

}
