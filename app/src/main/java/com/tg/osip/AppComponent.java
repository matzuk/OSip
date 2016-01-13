package com.tg.osip;

import com.tg.osip.business.AuthManager;
import com.tg.osip.business.media.MediaModule;
import com.tg.osip.tdclient.TGModule;
import com.tg.osip.ui.activities.LoginActivity;
import com.tg.osip.ui.activities.MainActivity;
import com.tg.osip.ui.activities.PhotoMediaActivity;
import com.tg.osip.ui.chats.ChatsFragment;
import com.tg.osip.ui.general.views.ProgressTextView;
import com.tg.osip.ui.general.views.progress_download.AudioProgressDownloadView;
import com.tg.osip.ui.general.views.progress_download.VideoProgressDownloadView;
import com.tg.osip.ui.general.views.progress_download.play_actions.AudioPlayAction;
import com.tg.osip.ui.general.views.progress_download.ProgressDownloadView;
import com.tg.osip.ui.general.views.images.PhotoView;
import com.tg.osip.ui.messages.MessagesFragment;
import com.tg.osip.utils.dagger2_static.StaticModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Component for all application
 * @author e.matsyuk
 */
@Component(modules = {AppModule.class, TGModule.class, MediaModule.class, StaticModule.class})
@Singleton
public interface AppComponent {

    MainActivity.MainActivityComponent plus( MainActivity.MainActivityModule mainActivityModule);
    ChatsFragment.ChatsComponent plus( ChatsFragment.ChatsModule chatsModule);
    MessagesFragment.MessagesComponent plus( MessagesFragment.MessagesModule messagesModule);

    void inject(ApplicationSIP applicationSIP);
    void inject(LoginActivity loginActivity);
    void inject(AuthManager authManager);
    void inject(PhotoMediaActivity photoMediaActivity);
    void inject(PhotoView photoView);
    void inject(AudioProgressDownloadView audioProgressDownloadView);
    void inject(VideoProgressDownloadView videoProgressDownloadView);
    void inject(AudioPlayAction progressActionAudio);
    void inject(ProgressTextView progressTextView);
}
