package com.yzx.chat.tool;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by YZX on 2017年10月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class AndroidTool {
    private static int sScreenWidth;
    private static int sScreenHeight;
    private static int sScreenDensity;
    private static int sActivityStartedCount;
    private static String sTopActivityName;
    private static Context sApplicationContext;

    public synchronized static void init(Application application) {
        if (sApplicationContext != null) {
            return;
        }
        application.registerActivityLifecycleCallbacks(sLifecycleCallbacks);
        sApplicationContext = application.getApplicationContext();
        DisplayMetrics dm = application.getResources().getDisplayMetrics();
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = sApplicationContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = sApplicationContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean isAppForeground(){
        return sActivityStartedCount>0;
    }

    public static String getTopActivityName(){
        return sTopActivityName;
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
        return sApplicationContext.getString(resID);
    }

    @ColorInt
    public static int getColor(@ColorRes int resID) {
        return ContextCompat.getColor(sApplicationContext, resID);
    }

    public static float dip2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, sApplicationContext.getResources().getDisplayMetrics());
    }


    public static float px2dip(float pxValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pxValue, sApplicationContext.getResources().getDisplayMetrics());
    }

    public static float px2sp(float pxValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, pxValue, sApplicationContext.getResources().getDisplayMetrics());
    }


    public static int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, sApplicationContext.getResources().getDisplayMetrics());

    }



    private static Application.ActivityLifecycleCallbacks sLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            sActivityStartedCount++;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            sTopActivityName = activity.getClass().getSimpleName();
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            sActivityStartedCount--;
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
}
