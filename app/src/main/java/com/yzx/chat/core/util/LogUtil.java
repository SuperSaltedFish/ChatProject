package com.yzx.chat.core.util;

import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class LogUtil {

    private static boolean isEnableV = true;
    private static boolean isEnableD = true;
    private static boolean isEnableI = true;
    private static boolean isEnableW = true;
    private static boolean isEnableE = true;
    private static boolean isEnableWTF = true;


    private static String generateTag() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
        return String.format(Locale.getDefault(),"%s.%s(line:%d)",
                stackTraceElement.getClassName(),
                stackTraceElement.getMethodName(),
                stackTraceElement.getLineNumber());
    }

    public static void v(String msg) {
        if(msg==null){
            msg="null";
        }
        if (isEnableV) {
            String tag = generateTag();
            Log.v(tag, msg);
        }
    }

    public static void v(Number number) {
        if (isEnableE) {
            String tag = generateTag();
            Log.e(tag, number==null?"null":number.toString());
        }
    }

    public static void v(String msg, Throwable tr) {
        if(msg==null){
            msg="null";
        }
        if (isEnableV) {
            String tag = generateTag();
            Log.v(tag, msg, tr);
        }
    }

    public static void d(String msg) {
        if(msg==null){
            msg="null";
        }
        if (isEnableD) {
            String tag = generateTag();
            Log.d(tag, msg);
        }
    }

    public static void d(Number number) {
        if (isEnableE) {
            String tag = generateTag();
            Log.e(tag, number==null?"null":number.toString());
        }
    }

    public static void d(String msg, Throwable tr) {
        if(msg==null){
            msg="null";
        }
        if (isEnableD) {
            String tag = generateTag();
            Log.d(tag, msg, tr);
        }
    }

    public static void i(String msg) {
        if(msg==null){
            msg="null";
        }
        if (isEnableI) {
            String tag = generateTag();
            Log.i(tag, msg);
        }
    }

    public static void i(Number number) {
        if (isEnableE) {
            String tag = generateTag();
            Log.e(tag, number==null?"null":number.toString());
        }
    }

    public static void i(String msg, Throwable tr) {
        if(msg==null){
            msg="null";
        }
        if (isEnableI) {
            String tag = generateTag();
            Log.i(tag, msg, tr);
        }
    }

    public static void w(String msg) {
        if(msg==null){
            msg="null";
        }
        if (isEnableW) {
            String tag = generateTag();
            Log.w(tag, msg);
        }
    }

    public static void w(Number number) {
        if (isEnableE) {
            String tag = generateTag();
            Log.e(tag, number==null?"null":number.toString());
        }
    }

    public static void w(String msg, Throwable tr) {
        if(msg==null){
            msg="null";
        }
        if (isEnableW) {
            String tag = generateTag();
            Log.w(tag, msg, tr);
        }
    }

    public static void e(String msg) {
        if(msg==null){
            msg="null";
        }
        if (isEnableE) {
            String tag = generateTag();
            Log.e(tag, msg);
        }
    }

    public static void e(Number number) {
        if (isEnableE) {
            String tag = generateTag();
            Log.e(tag, number==null?"null":number.toString());
        }
    }

    public static void e(String msg, Throwable tr) {
        if(msg==null){
            msg="null";
        }
        if (isEnableE) {
            String tag = generateTag();
            Log.e(tag, msg, tr);
        }
    }

    public static void wtf(String msg) {
        if(msg==null){
            msg="null";
        }
        if (isEnableWTF) {
            String tag = generateTag();
            Log.wtf(tag, msg);
        }
    }

    public static void wtf(String msg, Throwable tr) {
        if(msg==null){
            msg="null";
        }
        if (isEnableWTF) {
            String tag = generateTag();
            Log.wtf(tag, msg, tr);
        }
    }
}
