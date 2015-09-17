package com.tg.osip.ui.launcher_and_registration.country.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.tg.osip.R;
import com.tg.osip.ui.launcher_and_registration.country.adapters.CountryAdapter;
import com.tg.osip.ui.launcher_and_registration.country.utils.Country;
import com.tg.osip.utils.AndroidUtils;

/**
 * @author e.matsyuk
 */
public class CountryRegistrationFragment extends Fragment {

    private LetterSectionsListView listView;
    private CountryAdapter listViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.phone_registration_fragment, container, false);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
        listView = (LetterSectionsListView) view.findViewById(R.id.country_list_view);
        listViewAdapter = new CountryAdapter(getContext());
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(onItemClickListener);
        AndroidUtils.hideKeyboard(getActivity());
    }

    private void initToolbar(View rootView) {
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        if (getActivity() != null && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.reg_country_toolbar_title));
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Country country;
            int section = listViewAdapter.getSectionForPosition(i);
            int row = listViewAdapter.getPositionInSectionForPosition(i);
            if (row < 0 || section < 0) {
                return;
            }
            country = listViewAdapter.getItem(section, row);
            if (i < 0) {
                return;
            }
            PhoneRegistrationFragment phoneRegistrationFragment = (PhoneRegistrationFragment) FrameManager.getInstance().getLastFrameLayout();
            if (phoneRegistrationFragment != null) {
                phoneRegistrationFragment.setSelectedCountryFromCountryFragment(country);
            }
            finishFragment();
        }
    };

    public void finishFragment() {
        FrameManager.getInstance().removeTopBaseFragment();
//        if (getActivity() != null) {
//            getFragmentManager().popBackStack();
//        }
    }

}
