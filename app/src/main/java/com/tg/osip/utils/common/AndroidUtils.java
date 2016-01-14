package com.tg.osip.utils.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.utils.log.Logger;

import java.io.File;
import java.util.Locale;

/**
 * @author e.matsyuk
 */
public class AndroidUtils {

    public static final int KEYBOARD_HIDDEN_TIME = 500;

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static void setDefaultRequestedOrientation(Activity activity) {
        if (!isTablet(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void showKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View currentFocus = activity.getWindow().getCurrentFocus();
            if (currentFocus == null) {
                currentFocus = activity.getWindow().getDecorView();
            }
            imm.showSoftInput(currentFocus, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static boolean isKeyboardMayShow(Activity activity) {
        if (activity == null) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.
                INPUT_METHOD_SERVICE);
        return !(imm == null || activity.getCurrentFocus() == null);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.
                INPUT_METHOD_SERVICE);
        if (imm == null || activity.getCurrentFocus() == null) {
            return;
        }
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        imm.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static int dp(float value) {
        float density = ApplicationSIP.applicationContext.getResources().getDisplayMetrics().density;
        return (int) Math.ceil(density * value);
    }

    public static int fromDp(float value) {
        float density = ApplicationSIP.applicationContext.getResources().getDisplayMetrics().density;
        return (int) Math.ceil(value / density);
    }

    public static boolean isRTL() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();
        if (lang == null) {
            lang = "en";
        }
        return lang.toLowerCase().equals("ar");
    }

    public static String getCacheDirPath() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {
            Logger.error(e);
        }
        if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
            try {
                File file = ApplicationSIP.applicationContext.getExternalCacheDir();
                if (file != null) {
                    return file.getPath();
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        try {
            File file = ApplicationSIP.applicationContext.getCacheDir();
            if (file != null) {
                return file.getPath();
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return new File("").getPath();
    }

    /**
     * 4.0.3 and lower - crash with preloader
     */
    public static boolean isNewAndroid() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    public static String getLettersForPlug(String firstName, String lastName) {
        String text = "";
        if (firstName != null && firstName.length() > 0) {
            text += firstName.substring(0, 1);
        }
        if (lastName != null && lastName.length() > 0) {
            String lastch = null;
            for (int a = lastName.length() - 1; a >= 0; a--) {
                if (lastch != null && lastName.charAt(a) == ' ') {
                    break;
                }
                lastch = lastName.substring(a, a + 1);
            }
            if (Build.VERSION.SDK_INT >= 16) {
                text += "\u200C" + lastch;
            } else {
                text += lastch;
            }
        } else if (firstName != null && firstName.length() > 0) {
            for (int a = firstName.length() - 1; a >= 0; a--) {
                if (firstName.charAt(a) == ' ') {
                    if (a != firstName.length() - 1 && firstName.charAt(a + 1) != ' ') {
                        if (Build.VERSION.SDK_INT >= 16) {
                            text += "\u200C" + firstName.substring(a + 1, a + 2);
                        } else {
                            text += firstName.substring(a + 1, a + 2);
                        }
                        break;
                    }
                }
            }
        }
        return text;
    }

}
