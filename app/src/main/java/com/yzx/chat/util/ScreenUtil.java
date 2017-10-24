package com.yzx.chat.util;

import android.content.Context;
import android.support.annotation.UiThread;
import android.util.DisplayMetrics;

/**
 * Created by yzx on 2017年05月13日
 * 当你将信心放在自己身上时，你将永远充满力量
 */
public class ScreenUtil {

    private static int sScreenWidth;
    private static int sScreenHeight;
    private static int sScreenDensity;

    @UiThread
    private synchronized static void init() {
        Context context = ReflexUtil.getContext();
        if (context != null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            sScreenWidth = dm.widthPixels;
            sScreenHeight = dm.heightPixels;
        }
    }

    public static int getScreenWidth() {
        if (sScreenWidth == 0 || sScreenHeight == 0 || sScreenDensity == 0) {
            init();
        }
        return sScreenWidth;
    }

    public static int getScreenHeight() {
        if (sScreenWidth == 0 || sScreenHeight == 0 || sScreenDensity == 0) {
            init();
        }
        return sScreenHeight;
    }

    public static int getScreenDensity() {
        if (sScreenWidth == 0 || sScreenHeight == 0 || sScreenDensity == 0) {
            init();
        }
        return sScreenDensity;
    }

}
