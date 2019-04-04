package com.yzx.chat.tool;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import androidx.annotation.Nullable;

/**
 * Created by YZX on 2018年12月03日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ActivityHelper {
    private static int sActivityForegroundCount;

    private static Stack<Class<? extends Activity>> sActivityClassStack;
    private static Stack<Activity> sActivityInstanceStack;
    private static List<OnAppFrontAndBackListener> sAppFrontAndBackListenerList;

    public synchronized static void init(Application application) {
        sActivityClassStack = new Stack<>();
        sActivityInstanceStack = new Stack<>();
        sAppFrontAndBackListenerList = new LinkedList<>();

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                sActivityClassStack.push(activity.getClass());
                sActivityInstanceStack.push(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                sActivityForegroundCount++;
                if (sActivityForegroundCount == 1) {
                    for (OnAppFrontAndBackListener listener : sAppFrontAndBackListenerList) {
                        listener.onAppForeground();
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                sActivityForegroundCount--;
                if (sActivityForegroundCount == 0) {
                    for (OnAppFrontAndBackListener listener : sAppFrontAndBackListenerList) {
                        listener.onAppBackground();
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                int index = sActivityInstanceStack.indexOf(activity);
                sActivityInstanceStack.remove(index);
                sActivityClassStack.remove(index);

            }
        });
    }

    public static boolean isAppForeground() {
        return sActivityForegroundCount > 0;
    }

    public static Class<? extends Activity> getStackTopActivityClass() {
        try {
            return sActivityClassStack.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    public static Activity getStackTopActivityInstance() {
        return sActivityInstanceStack.peek();
    }

    public static boolean isExistInActivityStack(Class<? extends Activity> activityClass) {
        return sActivityClassStack.search(activityClass) >= 0;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Activity> T getActivityInstance(Class<T> activityClass) {
        for (int i = sActivityClassStack.size() - 1; i >= 0; i--) {
            if (activityClass.equals(sActivityClassStack.get(i))) {
                return (T) sActivityInstanceStack.get(i);
            }
        }
        return null;
    }

    public static void finishActivitiesInStackAbove(Class<? extends Activity> activityClass) {
        finishActivitiesInStackAbove(activityClass, false);
    }

    public static void finishActivitiesInStackAbove(Class<? extends Activity> activityClass, boolean include) {
        int index = sActivityClassStack.indexOf(activityClass);
        if (index < 0) {
            return;
        }
        if (include) {
            index--;
        }
        for (int i = sActivityClassStack.size() - 1; i > index; i--) {
            Class c = sActivityClassStack.get(i);
            if (!c.equals(activityClass)) {
                sActivityInstanceStack.get(i).finish();
            } else if (include) {
                sActivityInstanceStack.get(i).finish();
            }
        }
    }

    public static void finishActivitiesInStackBelow(Class<? extends Activity> activityClass) {
        int index = sActivityClassStack.indexOf(activityClass);
        if (index < 0) {
            return;
        }
        for (int i = 0; i < index; i++) {
            Class c = sActivityClassStack.get(i);
            if (!c.equals(activityClass)) {
                sActivityInstanceStack.get(i).finish();
            }
        }
    }

    public static void finishAllActivities() {
        for (Activity activity : sActivityInstanceStack) {
            activity.finish();
        }
    }


    public static synchronized void addAppFrontAndBackListener(OnAppFrontAndBackListener listener) {
        if (!sAppFrontAndBackListenerList.contains(listener)) {
            if (listener != null) {
                if (sActivityForegroundCount == 0) {
                    listener.onAppBackground();
                } else {
                    listener.onAppForeground();
                }
                sAppFrontAndBackListenerList.add(listener);
            }
        }
    }

    public static synchronized void removeAppFrontAndBackListener(OnAppFrontAndBackListener listener) {
        sAppFrontAndBackListenerList.remove(listener);
    }


    public interface OnAppFrontAndBackListener {
        void onAppForeground();

        void onAppBackground();
    }
}
