package com.tg.osip.business.models.messages.contents;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Abstract class for chat participant content items
 *
 * @author e.matsyuk
 */
public abstract class ChatParticipantItem<T extends TdApi.MessageContent> extends MessageContentItem {

    private final static String SPACE = " ";

    private String name;

    @VisibleForTesting
    public ChatParticipantItem() {}

    public ChatParticipantItem(T messageContent) {
        TdApi.User user = getUser(messageContent);
        initName(user);
    }

    abstract TdApi.User getUser(T messageContent);

    void initName(TdApi.User user) {
        if (user == null) {
            return;
        }
        String userName = "";
        if (user.firstName != null) {
            userName += user.firstName;
        }
        if (user.lastName != null) {
            if (user.firstName != null) {
                userName += SPACE;
            }
            userName += user.lastName;
        }
        name = userName;
    }

    @NonNull
    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

}
