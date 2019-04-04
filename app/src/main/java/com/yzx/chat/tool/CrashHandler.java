package com.yzx.chat.tool;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.yzx.chat.BuildConfig;
import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.util.FileUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


/**
 * Created by 叶智星 on 2018年10月18日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */
public class CrashHandler {

    public static final String TAG = "CrashHandler";


    public static void attachMainThread() {
        Thread.setDefaultUncaughtExceptionHandler(DEFAULT_CAUGHT_HANDLER);
    }

    private static final Thread.UncaughtExceptionHandler DEFAULT_CAUGHT_HANDLER = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, final Throwable e) {
            String error = collectErrorInfoToLocal(e);
            Log.e(TAG, error);
//            FileUtil.saveStringToFile(error, DirectoryHelper.getPublicLogPath(), DateFormatUtil.millisTo_yyyy_MM_dd_HH_mm_ss(System.currentTimeMillis()) + ".txt");
//            if (BuildConfig.isDebug) {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        Looper.prepare();
//                        Toast.makeText(AppApplication.getAppContext(), "Program exit:" + e.toString(), Toast.LENGTH_LONG).show();
//                        Looper.loop();
//                    }
//                }.start();
//                SystemClock.sleep(3000);
//            }
            ActivityHelper.finishAllActivities();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    };

    private static String collectErrorInfoToLocal(Throwable e) {
        if (e == null) {
            return "";
        }
        Writer mWriter = new StringWriter();
        PrintWriter mPrintWriter = new PrintWriter(mWriter);
        e.printStackTrace(mPrintWriter);
        Throwable throwable = e.getCause();
        while (throwable != null) {
            throwable.printStackTrace(mPrintWriter);
            mPrintWriter.append("\r\n");
            throwable = throwable.getCause();
        }
        mPrintWriter.close();
        return mWriter.toString();
    }
}
