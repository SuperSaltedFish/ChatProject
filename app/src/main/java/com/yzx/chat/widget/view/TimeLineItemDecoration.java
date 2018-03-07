package com.yzx.chat.widget.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by YZX on 2017年09月05日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class TimeLineItemDecoration extends RecyclerView.ItemDecoration {

    private Paint mPaint;
    private Drawable mTimePointDrawable;
    private int mTimePointOffsetY;
    private int mTimePointSize;
    private int mTimeLineWidth;


    public TimeLineItemDecoration() {
        mPaint = new Paint();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = mTimeLineWidth;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        View view;
        float lineStartX = mTimeLineWidth / 2f;
        int pointStartX = (mTimeLineWidth - mTimePointSize) / 2;
        RecyclerView.LayoutParams params;
        Rect pointRect = new Rect(pointStartX, 0, pointStartX + mTimePointSize, 0);
        for (int i = 0, childCount = parent.getChildCount(); i < childCount; i++) {
            view = parent.getChildAt(i);
            params = (RecyclerView.LayoutParams) view.getLayoutParams();
            c.drawLine(lineStartX, view.getTop()-params.topMargin, lineStartX, view.getBottom()+params.bottomMargin, mPaint);
            if (mTimePointDrawable != null) {
                pointRect.top = view.getTop() + mTimePointOffsetY;
                pointRect.bottom = pointRect.top + mTimePointSize;
                mTimePointDrawable.setBounds(pointRect);
                mTimePointDrawable.draw(c);
            }
        }
    }

    public TimeLineItemDecoration setTimeLineColor(@ColorInt int timeLineColor) {
        mPaint.setColor(timeLineColor);
        return this;
    }

    public TimeLineItemDecoration setLineWidth(@Px int lineWidth) {
        mPaint.setStrokeWidth(lineWidth);
        return this;
    }

    public TimeLineItemDecoration setTimeLineWidth(int timeLineWidth) {
        mTimeLineWidth = timeLineWidth;
        return this;
    }

    public TimeLineItemDecoration setTimePointDrawable(Drawable timePointDrawable) {
        mTimePointDrawable = timePointDrawable;
        return this;
    }

    public TimeLineItemDecoration setTimePointOffsetY(int timePointOffsetY) {
        mTimePointOffsetY = timePointOffsetY;
        return this;
    }

    public TimeLineItemDecoration setTimePointSize(int timePointSize) {
        mTimePointSize = timePointSize;
        return this;
    }


    public Paint getPaint() {
        return mPaint;
    }
}
