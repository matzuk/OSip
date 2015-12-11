package com.tg.osip;

import com.tg.osip.business.AuthManager;
import com.tg.osip.tdclient.TGModule;
import com.tg.osip.ui.activities.MainActivity;
import com.tg.osip.ui.activities.PhotoMediaActivity;
import com.tg.osip.ui.chats.ChatsFragment;
import com.tg.osip.ui.general.views.images.PhotoView;
import com.tg.osip.ui.messages.MessagesFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Component for all application
 * @author e.matsyuk
 */
@Component(modules = {AppModule.class, TGModule.class})
@Singleton
public interface AppComponent {

    MainActivity.MainActivityComponent plus( MainActivity.MainActivityModule mainActivityModule);
    ChatsFragment.ChatsComponent plus( ChatsFragment.ChatsModule chatsModule);
    MessagesFragment.MessagesComponent plus( MessagesFragment.MessagesModule messagesModule);

    void inject(ApplicationSIP applicationSIP);
    void inject(AuthManager authManager);
    void inject(PhotoMediaActivity photoMediaActivity);
    void inject(PhotoView photoView);
}
