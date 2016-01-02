package com.tg.osip;

import android.support.annotation.NonNull;

import com.tg.osip.tdclient.TGModule;
import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.tdclient.TGProxyImpl;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.update_managers.UpdateManager;

import static org.mockito.Mockito.mock;

/**
 * @author e.matsyuk
 */
public class ApplicationSIPTest extends ApplicationSIP {

    @NonNull
    @Override
    protected DaggerAppComponent.Builder prepareApplicationComponent() {
        return super.prepareApplicationComponent()
                .tGModule(new TGModule() {
                    @NonNull
                    @Override
                    public TGProxyI provideTGProxy(UpdateManager updateManager) {
                        return mock(TGProxyImpl.class);
                    }
                    @NonNull
                    @Override
                    public UpdateManager provideUpdateManager() {
                        return mock(UpdateManager.class);
                    }
                    @NonNull
                    @Override
                    public FileDownloaderManager provideFileDownloaderManager(TGProxyI tgProxyI) {
                        return mock(FileDownloaderManager.class);
                    }
                });
    }

}
