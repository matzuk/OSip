package com.tg.osip.ui.general.views.progress_download.view_resources;

import com.tg.osip.ui.general.views.progress_download.ProgressDownloadView;

/**
 * @author e.matsyuk
 */
public class ViewResourcesFactory {

    public static IViewResources getViewResources(ProgressDownloadView.Type type) {
        switch (type) {
            case AUDIO:
                return new AudioViewResources();
            case VIDEO:
                return new VideoViewResources();
            default:
                return null;
        }
    }

}
