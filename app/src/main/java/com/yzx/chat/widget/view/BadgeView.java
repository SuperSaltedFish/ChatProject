package com.yzx.chat.widget.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by YZX on 2018年02月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class BadgeView extends View {

    private final static int MAX_BADGE_NUMBER = 99;
    private static final long ANIMATOR_MAX_DURATION = 128;

    private Context mContext;

    private Paint mBadgePaint;
    private Paint mBackgroundPaint;
    private Rect mTextRect;
    private RectF mBadgeRectF;
    private int mBadgeNumber;
    private float mBadgeTextSize;
    private int mBadgeTextColor;
    private int mBackgroundColor;

    private ValueAnimator mAnimator;

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
        mBadgePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mBadgePaint.setAntiAlias(true);
        mBackgroundPaint.setAntiAlias(true);

        setWillNotDraw(false);

        setDefault();
        initAnimator();
    }

    private void initAnimator() {
        mAnimator = new ValueAnimator();
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                setScaleX(value);
                setScaleY(value);
            }
        });
    }

    private void setDefault() {
        setBadgeTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10.5f, mContext.getResources().getDisplayMetrics()));
        setBadgeTextColor(Color.WHITE);
        setBadgeBackgroundColor(ContextCompat.getColor(mContext, android.R.color.holo_red_light));
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        setPadding(padding, padding, padding, padding);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        boolean isOverstep;
        String strNumber;
        if (mBadgeNumber <= MAX_BADGE_NUMBER) {
            strNumber = String.valueOf(MAX_BADGE_NUMBER);
            isOverstep = false;
        } else {
            isOverstep = true;
            strNumber = MAX_BADGE_NUMBER + "+";
        }
        mBadgePaint.getTextBounds(strNumber, 0, strNumber.length(), mTextRect);
        if (widthMode != MeasureSpec.EXACTLY) {
            width = mTextRect.width();
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            height = mTextRect.height();
        }
        if (!isOverstep) {
            height = width = Math.max(width, height);
        }
        setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(), height + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBadgeRectF.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBadgeNumber == 0) {
            return;
        }
        float radius = Math.min(mBadgeRectF.width() / 2f, mBadgeRectF.height() / 2f);
        canvas.drawRoundRect(mBadgeRectF, radius, radius, mBackgroundPaint);
        String badge;
        if (mBadgeNumber > MAX_BADGE_NUMBER) {
            badge = String.valueOf(MAX_BADGE_NUMBER) + "+";
        } else {
            badge = String.valueOf(mBadgeNumber);
        }
        canvas.drawText(badge, mBadgeRectF.centerX(), (mBadgeRectF.top + mTextRect.height() + mBadgeRectF.bottom) / 2f, mBadgePaint);
    }

    public int getBadgeNumber() {
        return mBadgeNumber;
    }

    public void setBadgeNumber(int badgeNumber) {
        if (mBadgeNumber != badgeNumber) {
            if (mAnimator.isStarted()) {
                mAnimator.cancel();
            }
            if ((badgeNumber >= 10 && mBadgeNumber < 10) || (mBadgeNumber >= 10 && badgeNumber < 10)) {
                requestLayout();
            } else {
                invalidate();
            }
            if (mBadgeNumber == 0 && badgeNumber != 0) {
                float currentScale = getScrollX();
                mAnimator.setDuration((long) (ANIMATOR_MAX_DURATION * (1 - currentScale)));
                mAnimator.setFloatValues(currentScale, 1);
                mAnimator.start();
            }
            mBadgeNumber = badgeNumber;
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
