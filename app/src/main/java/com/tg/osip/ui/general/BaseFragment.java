package com.tg.osip.ui.general;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Base fragment for all fragments
 *
 * @author e.matsyuk
 */
public class BaseFragment extends Fragment {

    public AppCompatActivity getSupportActivity() {
        return ((AppCompatActivity)getActivity());
    }

}
