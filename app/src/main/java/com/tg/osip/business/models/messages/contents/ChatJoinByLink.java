package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatJoinByLink extends MessageContentItem {

    private int inviterId;

    public ChatJoinByLink(TdApi.MessageChatJoinByLink messageContent) {
        if (messageContent == null) {
            return;
        }
        inviterId = messageContent.inviterId;
    }

    public int getInviterId() {
        return inviterId;
    }

}
