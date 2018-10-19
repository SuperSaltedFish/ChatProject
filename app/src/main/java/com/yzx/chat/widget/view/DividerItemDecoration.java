package com.yzx.chat.widget.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Px;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class DividerItemDecoration extends RecyclerView.ItemDecoration {


    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HORIZONTAL, VERTICAL})
    public @interface OrientationMode {
    }

    public static DividerItemDecoration createHorizontalDividerItemDecoration(@Px int dividerWidth, @ColorInt int dividerColor, int paddingLeft, int paddingRight) {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(dividerWidth, dividerColor, HORIZONTAL);
        itemDecoration.setHorizontalPadding(paddingLeft, paddingRight);
        return itemDecoration;
    }

    public static DividerItemDecoration createVerticalDividerItemDecoration(@Px int dividerWidth, @ColorInt int dividerColor, int paddingTop, int paddingBottom) {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(dividerWidth, dividerColor, VERTICAL);
        itemDecoration.setVerticalPadding(paddingTop, paddingBottom);
        return itemDecoration;
    }

    private int mDividerWidth;
    private int mDividerColor;
    private int mOrientationMode;
    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;
    private Paint mPaint;

    private RecyclerView mRecyclerView;

    public DividerItemDecoration(@Px int dividerWidth, @ColorInt int dividerColor, @OrientationMode int orientationMode) {
        mDividerWidth = dividerWidth;
        mDividerColor = dividerColor;
        mOrientationMode = orientationMode;
        mPaint = new Paint();
        mPaint.setStrokeWidth(mDividerWidth);
        mPaint.setColor(mDividerColor);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        mRecyclerView = parent;
        switch (mOrientationMode) {
            case HORIZONTAL:
                outRect.bottom = mDividerWidth;
                break;
            case VERTICAL:
                outRect.right = mDividerWidth;
                break;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        View view;
        int childCount = parent.getChildCount();
        float[] lines = new float[childCount * 4];
        switch (mOrientationMode) {
            case HORIZONTAL:
                for (int i = 0, j = 0; i < childCount; i++, j += 4) {
                    view = parent.getChildAt(i);
                    lines[j] = mPaddingLeft;
                    lines[j + 1] = view.getBottom() + mDividerWidth / 2;
                    lines[j + 2] = parent.getWidth() - mPaddingRight;
                    lines[j + 3] = lines[j + 1];
                }
                break;
            case VERTICAL:
                for (int i = 0, j = 0; i < childCount; i++, j += 4) {
                    view = parent.getChildAt(i);
                    lines[j] = view.getRight() + mDividerWidth / 2;
                    lines[j + 1] = mPaddingTop;
                    lines[j + 2] = lines[j];
                    lines[j + 3] = parent.getHeight() - mPaddingBottom;
                }
                break;
            default:
                return;
        }
        c.drawLines(lines, mPaint);
    }

    public void setDividerWidth(int dividerWidth) {
        mDividerWidth = dividerWidth;
        invalidate();
    }

    public void setDividerColor(int dividerColor) {
        mDividerColor = dividerColor;
        invalidate();
    }

    public void setHorizontalPadding(int paddingLeft, int paddingRight) {
        mPaddingLeft = paddingLeft;
        mPaddingRight = paddingRight;
        if (mRecyclerView != null) {
            mRecyclerView.invalidate();
        }
        invalidate();
    }

    public void setVerticalPadding(int paddingTop, int paddingBottom) {
        mPaddingTop = paddingTop;
        mPaddingBottom = paddingBottom;
        invalidate();
    }

    private void invalidate() {
        if (mRecyclerView != null) {
            mRecyclerView.invalidate();
        }
    }
}
