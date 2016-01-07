package com.tg.osip.ui.general.views;

import android.app.Activity;
import android.support.v4.util.Pair;

import com.tg.osip.RobolectricUnitTestRunner;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.general.views.progress_download.ProgressDownloadView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

import rx.subjects.PublishSubject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Unit tests with Robolectric
 *
 * @author e.matsyuk
 */
@RunWith(RobolectricUnitTestRunner.class)
public class ProgressDownloadViewTest2 {

    @Mock
    FileDownloaderManager fileDownloaderManager;

    ProgressDownloadView progressDownloadView;
    PublishSubject<Pair<Integer, Integer>> downloadProgressChannel;
    Activity activity;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        // Robolectic
        activity = Robolectric.setupActivity(Activity.class);

        progressDownloadView = new ProgressDownloadView(activity);
        downloadProgressChannel = PublishSubject.create();

        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(10)).thenReturn(true);
        when(fileDownloaderManager.isFileInProgress(20)).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(30)).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(40)).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(50)).thenReturn(false);

        when(fileDownloaderManager.getProgressValue(10)).thenReturn(20);

        when(fileDownloaderManager.getDownloadProgressChannel()).thenReturn(downloadProgressChannel);

        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);
    }

    @Test
    public void progressUpdatingTest_downloadingState() {
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };

        assertThat(progressDownloadView.downloadProgressChannelSubscription).isNull();
        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(20);

        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(20, 60));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(10, 85));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(85);

        downloadProgressChannel.onNext(new Pair<>(10, 100));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.PLAY);
    }

    @Test
    public void progressUpdatingTest_startState() {
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 20;
            }
        };

        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.downloadProgressChannelSubscription).isNull();
        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);

        downloadProgressChannel.onNext(new Pair<>(20, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(21, 60));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(20, 85));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(85);

        downloadProgressChannel.onNext(new Pair<>(20, 100));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.PLAY);
    }

    @Test
    public void progressUpdatingTest_someStates() {
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };

        assertThat(progressDownloadView.downloadProgressChannelSubscription).isNull();
        progressDownloadView.setFileDownloader(fileDownloaderI);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(20);

        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        FileDownloaderI fileDownloaderI2 = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 40;
            }
        };
        progressDownloadView.setFileDownloader(fileDownloaderI2);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);

        downloadProgressChannel.onNext(new Pair<>(40, 100));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.PLAY);
    }

    @Test
    public void progressUpdatingTest_pauseState() {

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 50;
            }
        };

        assertThat(progressDownloadView.downloadProgressChannelSubscription).isNull();
        progressDownloadView.setFileDownloader(fileDownloaderI);
        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);

        downloadProgressChannel.onNext(new Pair<>(50, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.START);

        progressDownloadView.callOnClick();
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.DOWNLOADING);
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);
        downloadProgressChannel.onNext(new Pair<>(50, 50));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(50);

        downloadProgressChannel.onNext(new Pair<>(50, 100));
        assertThat(progressDownloadView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.PLAY);
    }

}