package com.yzx.chat.util;

import android.content.Context;
import android.support.annotation.UiThread;

import java.lang.reflect.Method;

/**
 * Created by yzx on 2017年05月13日
 * 当你将信心放在自己身上时，你将永远充满力量
 */
public class ReflexUtil {

    private static Context ApplicationContext;

    @UiThread
    public static Context getContext() {
        if (ApplicationContext == null) {
            synchronized (ReflexUtil.class) {
                if (ApplicationContext == null) {
                    try {
                        Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
                        Method currentActivityThreadMethod = ActivityThread.getMethod("currentActivityThread");
                        Object currentActivityThread = currentActivityThreadMethod.invoke(ActivityThread);
                        Method getApplicationMethod = currentActivityThread.getClass().getMethod("getApplication");
                        ApplicationContext = (Context) getApplicationMethod.invoke(currentActivityThread);
                    } catch (Exception e) {
                        ApplicationContext = null;
                    }

                }
            }
        }
        return ApplicationContext;
    }

}
