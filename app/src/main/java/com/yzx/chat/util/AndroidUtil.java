package com.yzx.chat.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleableRes;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yzx.chat.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by YZX on 2017年10月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class AndroidUtil {

    private static Context sAppContext;

    private static int sScreenWidth;
    private static int sScreenHeight;
    private static int sScreenDensity;

    private static int sActivityStartedCount;
    private static int sActivityLiveCount;

    private static Stack<Class<? extends Activity>> sActivityStack;
    private static Map<Class<? extends Activity>, Activity> sActivityInstanceMap;

    public synchronized static void init(Application application) {
        sAppContext = application.getApplicationContext();
        sActivityStack = new Stack<>();
        sActivityInstanceMap = new HashMap<>();

        DisplayMetrics dm = sAppContext.getResources().getDisplayMetrics();
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;
        sScreenDensity = dm.densityDpi;

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                sActivityLiveCount++;
                sActivityStack.push(activity.getClass());
                sActivityInstanceMap.put(activity.getClass(), activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                sActivityStartedCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

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
                sActivityLiveCount--;
                if (sActivityLiveCount == 0) {
                    NetworkAsyncTask.cleanAllTask();
                }
                sActivityStack.remove(activity.getClass());
                sActivityInstanceMap.remove(activity.getClass());
            }
        });
    }

    public static boolean isAppForeground() {
        return sActivityStartedCount > 0;
    }

    public static Class<? extends Activity> getStackTopActivityClass() {
        return sActivityStack.peek();
    }

    public static Activity getStackTopActivityInstance() {
        return sActivityInstanceMap.get(sActivityStack.peek());
    }

    public static boolean isExistInActivityStack(Class<? extends Activity> activityClass) {
        return sActivityStack.search(activityClass) >= 0;
    }

    @Nullable
    public static <T extends Activity> T getLaunchActivity(Class<T> activityClass) {
        return (T) sActivityInstanceMap.get(activityClass);
    }

    public static void finishActivityInStackAbove(Class<? extends Activity> activityClass) {
        int index = sActivityStack.indexOf(activityClass);
        if (index < 0) {
            return;
        }
        for (int i = sActivityStack.size() - 1; i > index; i--) {
            activityClass = sActivityStack.get(i);
            Activity instance = sActivityInstanceMap.get(activityClass);
            if (instance != null) {
                instance.finish();
            }
        }
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

    public static TypedArray obtainStyledAttributes(@StyleableRes int[] resID) {
        return sAppContext.obtainStyledAttributes(resID);
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
        Toast toast = new Toast(sAppContext);
        View toastView = LayoutInflater.from(sAppContext).inflate(R.layout.view_toast_default, null);
        TextView tvToast = toastView.findViewById(R.id.BaseCompatActivity_mTvToast);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastView);
        tvToast.setText(content);
        toast.show();
    }


}
