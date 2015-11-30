package com.tg.osip.ui.media;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.R;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.ui.general.views.images.PhotoView;
import com.tg.osip.utils.common.AndroidUtils;

/**
 * Fragment of Photo media
 *
 * @author e.matsyuk
 */
public class PhotoMediaFragment extends Fragment {

    private static String PHOTO_ITEM = "photoItem";

    private PhotoItem photoItem;

    public static PhotoMediaFragment newInstance(PhotoItem photoItem) {
        PhotoMediaFragment f = new PhotoMediaFragment();
        Bundle args = new Bundle();
        args.putSerializable(PHOTO_ITEM, photoItem);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }
        photoItem = (PhotoItem)getArguments().getSerializable(PHOTO_ITEM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_photo_media, container, false);
        setRetainInstance(true);
        init(rootView);
        return rootView;
    }

    private void init(View rootView) {
        PhotoView photoView = (PhotoView) rootView.findViewById(R.id.photo);
        if (photoItem != null) {
            photoView.setImageLoaderI(photoItem);
            android.view.ViewGroup.LayoutParams layoutParams = photoView.getLayoutParams();
            layoutParams.width = AndroidUtils.dp(photoItem.getWidth());
            layoutParams.height = AndroidUtils.dp(photoItem.getHeight());
            photoView.setLayoutParams(layoutParams);
            photoView.setImageLoaderI(photoItem);
        }
    }

}
