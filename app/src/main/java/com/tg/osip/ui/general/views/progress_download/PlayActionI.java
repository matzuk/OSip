package com.tg.osip.ui.general.views.progress_download;

import rx.subjects.PublishSubject;

/**
 * @author e.matsyuk
 */
public interface PlayActionI {
    void play();
    void pause();
    String getPath();
    int getId();
    PublishSubject<Integer> getPlayChannel();
}
