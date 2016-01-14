package com.tg.osip.utils.dagger2_static;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author e.matsyuk
 */
@Module
public class StaticModule {

    @Provides
    @Singleton
    public BytesFormatter provideBytesFormatter() {
        return new BytesFormatter();
    }

}
