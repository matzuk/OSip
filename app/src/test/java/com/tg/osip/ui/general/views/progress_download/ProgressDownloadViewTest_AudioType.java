package com.tg.osip.ui.general.views.progress_download;

import android.app.Activity;
import android.support.v4.util.Pair;

import com.tg.osip.RobolectricUnitTestRunner;
import com.tg.osip.business.media.MediaManager;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

import rx.subjects.PublishSubject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests with Robolectric
 *
 * @author e.matsyuk
 */
@RunWith(RobolectricUnitTestRunner.class)
public class ProgressDownloadViewTest_AudioType {

    @Mock
    FileDownloaderManager fileDownloaderManager;
    @Mock
    MediaManager mediaManager;

    PublishSubject<Pair<Integer, Integer>> downloadProgressChannel;
    PublishSubject<Integer> downloadChannel;
    Activity activity;

    @Before
    public void setup() {
        // Mockito
        MockitoAnnotations.initMocks(this);
        // Robolectic
        activity = Robolectric.setupActivity(Activity.class);

        downloadProgressChannel = PublishSubject.create();
        downloadChannel = PublishSubject.create();
        when(fileDownloaderManager.getDownloadProgressChannel()).thenReturn(downloadProgressChannel);
        when(fileDownloaderManager.getDownloadChannel()).thenReturn(downloadChannel);
    }

