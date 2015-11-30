package com.tg.osip.ui.general.views.images;

import android.graphics.drawable.Drawable;

import com.tg.osip.ui.general.views.images.PhotoView;

/**
 * Interface for download setting in {@link PhotoView SIPAvatar}
 *
 * @author e.matsyuk
 */
public interface ImageLoaderI {

    Drawable getPlug();
    String getPhotoFilePath();
    int getPhotoFileId();
    ImageLoaderI getPlugFile();
}
