package com.tg.osip.tdclient.models;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Comfortable model for {@link com.tg.osip.ui.main.MainRecyclerAdapter MainRecyclerAdapter}
 *
 * @author e.matsyuk
 */
public class MainListItem {

    private TdApi.Chat apiChat;
    private String lastMessageDate;

    public MainListItem(TdApi.Chat apiChat) {
        this.apiChat = apiChat;
    }

    public TdApi.Chat getApiChat() {
        return apiChat;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }
}
