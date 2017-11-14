package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by YZX on 2017年05月31日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class BadgeImageView extends android.support.v7.widget.AppCompatImageView {

    public static final int MODE_HIDE = 0;
    public static final int MODE_SHOW = 1;
    public static final int MODE_SHOW_ONLY_SMALL_BACKGROUND = 2;

    @IntDef({MODE_HIDE, MODE_SHOW, MODE_SHOW_ONLY_SMALL_BACKGROUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DigitalMode {
    }

    private Context mContext;

    private int mViewWidth;
    private int mViewHeight;

    private RectF mBadgeRectF;
    private String mBadgeText;
    private Paint mBadgeTextlPaint;
    private Paint mBadgeBackgroundPaint;
    private float mBadgeTextSize;
    private int mBadgeTextPadding;
    private int mBadgeTextHeight;
    private int mBadgeMode;
    private int mBadgeBackgroundColor;
    private int mBadgeTextColor;
    private int mBadgePaddingLeft;
    private int mBadgePaddingRight;
    private int mBadgePaddingTop;
    private int mBadgePaddingBottom;

    private boolean isReset = true;

    public BadgeImageView(Context context) {
        this(context, null);
    }

    public BadgeImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mBadgeBackgroundPaint = new Paint();
        mBadgeBackgroundPaint.setStyle(Paint.Style.FILL);
        mBadgeBackgroundPaint.setAntiAlias(true);

        mBadgeTextlPaint = new Paint();
        mBadgeTextlPaint.setStyle(Paint.Style.FILL);
        mBadgeTextlPaint.setAntiAlias(true);
        mBadgeTextlPaint.setTextAlign(Paint.Align.CENTER);

        setDefault();

    }

    private void setDefault() {
        mBadgeTextPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                4, mContext.getResources().getDisplayMetrics());
        mBadgeTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                12, mContext.getResources().getDisplayMetrics());
        mBadgeText = String.valueOf(0);
        mBadgeMode = MODE_SHOW;
        mBadgeBackgroundColor = Color.RED;
        mBadgeTextColor = Color.WHITE;
    }

    private void initDigitalHint() {
        mBadgeBackgroundPaint.setColor(mBadgeBackgroundColor);
        mBadgeTextlPaint.setColor(mBadgeTextColor);
        mBadgeTextlPaint.setTextSize(mBadgeTextSize);
        mBadgeRectF = new RectF();
        Rect textRect = new Rect();
        mBadgeTextlPaint.getTextBounds(mBadgeText, 0, mBadgeText.length(), textRect);
        int textWidth = textRect.width();
        int textHeight = textRect.height();
        mBadgeTextHeight = textHeight;
        mBadgeTextlPaint.getTextBounds("99", 0, 2, textRect);
        int minSize = Math.max(textRect.width(), textRect.height());
        if (textWidth < minSize) {
            textWidth = minSize;
        }
        if (textHeight < minSize) {
            textHeight = minSize;
        }
        if (textHeight > textWidth) {
            textWidth = textHeight;
        }
        textWidth += mBadgeTextPadding * 2;
        textHeight += mBadgeTextPadding * 2;
        if (mBadgeMode == MODE_SHOW_ONLY_SMALL_BACKGROUND) {
            textWidth *= 0.6;
            textHeight *= 0.6;
        }
        mBadgeRectF.set(mViewWidth - textWidth + mBadgePaddingLeft - mBadgePaddingRight,
                mBadgePaddingTop - mBadgePaddingBottom,
                mViewWidth + mBadgePaddingLeft - mBadgePaddingRight,
                textHeight + mBadgePaddingTop - mBadgePaddingBottom);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBadgeMode == MODE_HIDE) {
            return;
        }
        if (isReset) {
            initDigitalHint();
            isReset = false;
        }
        canvas.drawRoundRect(mBadgeRectF, mBadgeRectF.width() / 2f, mBadgeRectF.height() / 2f, mBadgeBackgroundPaint);
        if (mBadgeMode == MODE_SHOW) {
            canvas.drawText(mBadgeText, mBadgeRectF.centerX(), (mBadgeRectF.top + mBadgeTextHeight + mBadgeRectF.bottom) / 2f, mBadgeTextlPaint);
        }
    }

    public void setBadgeMode(@DigitalMode int mode) {
        if (mBadgeMode != mode) {
            mBadgeMode = mode;
            reset();
        }
    }

    public void setBadgeText(int number) {
        String strNumber = String.valueOf(number);
        if (!strNumber.equals(mBadgeText)) {
            mBadgeText = strNumber;
            reset();
        }
    }

    public void setBadgeTextSize(float spSize) {
        int digitalTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spSize, mContext.getResources().getDisplayMetrics());
        if (digitalTextSize != mBadgeTextSize) {
            mBadgeTextSize = digitalTextSize;
            reset();
        }
    }

    public void setBadgeTextPadding(int badgeTextPadding) {
        if (badgeTextPadding != mBadgeTextPadding) {
            mBadgeTextPadding = badgeTextPadding;
            reset();
        }
    }

    public void setBadgeTextColor(@ColorInt int color) {
        if (mBadgeTextColor != color) {
            mBadgeTextColor = color;
            reset();
        }
    }

    public void setBadgeBackgroundColor(@ColorInt int color) {
        if (mBadgeBackgroundColor != color) {
            mBadgeBackgroundColor = color;
            reset();
        }
    }

    public void setBadgePadding(int left, int top, int right, int bottom) {
        if (left == mBadgePaddingLeft && top == mBadgePaddingTop && right == mBadgePaddingRight && bottom == mBadgePaddingBottom) {
            return;
        }
        mBadgePaddingLeft = left;
        mBadgePaddingTop = top;
        mBadgePaddingRight = right;
        mBadgePaddingBottom = bottom;
        reset();
    }

    private void reset() {
        isReset = true;
        invalidate();
    }
}




