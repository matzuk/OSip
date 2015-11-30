package com.tg.osip.ui.activities;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.tg.osip.R;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.ui.media.DepthPageTransformer;
import com.tg.osip.ui.media.PhotoSlidePagerAdapter;
import com.tg.osip.ui.media.ZoomOutPageTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity of Photo slide show
 *
 * @author e.matsyuk
 */
public class PhotoMediaActivity extends AppCompatActivity {

    public static final String PHOTO_Y = "photoY";

    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photo_media);
        initAdapter();
        // Instantiate a ViewPager and a PagerAdapter.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(true, new DepthPageTransformer());
    }

    private void initAdapter() {
        List<PhotoItem> photoYItemList = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            photoYItemList = (List<PhotoItem>)extras.getSerializable(PHOTO_Y);
        }
        pagerAdapter = new PhotoSlidePagerAdapter(getSupportFragmentManager(), photoYItemList);
    }

}
