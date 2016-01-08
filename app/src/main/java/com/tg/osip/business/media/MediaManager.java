package com.tg.osip.business.media;

import android.media.MediaPlayer;

import com.tg.osip.utils.CommonStaticFields;
import com.tg.osip.utils.log.Logger;

import java.io.IOException;

/**
 * @author e.matsyuk
 */
public class MediaManager {

    private MediaPlayer mediaPlayer;
    private int currentIdFile;
    private boolean paused;

    public MediaManager(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void play(String path, int id) {
        if (id == currentIdFile) {
            mediaPlayer.start();
            paused = false;
            return;
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (Exception e) {
            Logger.debug(e);
            paused = false;
            return;
        }
        currentIdFile = id;
        mediaPlayer.start();
        paused = false;
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            paused = true;
        }
    }

    public void reset() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        paused = false;
        currentIdFile = CommonStaticFields.EMPTY_FILE_ID;
    }

    public int getCurrentIdFile() {
        return currentIdFile;
    }

    public boolean isPaused() {
        return paused;
    }

}
