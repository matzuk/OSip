package com.tg.osip.tdclient;

import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.update_managers.UpdateManager;

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
    public UpdateManager provideUpdateManager() {
        return new UpdateManager();
    }

    @Provides
    @Singleton
    public TGProxyI provideTGProxy(UpdateManager updateManager) {
        return new TGProxyImpl(updateManager);
    }

    @Provides
    @Singleton
    public FileDownloaderManager provideFileDownloaderManager(TGProxyI tgProxyI) {
        return new FileDownloaderManager(tgProxyI);
    }

}
