package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by YZX on 2018年07月23日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VisualizerView extends View {

    private byte[] mBytes;
    private float[] mPoints;
    private Paint mForePaint;

    private float mStrokeWidth;
    private int mStrokeColor;

    public VisualizerView(Context context) {
        this(context, null);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mForePaint = new Paint();
        mForePaint.setAntiAlias(true);
        setDefault(context.getResources().getDisplayMetrics());
    }

    private void setDefault(DisplayMetrics metrics) {
        setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics));
        setStrokeColor(Color.WHITE);
    }


    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    public void reset() {
        mBytes = null;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        float width = getWidth();
        float halfHeight = height / 2f;
        if (mBytes == null) {
            canvas.drawLine(0, halfHeight, width, halfHeight, mForePaint);
            return;
        }

        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }

        for (int i = 0; i < mBytes.length - 1; i++) {
            if (i == 0) {
                mPoints[i * 4] = width * i / (mBytes.length - 1);
                mPoints[i * 4 + 1] = ((128 + ((byte) (mBytes[i] + 128))) * halfHeight) / 128;
            } else {
                mPoints[i * 4] = mPoints[(i - 1) * 4 + 2];
                mPoints[i * 4 + 1] = mPoints[(i - 1) * 4 + 3];
            }
            mPoints[i * 4 + 2] = width * (i + 1) / (mBytes.length - 1);
            mPoints[i * 4 + 3] = ((128 + ((byte) (mBytes[i + 1] + 128))) * halfHeight) / 128;
        }

        canvas.drawLines(mPoints, mForePaint);
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
        mForePaint.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public void setStrokeColor(@ColorInt int strokeColor) {
        mStrokeColor = strokeColor;
        mForePaint.setColor(strokeColor);
        invalidate();
    }
}
