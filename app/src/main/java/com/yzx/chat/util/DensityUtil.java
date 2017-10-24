package com.yzx.chat.util;

import android.util.TypedValue;

/**
 * Created by yzx on 2017年05月13日
 * 当你将信心放在自己身上时，你将永远充满力量
 */

public class DensityUtil {
    public static float dip2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, ReflexUtil.getContext().getResources().getDisplayMetrics());
    }


    public static float px2dip(float pxValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pxValue, ReflexUtil.getContext().getResources().getDisplayMetrics());
    }

    public static float px2sp(float pxValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, pxValue, ReflexUtil.getContext().getResources().getDisplayMetrics());
    }


    public static int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, ReflexUtil.getContext().getResources().getDisplayMetrics());

    }
}
