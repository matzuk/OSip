package com.tg.osip.tdclient.models;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.FileDownloaderManager;
import com.tg.osip.utils.time.TimeUtils;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Comfortable model for {@link com.tg.osip.ui.main.MainRecyclerAdapter MainRecyclerAdapter}
 *
 * @author e.matsyuk
 */
public class MainListItem {

    private final static int EMPTY_FILE_ID = 0;
    private final static String ADD_TO_PATH = "file://";
    private final static String FILE_PATH_EMPTY = "";

    private TdApi.Chat apiChat;
    private String lastMessageDate;
    private String lastMessageText;
    private String userName;
    private boolean groupChat;
    private int smallPhotoFileId;
    private String smallPhotoFilePath;
    //
    private Drawable plug;

    public MainListItem(TdApi.Chat apiChat) {
        this.apiChat = apiChat;
        init(apiChat);
    }

    private void init(TdApi.Chat chat) {
        groupChat = determineIsChatGroupType(chat.type);
        lastMessageDate = TimeUtils.stringForMessageListDate(chat.topMessage.date);
        lastMessageText = getChatLastMessage(chat.topMessage);
        userName = getName(chat.type);
        smallPhotoFileId = getFileId(chat.type);
        smallPhotoFilePath = getFilePath(chat.type);
        plug = setPlug();
    }

    public TdApi.Chat getApiChat() {
        return apiChat;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isGroupChat() {
        return groupChat;
    }

    public int getSmallPhotoFileId() {
        return smallPhotoFileId;
    }

    public boolean isSmallPhotoFileIdValid() {
        return smallPhotoFileId != EMPTY_FILE_ID;
    }

    public String getSmallPhotoFilePath() {
        return smallPhotoFilePath;
    }

    public boolean isSmallPhotoFilePathValid() {
        return !smallPhotoFilePath.equals(FILE_PATH_EMPTY);
    }

    public Drawable getPlug() {
        return plug;
    }

    private String getChatLastMessage(TdApi.Message message) {
        TdApi.MessageContent messageContent = message.message;
        if (messageContent == null) {
            return "";
        }
        if (messageContent.getClass() == TdApi.MessageText.class) {
            return ((TdApi.MessageText)messageContent).text;
        }
        if (messageContent.getClass() == TdApi.MessageAudio.class) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_audio);
        }
        if (messageContent.getClass() == TdApi.MessageVideo.class) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_video);
        }
        if (messageContent.getClass() == TdApi.MessagePhoto.class) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_photo);
        }
        if (messageContent.getClass() == TdApi.MessageSticker.class) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_sticker);
        }
        return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_other);
    }

    private String getName(TdApi.ChatInfo chatInfo) {
        if (groupChat) {
            return ((TdApi.GroupChatInfo)chatInfo).groupChat.title;
        } else {
            return ((TdApi.PrivateChatInfo)chatInfo).user.firstName + " " + ((TdApi.PrivateChatInfo)chatInfo).user.lastName;
        }
    }

    private boolean determineIsChatGroupType(TdApi.ChatInfo chatInfo) {
        if (chatInfo instanceof TdApi.GroupChatInfo) {
            return true;
        }
        return false;
    }

    private Integer getFileId(TdApi.ChatInfo chatInfo) {
        if (groupChat) {
            return ((TdApi.GroupChatInfo)chatInfo).groupChat.photo.small.id;
        } else {
            return ((TdApi.PrivateChatInfo)chatInfo).user.profilePhoto.small.id;
        }
    }
    private String getFilePath(TdApi.ChatInfo chatInfo) {
        String filePath;
        if (groupChat) {
            filePath = ((TdApi.GroupChatInfo)chatInfo).groupChat.photo.small.path;
        } else {
            filePath = ((TdApi.PrivateChatInfo)chatInfo).user.profilePhoto.small.path;
        }
        if (!TextUtils.isEmpty(filePath)) {
            return ADD_TO_PATH + filePath;
        }
        return FILE_PATH_EMPTY;
    }

    private Drawable setPlug() {
        int id;
        StringBuilder name = new StringBuilder();
        if (isGroupChat()) {
            TdApi.GroupChatInfo groupChatInfo = ((TdApi.GroupChatInfo)getApiChat().type);
            id = groupChatInfo.groupChat.id;
            name.append(groupChatInfo.groupChat.title.substring(0, 1));
        } else {
            TdApi.PrivateChatInfo privateChatInfo = ((TdApi.PrivateChatInfo)getApiChat().type);
            id = privateChatInfo.user.id;
            name.append(privateChatInfo.user.firstName.substring(0, 1));
//            name.append(privateChatInfo.user.lastName.substring(0, 1));
        }
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(id);

        return TextDrawable.builder()
                .buildRoundRect(name.toString(), color, 100);
    }


}
