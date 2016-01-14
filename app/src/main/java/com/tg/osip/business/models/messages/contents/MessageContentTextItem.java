package com.tg.osip.business.models.messages.contents;

import android.support.annotation.NonNull;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class MessageContentTextItem extends MessageContentItem {

    private String text;

    public MessageContentTextItem(TdApi.MessageText messageContent) {
        if (messageContent == null) {
            return;
        }
        text = messageContent.text;
    }

    @NonNull
    public String getText() {
        if (text == null) {
            return "";
        }
        return text;
    }

}
