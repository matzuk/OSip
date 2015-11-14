package com.tg.osip.business.chat;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tg.osip.ui.views.images.ImageLoaderI;
import com.tg.osip.utils.common.AndroidUtils;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Comfortable model for {@link com.tg.osip.ui.chat.ChatRecyclerAdapter ChatRecyclerAdapter}
 *
 * @author e.matsyuk
 */
public class UserChatListItem implements ImageLoaderI {

    private final static int EMPTY_FILE_ID = 0;
    private final static String ADD_TO_PATH = "file://";
    private final static String FILE_PATH_EMPTY = "";

    private TdApi.User user;
    private int smallPhotoFileId;
    private String smallPhotoFilePath;
    private Drawable plug;

    public UserChatListItem(TdApi.User user) {
        this.user = user;
        init(user);
    }

    private void init(TdApi.User user) {
        smallPhotoFileId = getFileId(user);
        smallPhotoFilePath = getFilePath(user);
        plug = setPlug();
    }

    private Integer getFileId(TdApi.User user) {
        return user.profilePhoto.small.id;
    }

    private String getFilePath(TdApi.User user) {
        String filePath = user.profilePhoto.small.path;
        if (!TextUtils.isEmpty(filePath)) {
            return ADD_TO_PATH + filePath;
        }
        return FILE_PATH_EMPTY;
    }

    private Drawable setPlug() {
        int id = user.id;
        String name = AndroidUtils.getLettersForPlug(user.firstName, user.lastName);
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(id);
        return TextDrawable.builder()
                .buildRoundRect(name, color, 100);
    }

    public TdApi.User getUser() {
        return user;
    }

    @Override
    public int getSmallPhotoFileId() {
        return smallPhotoFileId;
    }

    @Override
    public boolean isSmallPhotoFileIdValid() {
        return smallPhotoFileId != EMPTY_FILE_ID;
    }

    @Override
    public String getSmallPhotoFilePath() {
        return smallPhotoFilePath;
    }

    @Override
    public boolean isSmallPhotoFilePathValid() {
        return !smallPhotoFilePath.equals(FILE_PATH_EMPTY);
    }

    @Override
    public Drawable getPlug() {
        return plug;
    }

}
