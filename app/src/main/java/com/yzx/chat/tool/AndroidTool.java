package com.yzx.chat.tool;

import android.app.Application;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

/**
 * Created by YZX on 2017年10月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class AndroidTool {
    private static int sScreenWidth;
    private static int sScreenHeight;
    private static int sScreenDensity;
    private static Context sApplicationContext;

    public synchronized static void init(Application context) {
        if(sApplicationContext!=null){
            return;
        }
        sApplicationContext = context;

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;
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

    public static String getString(@StringRes int resID){
        return sApplicationContext.getString(resID);
    }

    @ColorInt
    public static int getColor(@ColorRes int resID){
        return ContextCompat.getColor(sApplicationContext,resID);
    }
}
