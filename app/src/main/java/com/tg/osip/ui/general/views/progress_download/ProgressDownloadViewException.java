package com.tg.osip.ui.general.views.progress_download;

/**
 * @author e.matsyuk
 */
public class ProgressDownloadViewException extends RuntimeException {

    public ProgressDownloadViewException() {
        super("incorrect type");
    }

    public ProgressDownloadViewException(String message) {
        super(message);
    }

}
