package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * TEMP
 *
 * @author e.matsyuk
 */
public class MessageContentActionsItem extends MessageContentItem {

    private TdApi.MessageContent messageContent;


    public MessageContentActionsItem(TdApi.MessageContent messageContent) {
        this.messageContent = messageContent;
    }

    public TdApi.MessageContent getMessageContent() {
        return messageContent;
    }

}
