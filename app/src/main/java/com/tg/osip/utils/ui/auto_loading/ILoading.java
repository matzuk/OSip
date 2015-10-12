package com.tg.osip.utils.ui.auto_loading;

import java.util.List;

import rx.Observable;

/**
 * @author e.matsyuk
 */
public interface ILoading<T> {

    Observable<List<T>> getLoadingObservable(OffsetAndLimit offsetAndLimit);

}
