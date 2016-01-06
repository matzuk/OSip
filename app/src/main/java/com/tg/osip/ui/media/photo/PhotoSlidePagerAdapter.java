package com.tg.osip.ui.media.photo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tg.osip.business.models.PhotoItem;

import java.util.List;

/**
 * @author e.matsyuk
 */
public class PhotoSlidePagerAdapter extends FragmentStatePagerAdapter {

    private List<PhotoItem> photoItemList;

    public PhotoSlidePagerAdapter(FragmentManager fm, List<PhotoItem> photoItemList) {
        super(fm);
        this.photoItemList = photoItemList;
    }

    @Override
    public Fragment getItem(int position) {
        return PhotoMediaFragment.newInstance(photoItemList.get(position));
    }

    @Override
    public int getCount() {
        return photoItemList.size();
    }

}