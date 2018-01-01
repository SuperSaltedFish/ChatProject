package com.yzx.chat.tool;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.yzx.chat.util.NetworkAsyncTask;

/**
 * Created by YZX on 2017年12月15日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ActivityTool {
    private static int sActivityStartedCount;
    private static int sActivityLiveCount;

    public static boolean isAppForeground() {
        return sActivityStartedCount > 0;
    }

    public static Class<? extends Activity> getTopActivityClass() {
        return sTopActivityClass;
    }

    private static Class<? extends Activity> sTopActivityClass;

    public synchronized static void init(Application application) {
        application.unregisterActivityLifecycleCallbacks(sLifecycleCallbacks);
        application.registerActivityLifecycleCallbacks(sLifecycleCallbacks);
    }

    private static Application.ActivityLifecycleCallbacks sLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            sActivityLiveCount++;
        }

        @Override
        public void onActivityStarted(Activity activity) {
            sActivityStartedCount++;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            sTopActivityClass = activity.getClass();
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
        }
    };
}
