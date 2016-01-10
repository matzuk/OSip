package com.tg.osip.ui.general.views.progress_download.view_resources;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

/**
 * @author e.matsyuk
 */
public interface IViewResources {
    Drawable getProgressDrawable(Resources resources, Context context);
    @DrawableRes
    int getDownloadImage();
    @DrawableRes
    int getDownloadInner();
}
