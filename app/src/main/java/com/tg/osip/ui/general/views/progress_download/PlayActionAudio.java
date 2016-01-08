package com.tg.osip.ui.general.views.progress_download;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.business.media.MediaManager;

import javax.inject.Inject;

/**
 * @author e.matsyuk
 */
public class PlayActionAudio implements PlayActionI {

    @Inject
    MediaManager mediaManager;

    private String path;
    private int id;

    public PlayActionAudio(String path, int id) {
        ApplicationSIP.get().applicationComponent().inject(this);
        this.path = path;
        this.id = id;
    }

    @Override
    public void play() {
        mediaManager.play(path, id);
    }

    @Override
    public void pause() {
        mediaManager.pause();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public int getId() {
        return id;
    }
}
