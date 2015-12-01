package com.tg.osip.ui.messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tg.osip.R;
import com.tg.osip.business.models.ChatItem;
import com.tg.osip.ui.general.views.images.PhotoView;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Adapter for custom message toolbar
 *
 * @author e.matsyuk
 */
public class MessageToolbarAdapter {

    private Context context;
    private ChatItem chatItem;
    private View toolbarView;

    public MessageToolbarAdapter(Context context, TdApi.Chat chat) {
        this.context = context;
        this.chatItem = new ChatItem(chat);
        initToolbarView();
    }

    private void initToolbarView() {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View headerView = layoutInflater.inflate(R.layout.toolbar_messages, null);

        PhotoView headerAvatar = (PhotoView)headerView.findViewById(R.id.avatar);
        headerAvatar.setCircleRounds(true);
        headerAvatar.setImageLoaderI(chatItem);
        // not start file downloading because all chats avatars downloading was started in ChatsController

        TextView chatNameView = (TextView)headerView.findViewById(R.id.chat_name);
        chatNameView.setText(chatItem.getUserName());

        TextView chatInfoView = (TextView)headerView.findViewById(R.id.chat_info);
        chatInfoView.setText(chatItem.getInfo());

        toolbarView = headerView;
    }

    public View getToolbarView() {
        return toolbarView;
    }

}
