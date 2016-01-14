package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class GroupChatCreate extends MessageContentItem {

    private String title;
    // FIXME
    private TdApi.User[] users;

    public GroupChatCreate(TdApi.MessageGroupChatCreate messageContent) {
        if (messageContent == null) {
            return;
        }
        title = messageContent.title;
        users = messageContent.participants;
    }

    public String getTitle() {
        if (title == null) {
            return "";
        }
        return title;
    }

}
