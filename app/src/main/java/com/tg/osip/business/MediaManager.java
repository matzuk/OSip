package com.tg.osip.business;

import android.media.MediaPlayer;

import com.tg.osip.utils.log.Logger;

import java.io.IOException;

/**
 * @author e.matsyuk
 */
public class MediaManager {

    private MediaPlayer mediaPlayer;

    public MediaManager() {
        mediaPlayer = new MediaPlayer();
    }

    public void play(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Logger.debug(e);
            return;
        }
        mediaPlayer.start();
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

}
