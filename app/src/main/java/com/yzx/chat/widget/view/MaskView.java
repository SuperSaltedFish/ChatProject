package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;


public class MaskView extends View {

    private RectF mSpaceRect;
    private PorterDuffXfermode mXfermode;
    private Paint mPaint;
    private float mRoundRadius;
    private int mMaskColor;

    public MaskView(Context context) {
        this(context, null);
    }

    public MaskView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.WHITE);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        setMaskColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (mSpaceRect != null) {
            int width = getWidth();
            int height = getHeight();
            canvas.drawRect(0, 0, width, mSpaceRect.top, mPaint);
            canvas.drawRect(0, mSpaceRect.bottom, width, height, mPaint);
            canvas.drawRect(0, mSpaceRect.top, mSpaceRect.left, mSpaceRect.bottom, mPaint);
            canvas.drawRect(mSpaceRect.right, mSpaceRect.top, width, mSpaceRect.bottom, mPaint);

            int count = canvas.saveLayer(mSpaceRect, null);
            canvas.drawColor(mMaskColor);
            mPaint.setXfermode(mXfermode);
            canvas.drawRoundRect(mSpaceRect.left, mSpaceRect.top, mSpaceRect.right, mSpaceRect.bottom, mRoundRadius, mRoundRadius, mPaint);
            mPaint.setXfermode(null);
            canvas.restoreToCount(count);
        }
    }

    public void setSpaceRect(float left, float top, float right, float bottom) {
        if (mSpaceRect == null) {
            mSpaceRect = new RectF(left, top, right, bottom);
        } else {
            mSpaceRect.set(left, top, right, bottom);
        }
        invalidate();
    }

    public void setSpaceRect(Rect rect) {
        setSpaceRect(rect.left, rect.top, rect.right, rect.bottom);
    }

    public RectF getSpaceRect() {
        return mSpaceRect;
    }

    public int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(@ColorInt int maskColor) {
        mMaskColor = maskColor;
        mPaint.setColor(mMaskColor);
        invalidate();
    }

    public void setRoundRadius(float roundRadius) {
        mRoundRadius = roundRadius;
        invalidate();
    }
}
