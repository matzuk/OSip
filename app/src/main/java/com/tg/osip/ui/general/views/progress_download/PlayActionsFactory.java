package com.tg.osip.ui.general.views.progress_download;

import com.tg.osip.tdclient.update_managers.FileDownloaderI;

/**
 * factory of actions for different Types of ProgressDownloadView
 *
 * @author e.matsyuk
 */
public class PlayActionsFactory {

    public static PlayActionI getPlayAction(ProgressDownloadView.Type type, FileDownloaderI fileDownloaderI) {
        switch (type) {
            case AUDIO:
                return new PlayActionAudio(fileDownloaderI.getFilePath());
            default:
                return null;
        }
    }

}
