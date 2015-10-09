package com.tg.osip.utils.time;

import android.text.format.DateFormat;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author e.matsyuk
 */
public class TimeUtils {

    public static FastDateFormat formatterDay;
    public static FastDateFormat formatterWeek;
    public static FastDateFormat formatterMonth;
    public static FastDateFormat formatterYear;

    static {
        formatterMonth = createFormatter(Locale.getDefault(), ApplicationSIP.applicationContext.getResources().getString(R.string.formatterMonth), "dd MMM");
        formatterYear = createFormatter(Locale.getDefault(), ApplicationSIP.applicationContext.getResources().getString(R.string.formatterYear), "dd.MM.yy");
        formatterWeek = createFormatter(Locale.getDefault(), ApplicationSIP.applicationContext.getResources().getString(R.string.formatterWeek), "EEE");
        formatterDay = createFormatter(Locale.US, DateFormat.is24HourFormat(ApplicationSIP.applicationContext) ? ApplicationSIP.applicationContext.getResources().getString(R.string.formatterDay24H) : ApplicationSIP.applicationContext.getResources().getString(R.string.formatterDay12H), DateFormat.is24HourFormat(ApplicationSIP.applicationContext) ? "HH:mm" : "h:mm a");
    }

    private static FastDateFormat createFormatter(Locale locale, String format, String defaultFormat) {
        if (format == null || format.length() == 0) {
            format = defaultFormat;
        }
        FastDateFormat formatter = null;
        try {
            formatter = FastDateFormat.getInstance(format, locale);
        } catch (Exception e) {
            format = defaultFormat;
            formatter = FastDateFormat.getInstance(format, locale);
        }
        return formatter;
    }

    public static String stringForMessageListDate(long date) {
        Calendar rightNow = Calendar.getInstance();
        int day = rightNow.get(Calendar.DAY_OF_YEAR);
        int year = rightNow.get(Calendar.YEAR);
        rightNow.setTimeInMillis(date * 1000);
        int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
        int dateYear = rightNow.get(Calendar.YEAR);

        if (year != dateYear) {
            return formatterYear.format(new Date(date * 1000));
        } else {
            int dayDiff = dateDay - day;
            if(dayDiff == 0 || dayDiff == -1 && (int)(System.currentTimeMillis() / 1000) - date < 60 * 60 * 8) {
                return formatterDay.format(new Date(date * 1000));
            } else if(dayDiff > -7 && dayDiff <= -1) {
                return formatterWeek.format(new Date(date * 1000));
            } else {
                return formatterMonth.format(new Date(date * 1000));
            }
        }
    }

}
