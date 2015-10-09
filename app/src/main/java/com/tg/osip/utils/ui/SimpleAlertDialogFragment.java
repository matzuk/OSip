package com.tg.osip.utils.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;

/**
 * @author e.matsyuk
 */
public class SimpleAlertDialogFragment extends DialogFragment {

    public static final String TITLE_KEY = "title";
    public static final String MESSAGE_KEY = "message";

    public static SimpleAlertDialogFragment newInstance(String title, String message) {
        SimpleAlertDialogFragment frag = new SimpleAlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE_KEY);
        String message = getArguments().getString(MESSAGE_KEY);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(ApplicationSIP.applicationContext.getResources().getString(R.string.dialog_ok), null)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}