package com.tg.osip.ui.views.images;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.business.update_managers.UpdateManager;
import com.tg.osip.business.main.MainListItem;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Custom ImageView with circle bounds, letter settings with background (if there is no photo) and photo downloading from ChatInfo
 *
 * @author e.matsyuk
 */
public class SIPAvatar extends ImageView {

    private final static String ADD_TO_PATH = "file://";

    private int fileId;
    private Subscription updateChannelSubscription;

    public SIPAvatar(Context context) {
        super(context);
    }

    public SIPAvatar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SIPAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMainListItem(MainListItem mainListItem) {
        startImageLoading(mainListItem);
    }

    private void startImageLoading(MainListItem mainListItem) {
        unSubscribe();
        if (!mainListItem.isSmallPhotoFileIdValid()) {
            setImageDrawable(mainListItem.getPlug());
            return;
        }
        if (mainListItem.isSmallPhotoFilePathValid()) {
            setFileToView(mainListItem.getSmallPhotoFilePath());
            return;
        }
        // test file downloaded cache
        if (FileDownloaderManager.getInstance().isFileInCache(mainListItem.getSmallPhotoFileId())) {
            setFileToView(mainListItem.getSmallPhotoFilePath());
            return;
        }
        // start update manager listening
        fileId = mainListItem.getSmallPhotoFileId();
        subscribeToUpdateChannel();
    }

    private void setFileToView(String fileToPath) {
        Picasso.with(
                ApplicationSIP.applicationContext).
                load(fileToPath).
                placeholder(getDrawable()).
                transform(new CirclePicassoTransformation()).
                into(this
                );
    }

    private void subscribeToUpdateChannel() {
        updateChannelSubscription = UpdateManager.getInstance().getUpdateChannel()
                .filter(update -> update.getClass() == TdApi.UpdateFile.class && ((TdApi.UpdateFile)update).file.id == fileId)
                .map(update -> (TdApi.UpdateFile)update)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TdApi.UpdateFile>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.UpdateFile update) {
                        unsubscribe();
                        setFileToView(ADD_TO_PATH + update.file.path);
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unSubscribe();
    }

    private void unSubscribe() {
        if (updateChannelSubscription != null && !updateChannelSubscription.isUnsubscribed()) {
            updateChannelSubscription.unsubscribe();
        }
    }

}
