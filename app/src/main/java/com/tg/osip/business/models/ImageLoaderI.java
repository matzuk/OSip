package com.tg.osip.business.models;

import android.graphics.drawable.Drawable;

import com.tg.osip.ui.general.views.images.PhotoView;

/**
 * Interface for download setting in {@link PhotoView SIPAvatar}
 *
 * @author e.matsyuk
 */
public interface ImageLoaderI {

    boolean isPhotoFileIdValid();
    Drawable getPlug();
    boolean isPhotoFilePathValid();
    String getPhotoFilePath();
    int getPhotoFileId();

}
