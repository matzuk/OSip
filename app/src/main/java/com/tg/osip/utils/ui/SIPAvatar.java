package com.tg.osip.utils.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.FileDownloaderManager;
import com.tg.osip.business.UpdateManager;
import com.tg.osip.tdclient.TGProxy;
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

    private final static String FILE_PATH_EMPTY = "";
    private final static int EMPTY_FILE_ID = 0;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SIPAvatar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setChatInfo(TdApi.ChatInfo chatInfo) {
        startImageLoading(chatInfo);
    }

    private void startImageLoading(TdApi.ChatInfo chatInfo) {
        unSubscribe();
        String filePath = getFilePath(chatInfo);
        if (filePath.equals(FILE_PATH_EMPTY)) {
            setFileId(chatInfo);
            setLetterDrawable(this, chatInfo);
            String filePathFromCache = FileDownloaderManager.getInstance().getFilePath(fileId);
            if (filePathFromCache.equals(FILE_PATH_EMPTY)) {
                subscribeToUpdateChannel();
            } else {
                setFileToView(filePathFromCache);
            }
        } else {
            setFileToView(filePath);
        }
    }

    private void setLetterDrawable(ImageView imageView, TdApi.ChatInfo chatInfo) {
        LetterDrawable letterDrawable = getLetterDrawable(imageView, chatInfo);
        imageView.setImageDrawable(letterDrawable);
    }

    private LetterDrawable getLetterDrawable(ImageView imageView, TdApi.ChatInfo chatInfo) {
        int id = 0;
        String firstName = null;
        String lastName = null;

        if (chatInfo instanceof TdApi.GroupChatInfo) {
            id = ((TdApi.GroupChatInfo)chatInfo).groupChat.id;
            firstName = ((TdApi.GroupChatInfo)chatInfo).groupChat.title;
        }
        if (chatInfo instanceof TdApi.PrivateChatInfo) {
            id = ((TdApi.PrivateChatInfo)chatInfo).user.id;
            firstName = ((TdApi.PrivateChatInfo)chatInfo).user.firstName;
            lastName = ((TdApi.PrivateChatInfo)chatInfo).user.lastName;
        }
        // setImageDrawable for not null Rect rect!
        if (imageView.getDrawable() == null) {
            imageView.setImageDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), android.R.color.white)));
        }
        Rect rect = imageView.getDrawable().getBounds();
        return new LetterDrawable(rect, id, firstName, lastName);
    }

    private String getFilePath(TdApi.ChatInfo chatInfo) {
        String filePath = "";
        if (chatInfo instanceof TdApi.GroupChatInfo) {
            filePath = ((TdApi.GroupChatInfo)chatInfo).groupChat.photo.small.path;
        } else if (chatInfo instanceof TdApi.PrivateChatInfo) {
            filePath = ((TdApi.PrivateChatInfo)chatInfo).user.profilePhoto.small.path;
        }
        if (!TextUtils.isEmpty(filePath)) {
            return ADD_TO_PATH + filePath;
        }
        return FILE_PATH_EMPTY;
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

    private void setFileId(TdApi.ChatInfo chatInfo) {
        if (chatInfo instanceof TdApi.GroupChatInfo) {
            fileId = ((TdApi.GroupChatInfo)chatInfo).groupChat.photo.small.id;
        } else if (chatInfo instanceof TdApi.PrivateChatInfo) {
            fileId = ((TdApi.PrivateChatInfo)chatInfo).user.profilePhoto.small.id;
        } else {
            fileId = EMPTY_FILE_ID;
        }
    }

    private void downloadFile() {
        if (fileId > EMPTY_FILE_ID) {
            TGProxy.getInstance().sendTD(new TdApi.DownloadFile(fileId), TdApi.Ok.class)
                    .subscribe();
        }
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