    @Test
    public void setDownloadingState_emptyFileDownloaderI() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 0;
            }
        };
        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.START);
    }

    @Test
    public void setDownloadingState_play() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);

        MediaManager mediaManager = mock(MediaManager.class);
        when(mediaManager.getCurrentIdFile()).thenReturn(100);
        when(mediaManager.isPaused()).thenReturn(false);
        progressDownloadView.setMediaManager(mediaManager);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "123";
            }

            @Override
            public String getFilePath() {
                return "123";
            }

            @Override
            public int getFileId() {
                return 100;
            }
        };
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.playAction.getId()).isEqualTo(100);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PLAY);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
    }

    @Test
    public void setDownloadingState_pausePlay() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);

        MediaManager mediaManager = mock(MediaManager.class);
        when(mediaManager.getCurrentIdFile()).thenReturn(100);
        when(mediaManager.isPaused()).thenReturn(true);
        progressDownloadView.setMediaManager(mediaManager);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "123";
            }

            @Override
            public String getFilePath() {
                return "123";
            }

            @Override
            public int getFileId() {
                return 100;
            }
        };
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.playAction.getId()).isEqualTo(100);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PAUSE_PLAY);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
    }

    @Test
    public void progressUpdatingTest_downloadingState() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        // file is loading in fileDownloaderManager
        when(fileDownloaderManager.isFileInProgress(10)).thenReturn(true);
        when(fileDownloaderManager.getProgressValue(10)).thenReturn(20);
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };

        // before setFileDownloader all channels are null
        assertThat(progressDownloadView.downloadProgressChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadChannelSubscription).isNull();
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        // setFileDownloader
        progressDownloadView.setFileDownloader(fileDownloaderI);
        // download channels subscribed immediately
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);;
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        // progress
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(20);
        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);
        downloadProgressChannel.onNext(new Pair<>(20, 60));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);
        downloadProgressChannel.onNext(new Pair<>(10, 85));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(85);
        // finish loading
        downloadProgressChannel.onNext(new Pair<>(10, 100));
        downloadChannel.onNext(10);
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
    }

    @Test
    public void progressUpdatingTest_startState() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(20)).thenReturn(false);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 20;
            }
        };

        // setFileDownloader
        progressDownloadView.setFileDownloader(fileDownloaderI);
        // all channels are null
        assertThat(progressDownloadView.downloadProgressChannelSubscription).isNull();
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadChannelSubscription).isNull();
        // click download button
        progressDownloadView.callOnClick();
        // download channels subscribed
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // progress
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);
        downloadProgressChannel.onNext(new Pair<>(20, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);
        downloadProgressChannel.onNext(new Pair<>(21, 60));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);
        downloadProgressChannel.onNext(new Pair<>(20, 85));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(85);
        // finish loading
        downloadProgressChannel.onNext(new Pair<>(20, 100));
        downloadChannel.onNext(20);
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
    }

    @Test
    public void progressUpdatingTest_someFileDownloaders() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        // file 40 is not exist yet
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(40)).thenReturn(false);
        // file 10 is loading in fileDownloaderManager
        when(fileDownloaderManager.isFileInProgress(10)).thenReturn(true);
        when(fileDownloaderManager.getProgressValue(10)).thenReturn(20);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };

        // before setFileDownloader all channels are null
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadChannelSubscription).isNull();
        // setFileDownloader 10
        progressDownloadView.setFileDownloader(fileDownloaderI);
        // download channels subscribed immediately
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // progress
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(20);
        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        FileDownloaderI fileDownloaderI2 = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 40;
            }
        };
        // setFileDownloader 40
        progressDownloadView.setFileDownloader(fileDownloaderI2);
        // unsubscribe channels
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        // start new downloading
        progressDownloadView.callOnClick();
        // download channels subscribe
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);

        // progress
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);
        downloadProgressChannel.onNext(new Pair<>(40, 100));
        downloadChannel.onNext(40);
        // finish loading
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
    }

    @Test
    public void progressUpdatingTest_pauseState() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(50)).thenReturn(false);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 50;
            }
        };

        // before setFileDownloader all channels are null
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadChannelSubscription).isNull();
        // setFileDownloader 50
        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.START);
        // start downloading
        progressDownloadView.callOnClick();
        // download channels subscribe
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // progress
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);
        downloadProgressChannel.onNext(new Pair<>(50, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);
        // click pause
        progressDownloadView.callOnClick();
        // download channels unsubscribe
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.START);
        // start downloading again
        progressDownloadView.callOnClick();
        // download channels subscribe
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.DOWNLOADING);
        // progress
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);
        downloadProgressChannel.onNext(new Pair<>(50, 50));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(50);
        // finish loading
        downloadProgressChannel.onNext(new Pair<>(50, 100));
        downloadChannel.onNext(50);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
    }

    @Test
    public void initPlayActionAfterDownloading() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(50)).thenReturn(false);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 50;
            }
        };

        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.START);
        // downloading
        progressDownloadView.callOnClick();
        downloadProgressChannel.onNext(new Pair<>(50, 50));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(50);
        // ready
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(true);
        when(fileDownloaderManager.getTGFilePath(50)).thenReturn("123");
        when(fileDownloaderManager.getFilePath(50)).thenReturn("123");
        downloadProgressChannel.onNext(new Pair<>(50, 100));
        downloadChannel.onNext(50);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
        assertThat(progressDownloadView.playAction.getId()).isEqualTo(50);
        assertThat(progressDownloadView.playAction.getPath()).isEqualTo("123");
    }

    @Test
    public void playAndPause_ViewAndMediaManager() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "123";
            }

            @Override
            public String getFilePath() {
                return "123";
            }

            @Override
            public int getFileId() {
                return 50;
            }
        };
        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);

        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PLAY);
        assertThat(progressDownloadView.mediaManager.getCurrentIdFile()).isEqualTo(50);
        assertThat(progressDownloadView.mediaManager.isPaused()).isEqualTo(false);

        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PAUSE_PLAY);
        assertThat(progressDownloadView.mediaManager.getCurrentIdFile()).isEqualTo(50);
        assertThat(progressDownloadView.mediaManager.isPaused()).isEqualTo(true);

        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PLAY);
        assertThat(progressDownloadView.mediaManager.getCurrentIdFile()).isEqualTo(50);
        assertThat(progressDownloadView.mediaManager.isPaused()).isEqualTo(false);
    }

    @Test
    public void playChannel_test() {
        ProgressDownloadView progressDownloadView = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "123";
            }

            @Override
            public String getFilePath() {
                return "123";
            }

            @Override
            public int getFileId() {
                return 50;
            }
        };
        // null channel
        assertThat(progressDownloadView.playChannelSubscription).isNull();
        // setFileDownloader
        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
        // Play click
        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PLAY);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // media player play other file
        progressDownloadView.mediaManager.play("456", 60);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PAUSE_PLAY);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.mediaManager.getCurrentIdFile()).isEqualTo(60);
        // Play click again
        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PLAY);
        assertThat(progressDownloadView.playChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.mediaManager.getCurrentIdFile()).isEqualTo(50);
    }

}