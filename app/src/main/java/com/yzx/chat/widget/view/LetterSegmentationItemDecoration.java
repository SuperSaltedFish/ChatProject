package com.yzx.chat.widget.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import com.yzx.chat.util.AndroidHelper;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by YZX on 2017年06月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class LetterSegmentationItemDecoration extends RecyclerView.ItemDecoration {

    private float mTextSize;
    private float mLineWidth;
    private int mTextColor;
    private int mLineColor;

    private float mTextHeight;
    private int mSpace;
    private float mStartDrawX;
    private float mWidth;

    private TextPaint mTextPaint;
    private Paint mLinePaint;

    private boolean isUninitialized;


    public LetterSegmentationItemDecoration() {
        super();
        isUninitialized = true;
    }

    private void init() {
        isUninitialized = false;
        mSpace = (int) (mTextSize * 1.4);

        mLinePaint = new Paint();
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        mTextHeight = Math.abs(mTextPaint.getFontMetrics().ascent);
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (isUninitialized) {
            mStartDrawX = parent.getPaddingLeft() + AndroidHelper.dip2px(16);
            mWidth = parent.getWidth() - mStartDrawX - parent.getPaddingRight();
            init();
        }
        if (view.getTag() != null) {
            outRect.top = mSpace;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        float top;
        View view;
        String letter;
        float textWidth;
        for (int i = 0, childCount = parent.getChildCount(); i < childCount; i++) {
            view = parent.getChildAt(i);
            letter = (String) view.getTag();
            if (letter != null) {
                if("~".equals(letter)){
                    letter = "#";
                }
                top = view.getTop() - mSpace;
                textWidth = mTextPaint.measureText(letter);
                c.drawLine(mStartDrawX + textWidth , top + mSpace / 2f, mStartDrawX+mWidth, top + mSpace / 2f, mLinePaint);
                c.drawText(letter, mStartDrawX - textWidth / 2f, top + mSpace - (mSpace - mTextHeight) / 2, mTextPaint);
            }
        }
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        mTextColor = textColor;
    }

    public int getLineColor() {
        return mLineColor;
    }

    public void setLineColor(@ColorInt int lineColor) {
        mLineColor = lineColor;
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
    }

    public int getSpace() {
        return mSpace;
    }
}
