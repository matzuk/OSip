package com.tg.osip.business.media;

import android.media.MediaPlayer;

import com.tg.osip.utils.CommonStaticFields;
import com.tg.osip.utils.log.Logger;

import java.io.IOException;

import rx.subjects.PublishSubject;

/**
 * @author e.matsyuk
 */
public class MediaManager {

    private MediaPlayer mediaPlayer;
    private int currentIdFile = CommonStaticFields.EMPTY_FILE_ID;
    private boolean paused;

    PublishSubject<Integer> playChannel = PublishSubject.create();

    public MediaManager(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void play(String path, int id) {
        Logger.debug("play");
        Logger.debug("currentIdFile: " + currentIdFile);
        Logger.debug("path: " + path + ", id: " + id);
        if (id == currentIdFile) {
            mediaPlayer.start();
            paused = false;
            return;
        }
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Logger.debug(e);
            paused = false;
            return;
        }
        currentIdFile = id;
        playChannel.onNext(currentIdFile);
        mediaPlayer.start();
        paused = false;
    }

    public void pause() {
        Logger.debug("pause");
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
        playChannel.onNext(currentIdFile);
    }

    public int getCurrentIdFile() {
        return currentIdFile;
    }

    public boolean isPaused() {
        return paused;
    }

    public PublishSubject<Integer> getPlayChannel() {
        return playChannel;
    }
}
