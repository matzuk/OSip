package com.tg.osip.ui.general.views.progress_download.play_actions;

import rx.subjects.PublishSubject;

/**
 * @author e.matsyuk
 */
public interface IPlayAction {
    void play();
    void pause();
    String getPath();
    int getId();
    PublishSubject<Integer> getPlayChannel();
}
