package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.yzx.chat.R;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * Created by YZX on 2018年08月14日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class NumberCheckBox extends View {

    private Paint mPaint;
    private int mNumberTextColor;
    private float mNumberTextSize;
    private float mUnselectedStrokeWidth;
    private int mUnselectedStrokeColor;
    private int mSelectedFillColor;
    private float mRoundSize;
    private Rect mNumberTextRect;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    private boolean isChecked;
    private int mNumber;

    public NumberCheckBox(Context context) {
        this(context, null);
    }

    public NumberCheckBox(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberCheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        initDefault(context);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setChecked(!isChecked);
            }
        });
    }

    private void initDefault(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mNumberTextColor = Color.WHITE;
        mNumberTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, metrics);
        mUnselectedStrokeColor = Color.WHITE;
        mUnselectedStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, metrics);
        mRoundSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        mSelectedFillColor = typedValue.data;
    }

    public void setChecked(boolean isChecked) {
        if (isChecked == this.isChecked) {
            return;
        }
        this.isChecked = isChecked;
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this.isChecked);
        }
        invalidate();
    }

    public void setNumber(int number) {
        if (mNumber == number) {
            return;
        }
        mNumber = number;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mUnselectedStrokeWidth);
        mPaint.setColor(mUnselectedStrokeColor);
        canvas.drawRoundRect(0, 0, width, height, mRoundSize, mRoundSize, mPaint);

        if (isChecked) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mSelectedFillColor);
            canvas.drawRoundRect(mUnselectedStrokeWidth, mUnselectedStrokeWidth, width - mUnselectedStrokeWidth, height - mUnselectedStrokeWidth, mRoundSize, mRoundSize, mPaint);

            String number = String.valueOf(mNumber);
            mPaint.setColor(mNumberTextColor);
            mPaint.setTextSize(mNumberTextSize);
            if (mNumberTextRect == null) {
                mNumberTextRect = new Rect();
                mPaint.getTextBounds(number, 0, number.length(), mNumberTextRect);
            }
            canvas.drawText(String.valueOf(mNumber), width / 2f, (mNumberTextRect.height() + height) / 2f, mPaint);
        }
    }

    public void setNumberTextColor(@ColorInt int numberTextColor) {
        if (mNumberTextColor != numberTextColor) {
            mNumberTextColor = numberTextColor;
            invalidate();
        }
    }

    public void setNumberTextSize(float numberTextSize) {
        if (numberTextSize != mNumberTextSize) {
            mNumberTextSize = numberTextSize;
            mNumberTextRect = null;
            invalidate();
        }
    }

    public void setUnselectedStrokeWidth(float unselectedStrokeWidth) {
        if (unselectedStrokeWidth != mUnselectedStrokeWidth) {
            mUnselectedStrokeWidth = unselectedStrokeWidth;
            invalidate();
        }
    }

    public void setUnselectedStrokeColor(@ColorInt int unselectedStrokeColor) {
        if (mUnselectedStrokeColor != unselectedStrokeColor) {
            mUnselectedStrokeColor = unselectedStrokeColor;
            invalidate();
        }
    }

    public void setSelectedFillColor(@ColorInt int selectedFillColor) {
        if (mSelectedFillColor != selectedFillColor) {
            mSelectedFillColor = selectedFillColor;
            invalidate();
        }
    }

    public void setRoundSize(float roundSize) {
        if (mRoundSize != roundSize) {
            mRoundSize = roundSize;
            invalidate();
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean isChecked);
    }
}
