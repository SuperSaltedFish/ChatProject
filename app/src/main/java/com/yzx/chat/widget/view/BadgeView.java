package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by YZX on 2018年02月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class BadgeView extends View {

    private Context mContext;

    private Paint mBadgePaint;
    private Paint mBackgroundPaint;
    private Rect mTextRect;
    private RectF mBadgeRectF;
    private int mBadgeNumber;
    private float mBadgeTextSize;
    private int mBadgeTextColor;
    private int mBackgroundColor;

    public BadgeView(Context context) {
        this(context, null);
    }

    public BadgeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mBadgeRectF = new RectF();
        mTextRect = new Rect();

        mBadgePaint = new Paint();
        mBackgroundPaint = new Paint();

        mBadgePaint.setTextAlign(Paint.Align.CENTER);
        mBadgePaint.setAntiAlias(true);
        mBackgroundPaint.setAntiAlias(true);

        setDefault();

    }

    private void setDefault() {
        setBadgeTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, mContext.getResources().getDisplayMetrics()));
        setBadgeTextColor(Color.WHITE);
        setBadgeBackgroundColor(Color.RED);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4, mContext.getResources().getDisplayMetrics());
        setPadding(padding, padding, padding, padding);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            width = (int) mBadgePaint.measureText(String.valueOf(mBadgeNumber));
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            String strBadge = String.valueOf(mBadgeNumber);
            mBadgePaint.getTextBounds(strBadge, 0, strBadge.length(), mTextRect);
            height = mTextRect.height();
        }
        int maxSize = Math.max(width, height);
        super.onMeasure(MeasureSpec.makeMeasureSpec(maxSize, widthMode) + getPaddingLeft() + getPaddingRight(),
                MeasureSpec.makeMeasureSpec(maxSize + getPaddingTop() + getPaddingBottom(), heightMode));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBadgeRectF.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(mBadgeRectF, mBadgeRectF.width() / 2f, mBadgeRectF.height() / 2f, mBackgroundPaint);
        canvas.drawText(String.valueOf(mBadgeNumber), mBadgeRectF.centerX(), (mBadgeRectF.top + mTextRect.height() + mBadgeRectF.bottom) / 2f, mBadgePaint);
    }

    public int getBadgeNumber() {
        return mBadgeNumber;
    }

    public void setBadgeNumber(int badgeNumber) {
        if (mBadgeNumber != badgeNumber) {
            mBadgeNumber = badgeNumber;
            requestLayout();
        }
    }

    public float getBadgeTextSize() {
        return mBadgeTextSize;
    }

    public void setBadgeTextSize(float badgeTextSize) {
        if (mBadgeTextSize != badgeTextSize) {
            mBadgeTextSize = badgeTextSize;
            mBadgePaint.setTextSize(badgeTextSize);
            invalidate();
        }
    }

    public int getBadgeTextColor() {
        return mBadgeTextColor;
    }

    public void setBadgeTextColor(int badgeTextColor) {
        if (mBadgeTextColor != badgeTextColor) {
            mBadgeTextColor = badgeTextColor;
            mBadgePaint.setColor(mBadgeTextColor);
            invalidate();
        }
    }

    public int getBadgeBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBadgeBackgroundColor(int backgroundColor) {
        if (mBackgroundColor != backgroundColor) {
            mBackgroundColor = backgroundColor;
            mBackgroundPaint.setColor(mBackgroundColor);
            invalidate();
        }
    }

    public Paint getBadgePaint() {
        return mBadgePaint;
    }

    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }
}
