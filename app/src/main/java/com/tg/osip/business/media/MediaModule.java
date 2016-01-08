package com.tg.osip.business.media;

import android.media.MediaPlayer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module for media
 * @author e.matsyuk
 */
@Module
public class MediaModule {

    @Provides
    @Singleton
    public MediaPlayer provideMediaPlayer() {
        return new MediaPlayer();
    }

    @Provides
    @Singleton
    public MediaManager provideMediaManager(MediaPlayer mediaPlayer) {
        return new MediaManager(mediaPlayer);
    }

}


