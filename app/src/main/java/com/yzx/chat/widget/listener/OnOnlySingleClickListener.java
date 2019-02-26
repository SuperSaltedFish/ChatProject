package com.yzx.chat.widget.listener;

import android.os.SystemClock;
import android.view.View;

/**
 * Created by YZX on 2018年12月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public abstract class OnOnlySingleClickListener implements View.OnClickListener {

    private static final int MAX_CLICK_INTERVAL = 600;

    private long lastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long nowTime = SystemClock.elapsedRealtime();
        if (nowTime - lastClickTime >= MAX_CLICK_INTERVAL) {
            onSingleClick(v);
        }
        lastClickTime = nowTime;
    }
}
