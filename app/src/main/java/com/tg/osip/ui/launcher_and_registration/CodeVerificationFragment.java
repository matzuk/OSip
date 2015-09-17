package com.tg.osip.ui.launcher_and_registration;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.tg.osip.R;
import com.tg.osip.utils.AndroidUtils;
import com.tg.osip.utils.ui.PreLoader;
import com.tg.osip.utils.ui.ScalableImageView;
import com.tg.osip.utils.ui.TransitionTextView;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class CodeVerificationFragment extends Fragment {

    private EditText codeVerificationEdit;
    private ScalableImageView nextButton;
    private TransitionTextView resultTextView;
    private ScalableImageView verificationCodeImage;
    private PreLoader preLoader;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.verification_code_registration_fragment, container, false);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
        if (AndroidUtils.isNewAndroid()) {
            preLoader = (PreLoader) view.findViewById(R.id.pro_loader);
            preLoader.setVisibility(View.GONE);
        }
        resultTextView = (TransitionTextView)view.findViewById(R.id.verification_wrong_number);
        nextButton = (ScalableImageView)view.findViewById(R.id.next_button);
        verificationCodeImage = (ScalableImageView)view.findViewById(R.id.verification_code_image);
        nextButton.setOnClickListener(v -> onNextPressed());
        codeVerificationEdit = (EditText)view.findViewById(R.id.verification_code_edit);
        codeVerificationEdit.addTextChangedListener(codeEditTextWatcher);
        codeVerificationEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT) {
                onNextPressed();
                return true;
            }
            return false;
        });
    }

    private void initToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().show();
            getActivity().getActionBar().setTitle(getResources().getString(R.string.reg_verification_toolbar_title));
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void onNextPressed() {
        String code = codeVerificationEdit.getText().toString();
//        TGProxy.getInstance().getClientInstance().send(new TdApi.AuthSetCode(code), authGetResultHandler);
    }

//    private Client.ResultHandler authGetResultHandler = new Client.ResultHandler() {
//        @Override
//        public void onResult(TdApi.TLObject object) {
//            if (getActivity() == null) {
//                return;
//            }
//            if (object instanceof TdApi.AuthStateOk) {
//                long delay = AndroidUtils.KEYBOARD_HIDDEN_TIME;
//                long delayAnimation = 200;
//                handler.post(successUI);
//                if (AndroidUtils.isKeyboardMayShow(getActivity())) {
//                    delay += delayAnimation;
//                    handler.postDelayed(hideKeyboardUI, delayAnimation);
//                }
//                handler.postDelayed(chatLoadingUI, delay);
//            } else if (object instanceof TdApi.Error) {
//                handler.post(errorUI);
//            }
//        }
//    };

//    private Runnable chatLoadingUI = new Runnable() {
//        @Override
//        public void run() {
//            FrameManager.getInstance().removePreviousStack();
//            chatListFragment = new ChatListFragment(getActivity(), firstChatLoadingListener);
//            if (AndroidUtils.isNewAndroid()) {
//                preLoader.setVisibility(View.VISIBLE);
//            }
//        }
//    };

    private Runnable hideKeyboardUI = new Runnable() {
        @Override
        public void run() {
            AndroidUtils.hideKeyboard(getActivity());
        }
    };

    private Runnable errorUI = new Runnable() {
        @Override
        public void run() {
            showError();
        }
    };

    private void showError() {
        resultTextView.setVisibility(View.GONE);
        verificationCodeImage.setVisibility(View.GONE);

        resultTextView.setText(getResources().getString(R.string.reg_verification_wrong_number));
        resultTextView.setTextColor(getResources().getColor(R.color.registration_no_color));
        verificationCodeImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.register_no, null));

        resultTextView.setVisibility(View.VISIBLE);
        verificationCodeImage.setVisibility(View.VISIBLE);
    }

    private Runnable successUI = new Runnable() {
        @Override
        public void run() {
            showSuccess();
        }
    };

    private void showSuccess() {
        resultTextView.setVisibility(View.GONE);
        verificationCodeImage.setVisibility(View.GONE);

        resultTextView.setText(getResources().getString(R.string.reg_verification_right_number));
        resultTextView.setTextColor(getResources().getColor(R.color.registration_yes_color));
        verificationCodeImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.register_yes, null));

        resultTextView.setVisibility(View.VISIBLE);
        verificationCodeImage.setVisibility(View.VISIBLE);
    }

    private TextWatcher codeEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(codeVerificationEdit.getText())) {
                nextButton.setVisibility(View.GONE);
            } else {
                nextButton.setVisibility(View.VISIBLE);
            }
        }
    };

//    private FirstChatLoadingListener firstChatLoadingListener = new FirstChatLoadingListener() {
//        @Override
//        public void done() {
//            handler.post(startChatList);
//        }
//    };
//
//    private Runnable startChatList = new Runnable() {
//        @Override
//        public void run() {
//            if (AndroidUtils.isNewAndroid()) {
//                preLoader.setVisibility(View.GONE);
//            }
//            FrameManager.getInstance().putBaseFragment(chatListFragment, FrameManager.Animation.PUT_BOTTOM_TOP_ANIMATION, true);
//            chatListFragment = null;
//        }
//    };

}
