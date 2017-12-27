package com.yzx.chat.util;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import com.yzx.chat.configure.AppApplication;

/**
 * Created by YZX on 2017年10月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class AndroidUtil {

    private static int sScreenWidth;
    private static int sScreenHeight;
    private static int sScreenDensity;
    private static Context sAppContext;

    static {
        sAppContext = AppApplication.getAppContext();
        DisplayMetrics dm = sAppContext.getResources().getDisplayMetrics();
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;
        sScreenDensity = dm.densityDpi;
    }

    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = sAppContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = sAppContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public static int getScreenWidth() {
        return sScreenWidth;
    }

    public static int getScreenHeight() {
        return sScreenHeight;
    }

    public static int getScreenDensity() {
        return sScreenDensity;
    }

    public static String getString(@StringRes int resID) {
        return sAppContext.getString(resID);
    }

    @ColorInt
    public static int getColor(@ColorRes int resID) {
        return ContextCompat.getColor(sAppContext, resID);
    }

    public static Drawable getDrawable(@DrawableRes int resID) {
        return ContextCompat.getDrawable(sAppContext, resID);
    }


    public static float dip2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, sAppContext.getResources().getDisplayMetrics());
    }


    public static float px2dip(float pxValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pxValue, sAppContext.getResources().getDisplayMetrics());
    }

    public static float px2sp(float pxValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, pxValue, sAppContext.getResources().getDisplayMetrics());
    }


    public static int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, sAppContext.getResources().getDisplayMetrics());

    }

    public static void showToast(String content) {
        Toast.makeText(sAppContext, content, Toast.LENGTH_LONG).show();
    }

    public static void showToast(@StringRes int resID) {
        Toast.makeText(sAppContext, sAppContext.getString(resID), Toast.LENGTH_LONG).show();
    }

}
