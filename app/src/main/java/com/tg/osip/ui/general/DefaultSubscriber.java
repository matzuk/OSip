package com.tg.osip.ui.general;

import com.tg.osip.utils.log.Logger;

import rx.Subscriber;

/**
 * @author e.matsyuk
 */
public abstract class DefaultSubscriber<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        Logger.error(e);
        // later add common error handling
    }

}
