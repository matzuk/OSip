package com.tg.osip.business.models;

import java.util.List;
import java.util.Map;

/**
 * Model for MessagesAdapter
 *
 * @author e.matsyuk
 */
public class MessageAdapterModel {

    private List<MessageItem> messageItemList;
    private Map<Integer, UserItem> integerUserItemMap;

    public MessageAdapterModel(List<MessageItem> messageItemList, Map<Integer, UserItem> integerUserItemMap) {
        this.messageItemList = messageItemList;
        this.integerUserItemMap = integerUserItemMap;
    }

    public List<MessageItem> getMessageItemList() {
        return messageItemList;
    }

    public Map<Integer, UserItem> getIntegerUserItemMap() {
        return integerUserItemMap;
    }
}
