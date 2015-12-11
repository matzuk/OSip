package com.tg.osip.tdclient;

import android.util.Log;

import com.tg.osip.utils.common.BackgroundExecutor;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.Observable;

/**
 * @author e.matsyuk
 */
public interface TGProxyI {

    <T extends TdApi.TLObject> Observable<T> sendTD(TdApi.TLFunction tlFunction, Class<T> clazz);

}
