package com.tg.osip.utils.ui.auto_loaded_views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.tg.osip.utils.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * @author e.matsyuk
 */
public class AutoLoadedRecyclerView<T> extends RecyclerView {

    private static  final int START_OFFSET = 0;

    private PublishSubject<OffsetAndLimit> scrollLoadingChannel = PublishSubject.create();
    private int onceDownloadsLimit;
    private ILoading<T> iLoading;
    private AutoLoadedAdapter<T> autoLoadedAdapter;

    public AutoLoadedRecyclerView(Context context) {
        super(context);
        init();
    }

    public AutoLoadedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoLoadedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * required method
     * call after init all parameters in AutoLoadedRecyclerView
     */
    public void startLoading() {
        Logger.debug("start loading");
        OffsetAndLimit offsetAndLimit = new OffsetAndLimit(START_OFFSET, getOnceDownloadsLimit());
        loadNewItems(offsetAndLimit);
    }

    private void init() {
        startScrollingChannel();
    }

    private void startScrollingChannel() {
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int position = getSupportLinearLayoutManager().findLastVisibleItemPosition();
                int limit = getOnceDownloadsLimit();
                int updatePosition = getAdapter().getItemCount() - 1 - (limit / 2);
                if (position >= updatePosition) {
                    int offset = getAdapter().getItemCount() - 1;
                    OffsetAndLimit offsetAndLimit = new OffsetAndLimit(offset, limit);
                    Logger.debug("scroll update event in channel");
                    Logger.debug("offsetAndLimit: " + offsetAndLimit.toString());
                    scrollLoadingChannel.onNext(offsetAndLimit);
                }
            }
        });
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        // FIXME temp restriction
        // later add handling for StaggeredGridLayoutManager
        if (layout instanceof StaggeredGridLayoutManager) {
            throw new AutoLoadedRecyclerViewExceptions("Incorrect LayoutManager. Please set LinearLayoutManager!");
        }
        super.setLayoutManager(layout);
    }

    private LinearLayoutManager getSupportLinearLayoutManager() {
        return (LinearLayoutManager)getLayoutManager();
    }

    public int getOnceDownloadsLimit() {
        if (onceDownloadsLimit <= 0) {
            throw new AutoLoadedRecyclerViewExceptions("onceDownloadsLimit must be initialised! And onceDownloadsLimit must be more than zero!");
        }
        return onceDownloadsLimit;
    }

    /**
     * required method
     */
    public void setOnceDownloadsLimit(int onceDownloadsLimit) {
        this.onceDownloadsLimit = onceDownloadsLimit;
    }

    @Deprecated
    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof AutoLoadedAdapter) {
            super.setAdapter(adapter);
        } else {
            throw new AutoLoadedRecyclerViewExceptions("Adapter must be implement IAutoLoadedAdapter");
        }
    }

    /**
     * required method
     */
    public void setAdapter(AutoLoadedAdapter<T> autoLoadedAdapter) {
        if (autoLoadedAdapter == null) {
            throw new AutoLoadedRecyclerViewExceptions("Null adapter. Please initialise adapter!");
        }
        this.autoLoadedAdapter = autoLoadedAdapter;
        super.setAdapter(autoLoadedAdapter);
    }

    public AutoLoadedAdapter<T> getAdapter() {
        if (autoLoadedAdapter == null) {
            throw new AutoLoadedRecyclerViewExceptions("Null adapter. Please initialise adapter!");
        }
        return autoLoadedAdapter;
    }

    public void setLoadingObservable(ILoading<T> iLoading) {
        this.iLoading = iLoading;
    }

    public ILoading<T> getLoadingObservable() {
        if (iLoading == null) {
            throw new AutoLoadedRecyclerViewExceptions("Null LoadingObservable. Please initialise LoadingObservable!");
        }
        return iLoading;
    }

    private void subscribeToLoadingChannel() {
        Logger.debug("call method");
        Subscriber<OffsetAndLimit> subscriber = new Subscriber<OffsetAndLimit>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Logger.error(e);
            }

            @Override
            public void onNext(OffsetAndLimit offsetAndLimit) {
                Logger.debug("scroll update event in channel Subscriber");
                unsubscribe();
                loadNewItems(offsetAndLimit);
            }
        };
        scrollLoadingChannel
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    private void loadNewItems(OffsetAndLimit offsetAndLimit) {
        Logger.debug("call method");
        Subscriber<List<T>> subscriber = new Subscriber<List<T>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Logger.error(e);
                subscribeToLoadingChannel();
            }

            @Override
            public void onNext(List<T> ts) {
                Logger.debug("loaded new items");
                getAdapter().addNewItems(ts);
                getAdapter().notifyItemInserted(getAdapter().getItemCount() - ts.size());
                if (ts.size() > 0) {
                    subscribeToLoadingChannel();
                }
            }
        };

        getLoadingObservable().getLoadingObservable(offsetAndLimit)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

}
