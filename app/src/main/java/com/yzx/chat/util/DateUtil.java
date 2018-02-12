package com.yzx.chat.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by YZX on 2017年11月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class DateUtil {
    public static String msecToTime_HH_mm(long milliseconds) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format.format(new Date(milliseconds));
    }

    public static String msecToDate_yyyy_MM_dd(long milliseconds) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format.format(new Date(milliseconds));
    }

    public static String isoToDate_yyyy_MM_dd(String isoTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        try {
            format.parse(isoTime);
            return isoTime.substring(0, 10);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Calendar isoToCalendar(String isoTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        Calendar calendar =  Calendar.getInstance();
        try {
            Date date = format.parse(isoTime);
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public static String msecToISO(long milliseconds){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        return format.format(new Date(milliseconds));
    }
}
