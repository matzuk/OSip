package com.tg.osip.ui.activities;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.tg.osip.R;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.ui.media.PhotoSlidePagerAdapter;
import com.tg.osip.ui.media.ZoomOutPageTransformer;
import com.tg.osip.utils.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity of Photo slide show
 *
 * @author e.matsyuk
 */
public class PhotoMediaActivity extends AppCompatActivity {

    public static final String PHOTO_M = "photoM";
    public static final String PHOTO_Y = "photoY";

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private List<PhotoItem> photoMItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photo_media);
        if (savedInstanceState != null) {
            savePhotoFromExtras();
        }
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PhotoSlidePagerAdapter(getSupportFragmentManager(), photoMItemList);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    private void savePhotoFromExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            photoMItemList = (List<PhotoItem>)extras.getSerializable(PHOTO_M);
        }
    }

}
