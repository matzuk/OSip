package com.tg.osip.ui.general.views.progress_download;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.business.MediaManager;

import javax.inject.Inject;

/**
 * @author e.matsyuk
 */
public class PlayActionAudio implements PlayActionI {

    @Inject
    MediaManager mediaManager;

    private String path;

    public PlayActionAudio(String path) {
        ApplicationSIP.get().applicationComponent().inject(this);
        this.path = path;
    }

    @Override
    public void play() {
        mediaManager.play(path);
    }

    @Override
    public void pause() {
        mediaManager.pause();
    }

}
