package com.tg.osip.business.models;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.ui.chats.ChatRecyclerAdapter;
import com.tg.osip.ui.views.images.ImageLoaderI;
import com.tg.osip.utils.common.AndroidUtils;
import com.tg.osip.utils.time.TimeUtils;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Comfortable model for {@link ChatRecyclerAdapter ChatRecyclerAdapter}
 *
 * @author e.matsyuk
 */
public class ChatItem implements ImageLoaderI {

    private final static int EMPTY_FILE_ID = 0;
    private final static String ADD_TO_PATH = "file://";
    private final static String FILE_PATH_EMPTY = "";

    private TdApi.Chat chat;
    private String lastMessageDate;
    private String lastMessageText;
    private String userName;
    private boolean groupChat;
    private int smallPhotoFileId;
    private String smallPhotoFilePath;
    private Drawable plug;
    private String info;

    public ChatItem(TdApi.Chat chat) {
        this.chat = chat;
        init(chat);
    }

    private void init(TdApi.Chat chat) {
        groupChat = determineIsChatGroupType(chat.type);
        lastMessageDate = TimeUtils.stringForMessageListDate(chat.topMessage.date);
        lastMessageText = getChatLastMessage(chat.topMessage);
        userName = getName(chat.type);
        smallPhotoFileId = getFileId(chat.type);
        smallPhotoFilePath = getFilePath(chat.type);
        plug = loadPlug();
        info = loadInfo();
    }

    public TdApi.Chat getChat() {
        return chat;
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

    public String getInfo() {
        return info;
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
        return chatInfo.getClass() == TdApi.GroupChatInfo.class;
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

    private Drawable loadPlug() {
        int id;
        String name;
        if (isGroupChat()) {
            TdApi.GroupChatInfo groupChatInfo = ((TdApi.GroupChatInfo) getChat().type);
            id = groupChatInfo.groupChat.id;
            name = AndroidUtils.getLettersForPlug(groupChatInfo.groupChat.title, null);
        } else {
            TdApi.PrivateChatInfo privateChatInfo = ((TdApi.PrivateChatInfo) getChat().type);
            id = privateChatInfo.user.id;
            name = AndroidUtils.getLettersForPlug(privateChatInfo.user.firstName, privateChatInfo.user.lastName);
        }
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(id);

        return TextDrawable.builder()
                .buildRoundRect(name, color, 100);
    }

    private String loadInfo() {
        TdApi.ChatInfo chatInfo = chat.type;
        String info = "";
        if (chatInfo.getClass() == TdApi.GroupChatInfo.class) {
            return getHeaderInfoForGroupChat((TdApi.GroupChatInfo)chatInfo);
        } else if (chatInfo.getClass() == TdApi.PrivateChatInfo.class) {
            return getHeaderInfoForPrivateChat((TdApi.PrivateChatInfo)chatInfo);
        }
        return info;
    }

    private String getHeaderInfoForPrivateChat(TdApi.PrivateChatInfo privateChatInfo) {
        String info = "";
        TdApi.UserStatus userStatus = privateChatInfo.user.status;
        if (userStatus instanceof TdApi.UserStatusOnline) {
            info = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_status_online);
        } else {
            // FIXME other statuses
            info = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_status_offline);
        }
        return info;
    }

    private String getHeaderInfoForGroupChat(TdApi.GroupChatInfo groupChatInfo) {
        String info = "";
        int count = groupChatInfo.groupChat.participantsCount;
        info = String.valueOf(count) + " " + ApplicationSIP.applicationContext.getString(R.string.chat_status_members);
        // FIXME add online count
        return info;
    }

}
