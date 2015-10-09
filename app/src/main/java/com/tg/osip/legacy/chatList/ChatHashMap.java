package com.tg.osip.legacy.chatList;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Special container for ChatList Fragment
 * Container is enabled for getting from position and chatId
 * ALso it is enabled for real time updating
 *
 * @author e.matsyuk
 */
public class ChatHashMap {

    private List<TdApi.Chat> chatList = new ArrayList<>();
    /**
     * key - chat id, value - concrete TdApi.Chat
     */
    private HashMap<Long, TdApi.Chat> chatHashMap = new HashMap<>();

    public synchronized void addChat(TdApi.Chat chat) {
        if (chat == null) {
            return;
        }
        chatList.add(chat);
        chatHashMap.put(chat.id, chat);
    }

    public synchronized void addAllChat(TdApi.Chats chats) {
        if (chats == null || chats.chats == null) {
            return;
        }
        for (int i = 0; i < chats.chats.length; i++) {
            chatList.add(chats.chats[i]);
            chatHashMap.put(chats.chats[i].id, chats.chats[i]);
        }
    }

    public synchronized TdApi.Chat getChatFromPosition(int position) {
        return chatList.get(position);
    }

    public synchronized boolean isChatInMap(long chatId) {
        if (chatHashMap.get(chatId) == null) {
            return false;
        } else {
            return true;
        }
    }

    public synchronized void changeChat(TdApi.Chat chat) {
        if (chat == null) {
            return;
        }
        long chatId = chat.id;
        TdApi.Chat chatFromMap = chatHashMap.get(chatId);
        if (chatFromMap == null) {
            return;
        }
        chatFromMap.lastReadOutboxMessageId = chat.lastReadOutboxMessageId;
        chatFromMap.lastReadInboxMessageId = chat.lastReadInboxMessageId;
        chatFromMap.topMessage = chat.topMessage;
        chatFromMap.unreadCount = chat.unreadCount;
        chatFromMap.notificationSettings = chat.notificationSettings;
        chatFromMap.type = chat.type;
    }

    public synchronized int indexOf(TdApi.Chat chat)  {
        for (int i = 0; i < chatList.size(); i++) {
            TdApi.Chat chatInList = chatList.get(i);
            if (chatInList.id == chat.id) {
                return i;
            }
        }
        return -1;
    }

    public synchronized int size() {
        return chatList.size();
    }

}
