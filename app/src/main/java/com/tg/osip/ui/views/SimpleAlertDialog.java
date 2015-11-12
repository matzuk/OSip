package com.tg.osip.ui.views;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;

/**
 * @author e.matsyuk
 */
public class SimpleAlertDialog {

//    private Dialog dialog;

    public static void show(final Activity activity, final String title, final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            Dialog dialog = onCreateDialog(activity, title, message);
            dialog.show();
        });

    }

    private static Dialog onCreateDialog(Activity activity, String title, String message) {
        Dialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(ApplicationSIP.applicationContext.getResources().getString(R.string.dialog_ok), null)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

}
