package com.tg.osip.ui.launcher_and_registration;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.tg.osip.R;
import com.tg.osip.business.AuthManager;
import com.tg.osip.utils.common.AndroidUtils;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.ui.general.views.images.ScalableImageView;
import com.tg.osip.ui.general.views.TransitionTextView;

import rx.Subscriber;
import rx.Subscription;

/**
 * @author e.matsyuk
 */
public class CodeVerificationFragment extends Fragment {

    private Subscription channelSubscription;
    private EditText codeVerificationEdit;
    private ScalableImageView nextButton;
    private TransitionTextView resultTextView;
    private ScalableImageView verificationCodeImage;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_code_verification_registration, container, false);
        init(rootView);
        initToolbar(rootView);
        subscribeToChannel();
        return rootView;
    }

    private void init(View view) {
        if (AndroidUtils.isNewAndroid()) {
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
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
        showKeyboard();
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
        AuthManager.getInstance().setAuthCodeRequest(code);
    }

    private void subscribeToChannel() {
        channelSubscription = AuthManager.getInstance().getAuthChannel().subscribe(channelSubscriptionSubscriber);
    }

    private Subscriber<AuthManager.AuthStateEnum> channelSubscriptionSubscriber = new Subscriber<AuthManager.AuthStateEnum>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Logger.error(e);
            showError();

        }

        @Override
        public void onNext(AuthManager.AuthStateEnum authStateEnum) {
            Logger.debug(authStateEnum);
            if (authStateEnum == AuthManager.AuthStateEnum.AUTH_STATE_OK) {
                showSuccess();
            }
        }
    };

    private void showError() {
        resultTextView.setVisibility(View.GONE);
        verificationCodeImage.setVisibility(View.GONE);

        resultTextView.setText(getResources().getString(R.string.reg_verification_wrong_number));
        resultTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.color_registration_no));
        verificationCodeImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.register_no, null));

        resultTextView.setVisibility(View.VISIBLE);
        verificationCodeImage.setVisibility(View.VISIBLE);
    }

    private void showSuccess() {
        resultTextView.setVisibility(View.GONE);
        verificationCodeImage.setVisibility(View.GONE);

        resultTextView.setText(getResources().getString(R.string.reg_verification_right_number));
        resultTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.color_registration_yes));
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

    /**
     * hack method for showing keyboard in onResume
     */
    private void showKeyboard() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(() -> AndroidUtils.showKeyboard(getActivity()), 500);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (channelSubscription != null && !channelSubscription.isUnsubscribed()) {
            channelSubscription.unsubscribe();
        }
    }

}
