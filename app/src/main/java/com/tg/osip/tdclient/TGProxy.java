package com.tg.osip.tdclient;

import com.tg.osip.business.update_managers.UpdateManager;
import com.tg.osip.tdclient.exceptions.TdApiClassCastException;
import com.tg.osip.tdclient.exceptions.TdApiErrorException;
import com.tg.osip.utils.common.AndroidUtils;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;

import rx.Observable;
import rx.Subscriber;

/**
 * Proxy-singleton with RxJava for {@link org.drinkless.td.libcore.telegram.TG TG}
 *
 * @author e.matsyuk
 */
public class TGProxy {

    private static volatile TGProxy instance;

    /**
     * this method is necessary, because setDir and setUpdatesHandler for TG in first calling
     * @return TG.getClientInstance()
     */
    public static TGProxy getInstance() {
        if (instance == null) {
            synchronized (TGProxy.class) {
                if (instance == null) {
                    instance = new TGProxy();
                }
            }
        }
        return instance;
    }

    private TGProxy() {
        TG.setDir(AndroidUtils.getCacheDirPath());
        TG.setUpdatesHandler(updatesHandler);
    }

    private Client.ResultHandler updatesHandler = object -> {
        Class objectClass = object.getClass();
        if (objectClass == TdApi.Update.class || TdApi.Update.class.isAssignableFrom(objectClass)) {
            TdApi.Update update = (TdApi.Update)object;
            UpdateManager.getInstance().sendUpdateEvent(update);
        } else {
            Logger.error("Incorrect update object class. Not TdApi.Update.");
        }
    };

    public Client getClientInstance() {
        return TG.getClientInstance();
    }

    public <T extends TdApi.TLObject> Observable<T> sendTD(TdApi.TLFunction tlFunction, Class<T> clazz) {
        return BackgroundExecutor.createSafeBackgroundObservable(subscriber -> {
            try {
                TGProxy.getInstance().getClientInstance().send(tlFunction, object -> resultHandling(subscriber, object, clazz));
            } catch (Exception exception) {
                subscriber.onError(exception);
            }
        });
    }

    private <T> void resultHandling(Subscriber<? super T> subscriber, TdApi.TLObject object, Class<T> clazz) {
        if (subscriber != null && !subscriber.isUnsubscribed()) {
            if (object.getClass() == TdApi.Error.class) {
                TdApi.Error error = (TdApi.Error)object;
                subscriber.onError(new TdApiErrorException(error));
            } else {
                resultHandlingClassCast(subscriber, object, clazz);
            }
        }
    }

    private <T> void resultHandlingClassCast(Subscriber<? super T> subscriber, TdApi.TLObject object, Class<T> clazz) {
        T t;
        try {
            t = clazz.cast(object);
            subscriber.onNext(t);
            subscriber.onCompleted();
        } catch(ClassCastException e) {
            subscriber.onError(new TdApiClassCastException(e));
        }
    }

}
