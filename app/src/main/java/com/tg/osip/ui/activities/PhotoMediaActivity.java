package com.tg.osip.ui.activities;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.media.DepthPageTransformer;
import com.tg.osip.ui.media.PhotoSlidePagerAdapter;

import java.util.List;

import javax.inject.Inject;

/**
 * Activity of Photo slide show
 *
 * @author e.matsyuk
 */
public class PhotoMediaActivity extends AppCompatActivity {

    public static final String PHOTO_LARGE = "photoLarge";
    public static final String CLICKED_POSITION = "clickedPosition";

    private static final int PAGER_OFFSET = 2;

    @Inject
    FileDownloaderManager fileDownloaderManager;
    private List<PhotoItem> photoLargeItemList;
    private int clickedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photo_media);
        ApplicationSIP.get().applicationComponent().inject(this);
        getExtras();
        downloadPhotoY();
        initPager();
        setToolbar();
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clickedPosition = extras.getInt(CLICKED_POSITION);
            photoLargeItemList = (List<PhotoItem>)extras.getSerializable(PHOTO_LARGE);
        }
    }

    private void downloadPhotoY() {
        fileDownloaderManager.startFileListDownloading(photoLargeItemList);
    }

    private void initPager() {
        PagerAdapter pagerAdapter = new PhotoSlidePagerAdapter(getSupportFragmentManager(), photoLargeItemList);
        // Instantiate a ViewPager and a PagerAdapter.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(PAGER_OFFSET);
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(true, new DepthPageTransformer());
        pager.setCurrentItem(clickedPosition);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                String title = String.format(getResources().getString(R.string.toolbar_title_in_photo_gallery), position + 1, photoLargeItemList.size());
                getSupportActionBar().setTitle(title);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() == null) {
            return;
        }
        String title = String.format(getResources().getString(R.string.toolbar_title_in_photo_gallery), clickedPosition + 1, photoLargeItemList.size());
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportActionBar().show();
    }

}
