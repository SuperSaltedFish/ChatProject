package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;


public class MaskView extends View {

    private Rect mSpaceRect;
    private Rect mTopMaskRect;
    private Rect mBottomMaskRect;
    private Rect mLeftMaskRect;
    private Rect mRightMaskRect;

    private Paint mMaskPaint;
    private boolean isChange;
    private int mMaskColor;

    public MaskView(Context context) {
        this(context, null);
    }

    public MaskView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMaskPaint = new Paint();
        mMaskPaint.setStyle(Paint.Style.FILL);
        mLeftMaskRect = new Rect();
        mTopMaskRect = new Rect();
        mRightMaskRect = new Rect();
        mBottomMaskRect = new Rect();
        setMaskColor(Color.TRANSPARENT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSpaceRect();
    }

    private void initSpaceRect() {
        int width = getWidth();
        int height = getHeight();
        if (mSpaceRect == null) {
            mLeftMaskRect.set(0, 0, width, height);
            mTopMaskRect.set(0, 0, 0, 0);
            mRightMaskRect.set(0, 0, 0, 0);
            mBottomMaskRect.set(0, 0, 0, 0);
        } else {
            int spaceL = mSpaceRect.left;
            int spaceT = mSpaceRect.top;
            int spaceR = mSpaceRect.right;
            int spaceB = mSpaceRect.bottom;
            mLeftMaskRect.set(0, spaceT, spaceL, spaceB);
            mTopMaskRect.set(0, 0, width, spaceT);
            mRightMaskRect.set(spaceR, spaceT, width, spaceB);
            mBottomMaskRect.set(0, spaceB, width, height);
        }
        isChange = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isChange){
            initSpaceRect();
        }
        canvas.drawRect(mLeftMaskRect, mMaskPaint);
        canvas.drawRect(mTopMaskRect, mMaskPaint);
        canvas.drawRect(mRightMaskRect, mMaskPaint);
        canvas.drawRect(mBottomMaskRect, mMaskPaint);
    }

    public void setSpaceRect(int left, int top, int right, int bottom) {
        if (mSpaceRect == null) {
            mSpaceRect = new Rect(left, top, right, bottom);
        } else {
            mSpaceRect.set(left, top, right, bottom);
        }
        isChange = true;
        invalidate();
    }

    public void setSpaceRect(Rect rect) {
        setSpaceRect(rect.left, rect.top, rect.right, rect.bottom);
    }

    public Rect getSpaceRect() {
        return mSpaceRect;
    }

    public int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(@ColorInt int maskColor) {
        mMaskColor = maskColor;
        mMaskPaint.setColor(mMaskColor);
        invalidate();
    }
}
