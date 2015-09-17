/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.tg.osip.ui.launcher_and_registration.country.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.ui.launcher_and_registration.country.ui.DividerCell;
import com.tg.osip.ui.launcher_and_registration.country.ui.LetterSectionCell;
import com.tg.osip.ui.launcher_and_registration.country.ui.TextSettingsCell;
import com.tg.osip.ui.launcher_and_registration.country.utils.Country;
import com.tg.osip.ui.launcher_and_registration.country.utils.CountryUtils;
import com.tg.osip.utils.AndroidUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CountryAdapter extends BaseSectionsAdapter {

    private Context mContext;
    private LinkedHashMap<String, ArrayList<Country>> countries = new LinkedHashMap<>();
    private ArrayList<String> sortedCountries = new ArrayList<>();

    public CountryAdapter(Context context) {
        mContext = context;
        countries = CountryUtils.getSortedCountryMap();
        for (Map.Entry<String, ArrayList<Country>> entry : countries.entrySet()) {
            sortedCountries.add(entry.getKey());
        }
    }

    @Override
    public Country getItem(int section, int position) {
        if (section < 0 || section >= sortedCountries.size()) {
            return null;
        }
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        if (position < 0 || position >= arr.size()) {
            return null;
        }
        return arr.get(position);
    }

    @Override
    public boolean isRowEnabled(int section, int row) {
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        return row < arr.size();
    }

    @Override
    public int getSectionCount() {
        return sortedCountries.size();
    }

    @Override
    public int getCountForSection(int section) {
        int count = countries.get(sortedCountries.get(section)).size();
        if (section != sortedCountries.size() - 1) {
            count++;
        }
        return count;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new LetterSectionCell(mContext);
            ((LetterSectionCell) convertView).setCellHeight(AndroidUtils.dp(48));
        }
        ((LetterSectionCell) convertView).setLetter(sortedCountries.get(section).toUpperCase());
        return convertView;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(section, position);
        if (type == 1) {
            if (convertView == null) {
                convertView = new DividerCell(mContext);
                convertView.setPadding(AndroidUtils.dp(AndroidUtils.isRTL() ? 24 : 72), 0, AndroidUtils.dp(AndroidUtils.isRTL() ? 72 : 24), 0);
            }
        } else if (type == 0) {
            if (convertView == null) {
                convertView = new TextSettingsCell(mContext);
                convertView.setPadding(AndroidUtils.dp(AndroidUtils.isRTL() ? 16 : 54), 0, AndroidUtils.dp(AndroidUtils.isRTL() ? 54 : 16), 0);
            }

            ArrayList<Country> arr = countries.get(sortedCountries.get(section));
            Country c = arr.get(position);
            ((TextSettingsCell) convertView).setTextAndValue(c.getName(), "+" + c.getCode(), false);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int section, int position) {
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        return position < arr.size() ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
