package com.tg.osip.ui.views.images;

import android.graphics.drawable.Drawable;

/**
 * Interface for download setting in {@link SIPAvatar SIPAvatar}
 *
 * @author e.matsyuk
 */
public interface ImageLoaderI {

    boolean isSmallPhotoFileIdValid();
    Drawable getPlug();
    boolean isSmallPhotoFilePathValid();
    String getSmallPhotoFilePath();
    int getSmallPhotoFileId();

}
