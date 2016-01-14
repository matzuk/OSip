package com.tg.osip.ui.launcher_and_registration.country.utils;

/**
 * @author e.matsyuk
 */
public class Country {

    private String name;
    private String code;
    private String shortName;

    public Country(String name, String code, String shortName) {
        this.name = name;
        this.code = code;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getShortName() {
        return shortName;
    }

}
