package com.yzx.chat.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by YZX on 2017年11月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class DateUtil {
    public static String msecToTime_HH_mm(long milliseconds){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format.format(new Date(milliseconds));
    }
}
