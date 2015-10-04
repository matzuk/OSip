package com.tg.osip.ui.launcher_and_registration;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.tg.osip.R;
import com.tg.osip.business.AuthManager;
import com.tg.osip.ui.launcher_and_registration.country.ui.CountryRegistrationFragment;
import com.tg.osip.ui.launcher_and_registration.country.utils.Country;
import com.tg.osip.ui.launcher_and_registration.country.utils.CountryUtils;
import com.tg.osip.utils.AndroidUtils;
import com.tg.osip.utils.PhoneFormat.PhoneFormat;
import com.tg.osip.utils.ui.ScalableImageView;
import com.tg.osip.utils.ui.SimpleAlertDialog;

import java.util.HashMap;


/**
 * @author e.matsyuk
 */
public class PhoneRegistrationFragment extends Fragment {

    private EditText phoneEdit;
    private EditText countryEdit;
    private EditText codeEdit;
    private ScalableImageView nextButton;

    private Country selectedCountryFromCountryFragment;
    private HashMap<String, Country> countryCodeMap;

    // prevent stackoverflow exception in TextWatcher in codeEdit
    private boolean ignoreOnTextChange;
    // prevent stackoverflow exception in TextWatcher in phoneEdit
    private boolean ignoreOnPhoneChange;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_phone_registration, container, false);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void initToolbar(View rootView) {
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().show();
            getActivity().getActionBar().setTitle(getResources().getString(R.string.reg_phone_toolbar_title));
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void init(View view) {
        nextButton = (ScalableImageView)view.findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> onNextPressed());

        countryCodeMap = CountryUtils.getCountryCodeMap();

        phoneEdit = (EditText)view.findViewById(R.id.phone_reg_phone_edit);
        phoneEdit.addTextChangedListener(phoneEditTextWatcher);
        phoneEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT) {
                onNextPressed();
                return true;
            }
            return false;
        });
        phoneEdit.requestFocus();

        countryEdit = (EditText)view.findViewById(R.id.phone_reg_country_edit);
        countryEdit.setOnFocusChangeListener((v, hasFocus) -> goToCountryFragment());
        countryEdit.setOnClickListener(v -> goToCountryFragment());

        codeEdit = (EditText)view.findViewById(R.id.phone_reg_code_edit);
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(4);
        codeEdit.setFilters(inputFilters);
        codeEdit.addTextChangedListener(codeEditTextWatcher);
        codeEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT) {
                phoneEdit.requestFocus();
                return true;
            }
            return false;
        });

        setValuesFromSIM();
        showKeyboard();

    }

    @Override
    public void onResume() {
        super.onResume();
        setSelectedCountryFromCountryFragment(selectedCountryFromCountryFragment);
    }

    private TextWatcher phoneEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (ignoreOnPhoneChange) {
                return;
            }
            if (count == 1 && after == 0 && s.length() > 1) {
                String phoneChars = "0123456789";
                String str = s.toString();
                String substr = str.substring(start, start + 1);
                if (!phoneChars.contains(substr)) {
                    ignoreOnPhoneChange = true;
                    StringBuilder builder = new StringBuilder(str);
                    int toDelete = 0;
                    for (int a = start; a >= 0; a--) {
                        substr = str.substring(a, a + 1);
                        if (phoneChars.contains(substr)) {
                            break;
                        }
                        toDelete++;
                    }
                    builder.delete(Math.max(0, start - toDelete), start + 1);
                    str = builder.toString();
                    if (PhoneFormat.strip(str).length() == 0) {
                        phoneEdit.setText("");
                    } else {
                        phoneEdit.setText(str);
                        updatePhoneField();
                    }
                    ignoreOnPhoneChange = false;
                }
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if (ignoreOnPhoneChange) {
                return;
            }
            updatePhoneField();

            String phoneNumber = "+" + codeEdit.getText().toString() + phoneEdit.getText().toString();
            if (PhoneFormat.getInstance().isPhoneNumberValid(phoneNumber)) {
                nextButton.setVisibility(View.VISIBLE);
            } else {
                nextButton.setVisibility(View.GONE);
            }
        }
    };

    private void updatePhoneField() {
        ignoreOnPhoneChange = true;
        try {
            String codeText = codeEdit.getText().toString();
            String phone = PhoneFormat.getInstance().format("+" + codeText + phoneEdit.getText().toString());
            int idx = phone.indexOf(" ");
            if (idx != -1) {
                String resultCode = PhoneFormat.stripExceptNumbers(phone.substring(0, idx));
                if (!codeText.equals(resultCode)) {
                    phone = PhoneFormat.getInstance().format(phoneEdit.getText().toString()).trim();
                    phoneEdit.setText(phone);
                    phoneEdit.setSelection(phoneEdit.length());
                } else {
                    phoneEdit.setText(phone.substring(idx).trim());
                    phoneEdit.setSelection(phoneEdit.length());
                }
            } else {
                phoneEdit.setSelection(phoneEdit.length());
            }
        } catch (Exception e) {
//            FileLog.e("tmessages", e); FIXME
        }
        ignoreOnPhoneChange = false;
    }

    public void onNextPressed() {
        String countryCode = codeEdit.getText().toString();
        if (TextUtils.isEmpty(countryCode)) {
            SimpleAlertDialog.show(
                    getActivity(),
                    getContext().getString(R.string.app_name),
                    getResources().getString(R.string.reg_phone_choose_country)
            );
            return;
        }
        if (!countryCodeMap.containsKey(countryCode)) {
            SimpleAlertDialog.show(
                    getActivity(),
                    getContext().getString(R.string.app_name),
                    getResources().getString(R.string.reg_phone_wrong_country)
            );
            return;
        }
        String phoneNumber = "+" + codeEdit.getText().toString() + phoneEdit.getText().toString();
        if (!PhoneFormat.getInstance().isPhoneNumberValid(phoneNumber)) {
            SimpleAlertDialog.show(
                    getActivity(),
                    getContext().getString(R.string.app_name),
                    getResources().getString(R.string.dialog_invalid_phone)
            );
            return;
        }
        AuthManager.getInstance().setAuthPhoneNumberRequest(phoneNumber);
    }

    private TextWatcher codeEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

        @Override
        public void afterTextChanged(Editable editable) {
            if (ignoreOnTextChange) {
                ignoreOnTextChange = false;
                return;
            }
            ignoreOnTextChange = true;
            String text = PhoneFormat.stripExceptNumbers(codeEdit.getText().toString());
            codeEdit.setText(text);
            if (text.length() == 0) {
                countryEdit.setText(getResources().getString(R.string.reg_phone_choose_country));
            } else {
                Country country = countryCodeMap.get(text);
                if (country != null) {
                    countryEdit.setText(country.getName());
                    updatePhoneField();
                } else {
                    countryEdit.setText(getResources().getString(R.string.reg_phone_wrong_country));
                }
            }
            codeEdit.setSelection(codeEdit.getText().length());
        }
    };

    private void setValuesFromSIM() {
        String countryShortName = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                countryShortName = telephonyManager.getSimCountryIso().toUpperCase();
            }
        } catch (Exception e) {
//            FileLog.e("tmessages", e); FIXME
        }

        if (TextUtils.isEmpty(countryShortName)) {
            return;
        }

        HashMap<String, Country> countryShortNameMap = CountryUtils.getCountryShortNameMap();

        String countryName = countryShortNameMap.get(countryShortName).getName();
        String countryCode = countryShortNameMap.get(countryShortName).getCode();

        countryEdit.setText(countryName);
        codeEdit.setText(countryCode);

    }

    /**
     * hack method for showing keyboard in onResume
     */
    private void showKeyboard() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(() -> AndroidUtils.showKeyboard(getActivity()), 500);
    }

    private void goToCountryFragment() {
        AndroidUtils.hideKeyboard(getActivity());
        CountryRegistrationFragment countryRegistrationFragment = new CountryRegistrationFragment();
        countryRegistrationFragment.setTargetFragment(this, 0);
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, countryRegistrationFragment)
                .commit();

    }

    public void setEditsFromSelectedCountry() {
        if (selectedCountryFromCountryFragment == null) {
            return;
        }
        countryEdit.setText(selectedCountryFromCountryFragment.getName());
        codeEdit.setText(selectedCountryFromCountryFragment.getCode());
    }

    public void setSelectedCountryFromCountryFragment(Country selectedCountryFromCountryFragment) {
        this.selectedCountryFromCountryFragment = selectedCountryFromCountryFragment;
        setEditsFromSelectedCountry();
    }

}
