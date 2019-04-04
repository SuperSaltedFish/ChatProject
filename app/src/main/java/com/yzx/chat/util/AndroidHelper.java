package com.yzx.chat.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleableRes;
import androidx.core.content.ContextCompat;

/**
 * Created by YZX on 2017年10月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class AndroidHelper {

    private static final String TAG = AndroidHelper.class.getName();

    private static Context sAppContext;

    private static int sScreenWidth;
    private static int sScreenHeight;
    private static int sScreenDensity;

    public synchronized static void init(Context context) {
        sAppContext = context.getApplicationContext();

        WindowManager manager = (WindowManager) sAppContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = manager.getDefaultDisplay();
        display.getRealMetrics(dm);
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

    public static String[] getStringArray(@ArrayRes int resID) {
        return sAppContext.getResources().getStringArray(resID);
    }


    @ColorInt
    public static int getColor(@ColorRes int resID) {
        return ContextCompat.getColor(sAppContext, resID);
    }

    public static Drawable getDrawable(@DrawableRes int resID) {
        return ContextCompat.getDrawable(sAppContext, resID);
    }

    public static float getDimension(@DimenRes int resID) {
        return sAppContext.getResources().getDimension(resID);
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
        Toast.makeText(sAppContext, content, Toast.LENGTH_SHORT).show();
    }

    public static boolean rawResToLocalFile(@RawRes int resID, String savePath) {
        InputStream inputStream = sAppContext.getResources().openRawResource(resID);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(savePath);
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            Log.d(TAG, e.toString(), e);
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString(), e);
                }
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.d(TAG, e.toString(), e);
            }
        }
        return true;
    }


}
