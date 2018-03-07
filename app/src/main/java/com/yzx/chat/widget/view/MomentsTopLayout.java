package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by YZX on 2017年09月09日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class MomentsTopLayout extends RelativeLayout {

    private int mViewWidth;
    private int mViewHeight;
    private Paint mBackgroundPaint;
    private Path mBackgroundPath;
    private int mOutlineBackgroundColor;

    public MomentsTopLayout(@NonNull Context context) {
        this(context, null);
    }

    public MomentsTopLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MomentsTopLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        mOutlineBackgroundColor = Color.WHITE;
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(mOutlineBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mBackgroundPath = null;
    }

    private Path getBackgroundPath() {
        if (mBackgroundPath == null) {
            mBackgroundPath = new Path();
            mBackgroundPath.reset();
            mBackgroundPath.moveTo(0, 0);
            mBackgroundPath.quadTo(mViewWidth / 2, mViewHeight / 2, mViewWidth, 0);
            mBackgroundPath.lineTo(mViewWidth, mViewHeight);
            mBackgroundPath.quadTo(mViewWidth / 2, mViewHeight / 2, 0, mViewHeight);
            mBackgroundPath.lineTo(0, 0);
            mBackgroundPath.close();
        }
        return mBackgroundPath;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(getBackgroundPath(), mBackgroundPaint);
    }

    public int getOutlineBackgroundColor() {
        return mOutlineBackgroundColor;
    }

    public void setOutlineBackgroundColor(@ColorInt int outlineBackgroundColor) {
        mOutlineBackgroundColor = outlineBackgroundColor;
    }
}
