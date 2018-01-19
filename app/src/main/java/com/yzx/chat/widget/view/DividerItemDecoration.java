package com.yzx.chat.widget.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private int mDividerWidth;
    private int mDividerColor;
    private Paint mPaint;

    public DividerItemDecoration(@Px int dividerWidth, @ColorInt int dividerColor) {
        mDividerWidth = dividerWidth;
        mDividerColor = dividerColor;
        mPaint = new Paint();
        mPaint.setStrokeWidth(mDividerWidth);
        mPaint.setColor(mDividerColor);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = mDividerWidth;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        View view;
        int childCount = parent.getChildCount();
        float[] lines = new float[childCount*4];
        for (int i = 0,j=0; i < childCount; i++,j+=4) {
            view = parent.getChildAt(i);
            lines[j] = 0;
            lines[j+1] = view.getBottom() ;
            lines[j+2] = parent.getWidth();
            lines[j+3] = lines[j+1];
        }
        c.drawLines(lines, mPaint);
    }
}
