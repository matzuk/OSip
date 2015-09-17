package com.tg.osip.ui.launcher_and_registration;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import com.tg.osip.R;
import com.tg.osip.utils.ui.ScalableImageView;
import com.tg.osip.utils.ui.SimpleAlertDialog;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class NameRegistrationFragment extends Fragment {

    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private ScalableImageView nextButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.name_registration_fragment, container, false);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
        nextButton = (ScalableImageView)view.findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> onNextPressed());
        firstNameEdit = (EditText)view.findViewById(R.id.first_name_edit);
        firstNameEdit.addTextChangedListener(nameEditTextWatcher);
        firstNameEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT) {
                lastNameEdit.requestFocus();
                return true;
            }
            return false;
        });

        lastNameEdit = (EditText)view.findViewById(R.id.last_name_edit);
        lastNameEdit.addTextChangedListener(nameEditTextWatcher);
        lastNameEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT) {
                onNextPressed();
                return true;
            }
            return false;
        });
    }

    private void initToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        getActivity().setActionBar(toolbar);

        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().show();
            getActivity().getActionBar().setTitle(getResources().getString(R.string.reg_name_toolbar_title));
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onNextPressed() {
        String firstName = firstNameEdit.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            SimpleAlertDialog.show(
                    getActivity(),
                    getResources().getString(R.string.app_name),
                    getResources().getString(R.string.reg_enter_first_name)
            );
            return;
        }
        String lastName = lastNameEdit.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            SimpleAlertDialog.show(
                    getActivity(),
                    getResources().getString(R.string.app_name),
                    getResources().getString(R.string.reg_enter_last_name)
            );
            return;
        }
//        TGProxy.getInstance().getClientInstance().send(new TdApi.AuthSetName(firstName, lastName), authGetResultHandler);
    }

    private TextWatcher nameEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(firstNameEdit.getText()) || TextUtils.isEmpty(lastNameEdit.getText())) {
                nextButton.setVisibility(View.GONE);
            } else {
                nextButton.setVisibility(View.VISIBLE);
            }
        }
    };

//    private Client.ResultHandler authGetResultHandler = new Client.ResultHandler() {
//        @Override
//        public void onResult(TdApi.TLObject object) {
//            if (getActivity() == null) {
//                return;
//            }
//            if (object instanceof TdApi.AuthStateWaitSetCode ) {
//                goToCodeVerificationFragment();
//            } else if (object instanceof TdApi.Error) {
//                String message = "ERROR \ncode:" + ((TdApi.Error)object).code + "\ntext:" + ((TdApi.Error)object).text;
//                SimpleAlertDialog.show(
//                        getActivity(),
//                        getResources().getString(R.string.app_name),
//                        message
//                );
//            }
//        }
//    };
//
//    private void goToCodeVerificationFragment() {
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                AndroidUtils.hideKeyboard(getActivity());
//            }
//        });
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CodeVerificationFragment codeVerificationFragment = new CodeVerificationFragment(getActivity());
//                FrameManager.getInstance().putBaseFragment(codeVerificationFragment, FrameManager.Animation.PUT_RIGHT_LEFT_ANIMATION, true);
//            }
//        }, AndroidUtils.KEYBOARD_HIDDEN_TIME);
//    }

}