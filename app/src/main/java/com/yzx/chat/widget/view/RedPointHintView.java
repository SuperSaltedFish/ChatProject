package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by YZX on 2018年02月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class RedPointHintView extends View {

    private int mNumber;

    public RedPointHintView(Context context) {
        this(context, null);
    }

    public RedPointHintView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RedPointHintView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }
}
