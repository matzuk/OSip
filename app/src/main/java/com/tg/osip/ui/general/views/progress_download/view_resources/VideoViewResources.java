package com.tg.osip.ui.general.views.progress_download.view_resources;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

import com.tg.osip.R;

/**
 * @author e.matsyuk
 */
public class VideoViewResources implements IViewResources {

    @Override
    public Drawable getProgressDrawable(Resources resources, Context context) {
        return ResourcesCompat.getDrawable(resources, R.drawable.progress_video_circular_outer, context.getTheme());
    }

    @Override
    public int getDownloadImage() {
        return R.drawable.progress_video_download_icon;
    }

    @Override
    public int getDownloadInner() {
        return R.drawable.progress_video_download_inner;
    }
}