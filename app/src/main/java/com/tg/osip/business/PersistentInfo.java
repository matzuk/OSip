package com.tg.osip.business;

import android.content.Context;
import android.content.SharedPreferences;

import com.tg.osip.ApplicationSIP;

/**
 * Store or persistent info
 *
 * @author e.matsyuk
 */
public class PersistentInfo {

    private static final String PREFS_NAME = "persistentInfo";
    private static final String ME_USER_ID = "meUserId";
    private static final int DEFAULT_USER_ID = 0;

    private int meUserId;

    public PersistentInfo() {
        SharedPreferences preferences = getSharedPreferences();
        meUserId = preferences.getInt(ME_USER_ID, DEFAULT_USER_ID);
    }

    private static SharedPreferences getSharedPreferences() {
        return ApplicationSIP.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getMeUserId() {
        return meUserId;
    }

    public void setMeUserId(int meUserId) {
        this.meUserId = meUserId;
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(ME_USER_ID, meUserId);
        editor.apply();
    }

}
