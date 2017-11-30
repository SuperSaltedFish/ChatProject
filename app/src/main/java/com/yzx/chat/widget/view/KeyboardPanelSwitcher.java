package com.yzx.chat.widget.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2017年11月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class KeyboardPanelSwitcher extends LinearLayout {
    public KeyboardPanelSwitcher(Context context) {
        this(context,null);
    }

    public KeyboardPanelSwitcher(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public KeyboardPanelSwitcher(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        LogUtil.e(changed+" "+l+" "+t+" "+r+" "+b);
    }
}
