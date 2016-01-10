package com.tg.osip.ui.general.views.progress_download.play_actions;

import com.tg.osip.ui.general.views.progress_download.ProgressDownloadView;

/**
 * factory of actions for different Types of ProgressDownloadView
 *
 * @author e.matsyuk
 */
public class PlayActionsFactory {

    public static IPlayAction getPlayAction(ProgressDownloadView.Type type, String filePath, int fileId) {
        switch (type) {
            case AUDIO:
                return new AudioPlayAction(filePath, fileId);
            default:
                return null;
        }
    }

}
