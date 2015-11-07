package com.tg.osip.utils.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.squareup.picasso.Picasso;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.FileDownloaderManager;
import com.tg.osip.business.UpdateManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.tdclient.models.MainListItem;
import com.tg.osip.utils.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.CirclePicassoTransformation;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Observable;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
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
        if (!mainListItem.isSmallPhotoFilePathValid()) {
            fileId = mainListItem.getSmallPhotoFileId();
            setImageDrawable(mainListItem.getPlug());
//            setLetterDrawable(this, mainListItem);
//            setImageDrawable(null);
//            TextDrawable drawable = TextDrawable.builder()
//                    .buildRoundRect("A", Color.RED, 100);
//            setImageDrawable(drawable);
            String filePathFromCache = FileDownloaderManager.getInstance().getFilePath(fileId);
            if (filePathFromCache.equals(FileDownloaderManager.FILE_PATH_EMPTY)) {
                subscribeToUpdateChannel();
            } else {
                setFileToView(filePathFromCache);
            }
        } else {
            setFileToView(mainListItem.getSmallPhotoFilePath());
        }
    }

    private void setLetterDrawable(ImageView imageView, MainListItem mainListItem) {
        LetterDrawable letterDrawable = getLetterDrawable(imageView, mainListItem);
        imageView.setImageDrawable(letterDrawable);
    }

    private LetterDrawable getLetterDrawable(ImageView imageView, MainListItem mainListItem) {
        int id = 0;
        String firstName = null;
        String lastName = null;

        if (mainListItem.isGroupChat()) {
            TdApi.GroupChatInfo groupChatInfo = ((TdApi.GroupChatInfo)mainListItem.getApiChat().type);
            id = groupChatInfo.groupChat.id;
            firstName = groupChatInfo.groupChat.title;
        } else {
            TdApi.PrivateChatInfo privateChatInfo = ((TdApi.PrivateChatInfo)mainListItem.getApiChat().type);
            id = privateChatInfo.user.id;
            firstName = privateChatInfo.user.firstName;
            lastName = privateChatInfo.user.lastName;
        }
        // setImageDrawable for not null Rect rect!
        if (imageView.getDrawable() == null) {
            imageView.setImageDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), android.R.color.white)));
        }
        Rect rect = imageView.getDrawable().getBounds();
        Logger.debug("rect", rect);
        return new LetterDrawable(rect, id, firstName, lastName);
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
                .filter(update -> update instanceof TdApi.UpdateFile && ((TdApi.UpdateFile)update).file.id == fileId)
                .map(update -> (TdApi.UpdateFile)update)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TdApi.UpdateFile>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

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
