package com.tg.osip.ui.launcher_and_registration.country.utils;

import com.tg.osip.ApplicationSIP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author e.matsyuk
 */
public class CountryUtils {

    public static List<Country> getCountryList() {
        List<Country> countryList = new ArrayList<>();
        try {
            InputStream stream = ApplicationSIP.applicationContext.getResources().getAssets().open("countries.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                String name = args[2];
                String code = args[0];
                String shortName = args[1];
                Country country = new Country(name, code, shortName);
                countryList.add(country);
            }
            reader.close();
            stream.close();
        } catch (Exception e) {
//            FileLog.e("tmessages", e); FIXME
        }

        return countryList;
    }

    /**
     *
     * @return Map, in which Key is first letter of Country, value - is list of countries with this concrete first letter in name
     */
    public static LinkedHashMap<String, ArrayList<Country>> getSortedCountryMap() {
        LinkedHashMap<String, ArrayList<Country>> countriesMap = new LinkedHashMap<>();

        List<Country> countryList = getCountryList();
        Collections.sort(countryList, new Comparator<Country>() {
            @Override
            public int compare(Country country, Country country2) {
                return country.getName().compareTo(country2.getName());
            }
        });

        for (Country country : countryList) {
            String firstLetter = country.getName().substring(0, 1).toUpperCase();
            ArrayList<Country> countryListWithConcreteFirstLetter = countriesMap.get(firstLetter);
            if (countryListWithConcreteFirstLetter == null) {
                countryListWithConcreteFirstLetter = new ArrayList<>();
                countriesMap.put(firstLetter, countryListWithConcreteFirstLetter);
            }
            countryListWithConcreteFirstLetter.add(country);
        }

        return countriesMap;
    }

    /**
     *
     * @return Map, in which Key is code of Country, value - is country with this concrete code
     */
    public static HashMap<String, Country> getCountryCodeMap() {
        HashMap<String, Country> countriesMap = new HashMap<>();

        List<Country> countryList = getCountryList();
        for (Country country : countryList) {
            countriesMap.put(country.getCode(), country);
        }

        return countriesMap;
    }

    /**
     *
     * @return Map, in which Key is short name of Country, value - is country with this concrete code
     */
    public static HashMap<String, Country> getCountryShortNameMap() {
        HashMap<String, Country> countriesMap = new HashMap<>();

        List<Country> countryList = getCountryList();
        for (Country country : countryList) {
            countriesMap.put(country.getShortName(), country);
        }

        return countriesMap;
    }

}
