package com.tg.osip.business.models;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tg.osip.ui.general.views.images.ImageLoaderI;
import com.tg.osip.ui.messages.MessagesRecyclerAdapter;
import com.tg.osip.utils.CommonStaticFields;
import com.tg.osip.utils.common.AndroidUtils;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Comfortable model for {@link MessagesRecyclerAdapter MessagesRecyclerAdapter}
 *
 * @author e.matsyuk
 */
public class UserItem implements ImageLoaderI {

    private TdApi.User user;
    private int photoFileId;
    private String photoFilePath;
    private Drawable plug;
    private String name;
    private String phone;

    public UserItem(TdApi.User user) {
        this.user = user;
        init(user);
    }

    private void init(TdApi.User user) {
        initFileId(user);
        initFilePath(user);
        initPlug();
        initName();
        initPhone();
    }

    private void initFileId(TdApi.User user) {
        photoFileId = user.profilePhoto.small.id;
    }

    private void initFilePath(TdApi.User user) {
        String filePath = user.profilePhoto.small.path;
        if (!TextUtils.isEmpty(filePath)) {
            photoFilePath = CommonStaticFields.ADD_TO_PATH + filePath;
            return;
        }
        photoFilePath = CommonStaticFields.EMPTY_STRING;
    }

    private void initPlug() {
        int id = user.id;
        String name = AndroidUtils.getLettersForPlug(user.firstName, user.lastName);
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(id);
        plug = TextDrawable.builder().buildRoundRect(name, color, 100);
    }

    private void initName() {
        StringBuilder stringBuilder = new StringBuilder();
        if (user.firstName != null) {
            stringBuilder.append(user.firstName);
            stringBuilder.append(CommonStaticFields.SPACE);
        }
        if (user.lastName != null) {
            stringBuilder.append(user.lastName);
        }
        name = stringBuilder.toString();
    }

    private void initPhone() {
        if (user.phoneNumber != null) {
            phone = user.phoneNumber;
        } else {
            phone = CommonStaticFields.EMPTY_STRING;
        }
    }

    public TdApi.User getUser() {
        return user;
    }

    @Override
    public int getFileId() {
        return photoFileId;
    }

    @Override
    public String getFilePath() {
        return photoFilePath;
    }

    @Override
    public Drawable getPlug() {
        return plug;
    }

    @Override
    public PhotoItem getPlugFile() {
        return null;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public String getPhone() {
        return phone;
    }

}
