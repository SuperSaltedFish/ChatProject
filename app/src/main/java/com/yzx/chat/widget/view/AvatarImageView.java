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

public class AvatarImageView extends android.support.v7.widget.AppCompatImageView {

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

    private RectF mDigitalRectF;
    private String mDigital;
    private Paint mDigitalPaint;
    private Paint mDigitalBackgroundPaint;
    private float mDigitalTextSize;
    private int mDigitalPadding;
    private int mDigitalTextHeight;
    private int mDigitalMode;
    private int mDigitalBackgroundColor;
    private int mDigitalTextColor;

    private boolean isReset = true;

    public AvatarImageView(Context context) {
        this(context, null);
    }

    public AvatarImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mDigitalBackgroundPaint = new Paint();
        mDigitalBackgroundPaint.setStyle(Paint.Style.FILL);
        mDigitalBackgroundPaint.setAntiAlias(true);

        mDigitalPaint = new Paint();
        mDigitalPaint.setStyle(Paint.Style.FILL);
        mDigitalPaint.setAntiAlias(true);
        mDigitalPaint.setTextAlign(Paint.Align.CENTER);

        setDefault();

    }

    private void setDefault() {
        mDigitalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                4, mContext.getResources().getDisplayMetrics());
        mDigitalTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                12, mContext.getResources().getDisplayMetrics());
        mDigital = String.valueOf(0);
        mDigitalMode = MODE_SHOW;
        mDigitalBackgroundColor = Color.RED;
        mDigitalTextColor = Color.WHITE;
    }

    private void initDigitalHint() {
        mDigitalBackgroundPaint.setColor(mDigitalBackgroundColor);
        mDigitalPaint.setColor(mDigitalTextColor);
        mDigitalPaint.setTextSize(mDigitalTextSize);
        mDigitalRectF = new RectF();
        Rect textRect = new Rect();
        mDigitalPaint.getTextBounds(mDigital, 0, mDigital.length(), textRect);
        int textWidth = textRect.width();
        int textHeight = textRect.height();
        mDigitalTextHeight = textHeight;
        mDigitalPaint.getTextBounds("99", 0, 2, textRect);
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
        textWidth += mDigitalPadding * 2;
        textHeight += mDigitalPadding * 2;
        if (mDigitalMode == MODE_SHOW_ONLY_SMALL_BACKGROUND) {
            textWidth *= 0.6;
            textHeight *= 0.6;
        }
        mDigitalRectF.set(mViewWidth - textWidth, 0, mViewWidth, textHeight);
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
        if (mDigitalMode == MODE_HIDE) {
            return;
        }
        if (isReset) {
            initDigitalHint();
            isReset = false;
        }
        canvas.drawRoundRect(mDigitalRectF, mDigitalRectF.width() / 2f, mDigitalRectF.height() / 2f, mDigitalBackgroundPaint);
        if (mDigitalMode == MODE_SHOW) {
            canvas.drawText(mDigital, mDigitalRectF.centerX(), (mDigitalRectF.bottom + mDigitalTextHeight) / 2f, mDigitalPaint);
        }
    }

    public void setDigitalMode(@DigitalMode int mode) {
        if (mDigitalMode != mode) {
            mDigitalMode = mode;
            reset();
        }
    }

    public void setDigital(int number) {
        String strNumber = String.valueOf(number);
        if (!strNumber.equals(mDigital)) {
            mDigital = strNumber;
            reset();
        }
    }

    public void setDigitalTextSize(float spSize) {
        int digitalTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spSize, mContext.getResources().getDisplayMetrics());
        if (digitalTextSize != mDigitalTextSize) {
            reset();
        }
    }

    public void setDigitalPadding(int digitalPadding) {
        if (digitalPadding != mDigitalPadding) {
            mDigitalPadding = digitalPadding;
            reset();
        }
    }

    public void setDigitalTextColor(@ColorInt int color) {
        if (mDigitalTextColor != color) {
            mDigitalTextColor = color;
            reset();
        }
    }

    public void setDigitalBackgroundColor(@ColorInt int color) {
        if (mDigitalBackgroundColor != color) {
            mDigitalBackgroundColor = color;
            reset();
        }
    }

    private void reset() {
        isReset = true;
        invalidate();
    }
}




