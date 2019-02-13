package com.yzx.chat.core.util;

import android.content.Context;
import android.support.annotation.StringRes;



/**
 * Created by YZX on 2017年10月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ResourcesHelper {

    private static Context sAppContext;

    public synchronized static void init(Context appContent) {
        sAppContext = appContent.getApplicationContext();
    }


    public static String getString(@StringRes int resID) {
        return sAppContext.getString(resID);
    }

}
