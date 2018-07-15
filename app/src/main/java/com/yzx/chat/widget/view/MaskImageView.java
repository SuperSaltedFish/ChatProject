package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MaskImageView extends ImageView {

    private RadialGradient mMaskGradient;
    private Paint mPaint;

    private int[] mMaskColors;

    public MaskImageView(Context context) {
        this(context, null);
    }

    public MaskImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMaskColors = new int[]{
                Color.TRANSPARENT,
                Color.argb(24, 0, 0, 0),
                Color.argb(72, 0, 0, 0),
                Color.argb(148, 0, 0, 0)
        };
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float centerX = w / 2f;
        float centerY = h / 2f;
        mMaskGradient = new RadialGradient(centerX, centerY, (float) Math.sqrt((centerX * centerX + centerY * centerY)*0.9f), mMaskColors, null, Shader.TileMode.CLAMP);
        mPaint.setShader(mMaskGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);

    }


}
