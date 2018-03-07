package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by YZX on 2017年06月19日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class IndexBarView extends View {

    private TextPaint mTextPaint;
    private int currentIndex = -1;
    private float mOffsetY;
    private float mWordRectHeight;
    private float mWordSize;
    private int mWidth;
    private int mHeight;
    private int mStartX;
    private int mStartY;
    private OnTouchSelectedListener mOnTouchSelectedListener;

    private int mSelectedTextColor = Color.BLACK;
    private int mUnselectedTextColor = Color.GRAY;

    private static final String[] WORDS = new String[]{"☆", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    public IndexBarView(Context context) {
        this(context, null);
    }

    public IndexBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            getParent().requestDisallowInterceptTouchEvent(true);
        } else {
            getParent().requestDisallowInterceptTouchEvent(false);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mStartX = getPaddingLeft();
        mStartY = getPaddingTop();
        mWidth = w - mStartX - getPaddingRight();
        mHeight = h - mStartY - getPaddingBottom();

        mWordRectHeight = (float) mHeight / WORDS.length;
        mWordSize = mWordRectHeight * 0.8f;

        mTextPaint.setTextSize(mWordSize);
        mTextPaint.setColor(mUnselectedTextColor);

        initOffsetY();
    }

    private void initOffsetY() {
        Paint.FontMetricsInt fm = mTextPaint.getFontMetricsInt();
        mOffsetY = mWordRectHeight - (mWordRectHeight + fm.ascent) / 2;
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0, count = WORDS.length; i < count; i++) {
            if (currentIndex == i) {
                mTextPaint.setTextSize(mWordSize * 1.5f);
                mTextPaint.setColor(mSelectedTextColor);
                initOffsetY();
                canvas.drawText(WORDS[i], mStartX + (mWidth - mTextPaint.measureText(WORDS[i])) / 2f, mWordRectHeight * i + mOffsetY, mTextPaint);
                mTextPaint.setTextSize(mWordSize);
                mTextPaint.setColor(mUnselectedTextColor);
                initOffsetY();
            } else {
                canvas.drawText(WORDS[i], mStartX + (mWidth - mTextPaint.measureText(WORDS[i])) / 2f, mWordRectHeight * i + mOffsetY, mTextPaint);
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        boolean isNewSelectIndex = false;
        if (mOnTouchSelectedListener != null&&y>=0&&y<=mHeight) {
            mOnTouchSelectedListener.onMove((int) y);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (y >= mStartY && y <= mStartY + mHeight) {
                    currentIndex = (int) ((y - mStartY) / mWordRectHeight);
                    isNewSelectIndex = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (y >= mStartY && y <= mStartY + mHeight) {
                    int newIndex = (int) ((y - mStartY) / mWordRectHeight);
                    if (currentIndex != newIndex) {
                        currentIndex = newIndex;
                        isNewSelectIndex = true;
                    }
                }
                break;
            default:
                if (mOnTouchSelectedListener != null) {
                    mOnTouchSelectedListener.onCancelSelected();
                }
                currentIndex = -1;
                isNewSelectIndex = true;
                break;
        }
        if (isNewSelectIndex) {
            if (mOnTouchSelectedListener != null && currentIndex < WORDS.length) {
                if (currentIndex != -1) {
                    mOnTouchSelectedListener.onSelected(currentIndex, WORDS[currentIndex]);
                }
                invalidate();
            }
        }
        return true;
    }

    public int getSelectedTextColor() {
        return mSelectedTextColor;
    }

    public void setSelectedTextColor(@ColorInt int selectedTextColor) {
        mSelectedTextColor = selectedTextColor;
    }

    public int getUnselectedTextColor() {
        return mUnselectedTextColor;
    }

    public void setUnselectedTextColor(int unselectedTextColor) {
        mUnselectedTextColor = unselectedTextColor;
    }

    public void setOnTouchSelectedListener(OnTouchSelectedListener listener) {
        mOnTouchSelectedListener = listener;
    }

    public interface OnTouchSelectedListener {
        void onSelected(int position, String text);

        void onCancelSelected();

        void onMove(int offsetPixelsY);
    }
}
