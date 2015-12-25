package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatChangeTitleItem extends MessageContentItem {

    private String title;

    public ChatChangeTitleItem(TdApi.MessageChatChangeTitle messageContent) {
        if (messageContent == null) {
            return;
        }
        title = messageContent.title;
    }

    public String getTitle() {
        if (title == null) {
            return "";
        }
        return title;
    }

}
