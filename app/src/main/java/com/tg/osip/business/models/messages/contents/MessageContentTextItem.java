package com.tg.osip.business.models.messages.contents;

import android.support.annotation.NonNull;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class MessageContentTextItem extends MessageContentItem {

    private String text;

    public MessageContentTextItem(TdApi.MessageContent messageContent) {
        if (messageContent == null || messageContent.getClass() != TdApi.MessageText.class) {
            return;
        }
        TdApi.MessageText messageText = (TdApi.MessageText)messageContent;
        text = messageText.text;
    }

    @NonNull
    public String getText() {
        if (text == null) {
            return "";
        }
        return text;
    }

}
