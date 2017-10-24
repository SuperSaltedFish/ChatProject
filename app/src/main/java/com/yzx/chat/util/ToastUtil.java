package com.yzx.chat.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by YZX on 2017年10月21日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ToastUtil {

    public static void showShort(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
