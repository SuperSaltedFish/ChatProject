package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;


/**
 * Created by YZX on 2017年05月30日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ViewPagerIndicator extends LinearLayout {

    private Context mContext;
    private int mIndicatorCount;
    private int mSelectedColor;
    private int mUnSelectedColor;
    private int mRadiusSize;
    private int mIntervalSize;
    private int mCurrentSelectedIndex;

    public ViewPagerIndicator(Context context) {
        this(context, null);
    }

    public ViewPagerIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initDefault();
    }

    private void initDefault() {
        mSelectedColor = Color.argb(225, 237, 56, 81);
        mUnSelectedColor = Color.argb(225, 168, 168, 168);
//        mRadiusSize = DensityUtil.dip2px(8);
//        mIntervalSize = DensityUtil.dip2px(8);
        mCurrentSelectedIndex = 0;
    }

    private void initView() {
        for (int i = 0; i < mIndicatorCount; i++) {
            IndicatorView indicatorView = new IndicatorView(mContext);
            LayoutParams layoutParams = new LayoutParams(mRadiusSize, mRadiusSize);
            layoutParams.leftMargin = mIntervalSize;
            layoutParams.bottomMargin = mIntervalSize;
            indicatorView.setLayoutParams(layoutParams);
            indicatorView.setIndicatorColor(mUnSelectedColor);
            addView(indicatorView);
        }
    }

    public void setSelectedIndex(int index) {
        IndicatorView currentSelectedView = (IndicatorView) getChildAt(mCurrentSelectedIndex);
        IndicatorView nextSelectedView = (IndicatorView) getChildAt(index);
        currentSelectedView.setIndicatorColor(mUnSelectedColor);
        nextSelectedView.setIndicatorColor(mSelectedColor);
        mCurrentSelectedIndex = index;

    }

    public void setIndicatorCount(int count) {
        mIndicatorCount = count;
        initView();
    }


    public void setIndicatorColor(@ColorInt int selectedColor, @ColorInt int unselectedColor) {
        mSelectedColor = selectedColor;
        mUnSelectedColor = unselectedColor;
    }

    public void setRadiusSize(int radiusSize) {
        mRadiusSize = radiusSize;
    }

    public void setIntervalSize(int intervalSize) {
        mIntervalSize = intervalSize;
    }

    private static class IndicatorView extends android.support.v7.widget.AppCompatImageView {

        private Paint mPaint;

        public IndicatorView(Context context) {
            super(context);
            this.initView();
        }

        private void initView() {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);
        }

        public void setIndicatorColor(@ColorInt int color) {
            mPaint.setColor(color);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            int minSize = Math.min(width, height);
            canvas.drawCircle(width / 2, height / 2, minSize / 2, mPaint);
        }
    }
}
