package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by YZX on 2018年03月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropView extends View {

    private int mViewWidth;
    private int mViewHeight;

    private int mStrokeColor;
    private float mStrokeWidth;
    private int mMaskColor;

    private Paint mPaint;
    private PorterDuffXfermode mPorterDuffXfermode;

    public CropView(Context context) {
        this(context, null);
    }

    public CropView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

        mStrokeColor = Color.WHITE;
        mMaskColor = Color.argb(96, 0, 0, 0);
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    float radius;
    float cx;
    float cy;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int layerId = canvas.saveLayer(0, 0, mViewWidth, mViewHeight, null, Canvas.ALL_SAVE_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, mViewWidth, mViewHeight, mPaint);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        radius = Math.min(mViewWidth - paddingLeft - paddingRight, mViewHeight - paddingTop - paddingBottom) / 2 - mStrokeColor;
        cx = (mViewWidth + paddingLeft - paddingRight) / 2;
        cy = (mViewHeight + paddingTop - paddingBottom) / 2;
        mPaint.setColor(Color.WHITE);
        mPaint.setXfermode(mPorterDuffXfermode);
        canvas.drawCircle(cx, cy, radius, mPaint);
        mPaint.setXfermode(null);
        canvas.restoreToCount(layerId);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mStrokeColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawCircle(cx, cy, radius, mPaint);
    }

    public Rect getCircleRect() {
        int left = (int) (cx - radius);
        int top = (int) (cy - radius);
        int right = (int) (left + 2 * radius);
        int bottom = (int) (top + 2 * radius);
        return new Rect(left, top, right, bottom);
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(@ColorInt int strokeColor) {
        if (mStrokeColor != strokeColor) {
            mStrokeColor = strokeColor;
            invalidate();
        }
    }

    public int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(@ColorInt int maskColor) {
        if (mMaskColor != maskColor) {
            mMaskColor = maskColor;
            invalidate();
        }
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        if (mStrokeWidth != strokeWidth) {
            mStrokeWidth = strokeWidth;
            invalidate();
        }
    }
}
