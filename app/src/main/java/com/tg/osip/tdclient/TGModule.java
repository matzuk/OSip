package com.tg.osip.tdclient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author e.matsyuk
 */
@Module
public class TGModule {

    @Provides
    @Singleton
    public TGProxyI provideTGProxy() {
        return new TGProxyImpl();
    }

}
