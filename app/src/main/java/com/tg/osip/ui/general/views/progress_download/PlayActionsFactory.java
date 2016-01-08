package com.tg.osip.ui.general.views.progress_download;

import com.tg.osip.tdclient.update_managers.FileDownloaderI;

/**
 * factory of actions for different Types of ProgressDownloadView
 *
 * @author e.matsyuk
 */
public class PlayActionsFactory {

    public static PlayActionI getPlayAction(ProgressDownloadView.Type type, String filePath, int fileId) {
        switch (type) {
            case AUDIO:
                return new PlayActionAudio(filePath, fileId);
            default:
                return null;
        }
    }

}
