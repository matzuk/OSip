package com.tg.osip.business.models;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.ui.chats.ChatRecyclerAdapter;
import com.tg.osip.ui.general.views.images.ImageLoaderI;
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
    private final static String EMPTY_STRING = "";
    private final static String SPACE = " ";

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
        initChatLastMessage(chat.topMessage);
        initName(chat.type);
        initFileId(chat.type);
        initFilePath(chat.type);
        initPlug();
        initInfo();
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
        return !smallPhotoFilePath.equals(EMPTY_STRING);
    }

    @Override
    public Drawable getPlug() {
        return plug;
    }

    public String getInfo() {
        return info;
    }

    private void initChatLastMessage(TdApi.Message message) {
        TdApi.MessageContent messageContent = message.message;
        if (messageContent == null) {
            lastMessageText = EMPTY_STRING;
            return;
        }
        if (messageContent.getClass() == TdApi.MessageText.class) {
            lastMessageText = ((TdApi.MessageText)messageContent).text != null ? ((TdApi.MessageText)messageContent).text : EMPTY_STRING;
        } else if (messageContent.getClass() == TdApi.MessageAudio.class) {
            lastMessageText = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_audio);
        } else if (messageContent.getClass() == TdApi.MessageVideo.class) {
            lastMessageText = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_video);
        } else if (messageContent.getClass() == TdApi.MessagePhoto.class) {
            lastMessageText = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_photo);
        } else if (messageContent.getClass() == TdApi.MessageSticker.class) {
            lastMessageText = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_sticker);
        } else {
            lastMessageText = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_other);
        }
    }

    private void initName(TdApi.ChatInfo chatInfo) {
        if (groupChat) {
            userName = ((TdApi.GroupChatInfo)chatInfo).groupChat.title != null ? ((TdApi.GroupChatInfo)chatInfo).groupChat.title : EMPTY_STRING;
        } else {
            userName = initNameFromPrivateChat(((TdApi.PrivateChatInfo)chatInfo).user);
        }
    }

    private String initNameFromPrivateChat(TdApi.User user) {
        StringBuilder stringBuilder = new StringBuilder();
        if (user.firstName != null) {
            stringBuilder.append(user.firstName);
            stringBuilder.append(SPACE);
        }
        if (user.lastName != null) {
            stringBuilder.append(user.lastName);
        }
        return stringBuilder.toString();
    }

    private boolean determineIsChatGroupType(TdApi.ChatInfo chatInfo) {
        return chatInfo.getClass() == TdApi.GroupChatInfo.class;
    }

    private void initFileId(TdApi.ChatInfo chatInfo) {
        if (groupChat) {
            smallPhotoFileId = ((TdApi.GroupChatInfo)chatInfo).groupChat.photo.small.id;
        } else {
            smallPhotoFileId = ((TdApi.PrivateChatInfo)chatInfo).user.profilePhoto.small.id;
        }
    }

    private void initFilePath(TdApi.ChatInfo chatInfo) {
        String filePath;
        if (groupChat) {
            filePath = ((TdApi.GroupChatInfo)chatInfo).groupChat.photo.small.path;
        } else {
            filePath = ((TdApi.PrivateChatInfo)chatInfo).user.profilePhoto.small.path;
        }
        if (!TextUtils.isEmpty(filePath)) {
            smallPhotoFilePath = ADD_TO_PATH + filePath;
        } else {
            smallPhotoFilePath = EMPTY_STRING;
        }
    }

    private void initPlug() {
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
        plug = TextDrawable.builder().buildRoundRect(name, color, 100);
    }

    private void initInfo() {
        TdApi.ChatInfo chatInfo = chat.type;
        if (chatInfo.getClass() == TdApi.GroupChatInfo.class) {
            info = getHeaderInfoForGroupChat((TdApi.GroupChatInfo)chatInfo);
            return;
        } else if (chatInfo.getClass() == TdApi.PrivateChatInfo.class) {
            info = getHeaderInfoForPrivateChat((TdApi.PrivateChatInfo)chatInfo);
            return;
        }
        info = EMPTY_STRING;
    }

    private String getHeaderInfoForPrivateChat(TdApi.PrivateChatInfo privateChatInfo) {
        TdApi.UserStatus userStatus = privateChatInfo.user.status;
        if (userStatus instanceof TdApi.UserStatusOnline) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_status_online);
        } else {
            // FIXME other statuses
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_status_offline);
        }
    }

    private String getHeaderInfoForGroupChat(TdApi.GroupChatInfo groupChatInfo) {
        int count = groupChatInfo.groupChat.participantsCount;
        return String.valueOf(count) + " " + ApplicationSIP.applicationContext.getString(R.string.chat_status_members);
        // FIXME add online count
    }

}
