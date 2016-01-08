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

    ProgressDownloadView progressDownloadView_ProgressUpdate;
    PublishSubject<Pair<Integer, Integer>> downloadProgressChannel;
    PublishSubject<Integer> downloadChannel;
    Activity activity;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        // Robolectic
        activity = Robolectric.setupActivity(Activity.class);

        progressDownloadView_ProgressUpdate = new ProgressDownloadView(activity, ProgressDownloadView.Type.AUDIO);
        downloadProgressChannel = PublishSubject.create();
        downloadChannel = PublishSubject.create();
        when(fileDownloaderManager.getDownloadProgressChannel()).thenReturn(downloadProgressChannel);
        when(fileDownloaderManager.getDownloadChannel()).thenReturn(downloadChannel);
        progressDownloadView_ProgressUpdate.setFileDownloaderManager(fileDownloaderManager);
    }

    @Test
    public void setDownloadingState_emptyFileDownloaderI() {
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
        progressDownloadView_ProgressUpdate.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.START);
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

        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.playAction.getId()).isEqualTo(100);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PLAY);
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

        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.playAction.getId()).isEqualTo(100);
        assertThat(progressDownloadView.viewState).isEqualTo(ProgressDownloadView.ViewState.PAUSE_PLAY);
    }

    @Test
    public void progressUpdatingTest_downloadingState() {
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

        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription).isNull();
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription).isNull();
        progressDownloadView_ProgressUpdate.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(20);

        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(20, 60));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(10, 85));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(85);

        downloadProgressChannel.onNext(new Pair<>(10, 100));
        downloadChannel.onNext(10);
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
    }

    @Test
    public void progressUpdatingTest_startState() {
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

        progressDownloadView_ProgressUpdate.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription).isNull();
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription).isNull();
        progressDownloadView_ProgressUpdate.callOnClick();
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(0);

        downloadProgressChannel.onNext(new Pair<>(20, 40));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(21, 60));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(20, 85));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(85);

        downloadProgressChannel.onNext(new Pair<>(20, 100));
        downloadChannel.onNext(20);
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
    }

    @Test
    public void progressUpdatingTest_someFileDownloaders() {
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(40)).thenReturn(false);
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

        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription).isNull();
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription).isNull();
        progressDownloadView_ProgressUpdate.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(20);

        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(40);

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
        progressDownloadView_ProgressUpdate.setFileDownloader(fileDownloaderI2);
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        progressDownloadView_ProgressUpdate.callOnClick();
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(0);

        downloadProgressChannel.onNext(new Pair<>(40, 100));
        downloadChannel.onNext(40);
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
    }

    @Test
    public void progressUpdatingTest_pauseState() {
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

        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription).isNull();
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription).isNull();
        progressDownloadView_ProgressUpdate.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.START);
        progressDownloadView_ProgressUpdate.callOnClick();
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(0);

        downloadProgressChannel.onNext(new Pair<>(50, 40));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(40);

        progressDownloadView_ProgressUpdate.callOnClick();
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(0);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.START);

        progressDownloadView_ProgressUpdate.callOnClick();
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.DOWNLOADING);
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(0);
        downloadProgressChannel.onNext(new Pair<>(50, 50));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(50);

        downloadProgressChannel.onNext(new Pair<>(50, 100));
        downloadChannel.onNext(50);
        assertThat(progressDownloadView_ProgressUpdate.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.downloadChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
    }

    @Test
    public void initPlayActionAfterDownloading() {
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

        progressDownloadView_ProgressUpdate.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.START);
        // downloading
        progressDownloadView_ProgressUpdate.callOnClick();
        downloadProgressChannel.onNext(new Pair<>(50, 50));
        assertThat(progressDownloadView_ProgressUpdate.progressBar.getProgress()).isEqualTo(50);

        // ready
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(true);
        when(fileDownloaderManager.getTGFilePath(50)).thenReturn("123");
        when(fileDownloaderManager.getFilePath(50)).thenReturn("123");
        downloadProgressChannel.onNext(new Pair<>(50, 100));
        downloadChannel.onNext(50);
        assertThat(progressDownloadView_ProgressUpdate.viewState).isEqualTo(ProgressDownloadView.ViewState.READY);
        assertThat(progressDownloadView_ProgressUpdate.playAction.getId()).isEqualTo(50);
        assertThat(progressDownloadView_ProgressUpdate.playAction.getPath()).isEqualTo("123");
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

}