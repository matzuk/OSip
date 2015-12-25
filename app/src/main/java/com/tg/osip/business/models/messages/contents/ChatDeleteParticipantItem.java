package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatDeleteParticipantItem extends ChatParticipantItem<TdApi.MessageChatDeleteParticipant> {

    public ChatDeleteParticipantItem(TdApi.MessageChatDeleteParticipant messageContent) {
        super(messageContent);
    }

    @Override
    TdApi.User getUser(TdApi.MessageChatDeleteParticipant messageContent) {
        if (messageContent == null) {
            return null;
        }
        return messageContent.user;
    }

}
