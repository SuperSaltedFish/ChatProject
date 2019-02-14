package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MaskImageView extends ImageView {

    public static final int MASK_MODE_RADIAL = 0;
    public static final int MASK_MODE_LINEAR = 1;

    @IntDef({MASK_MODE_RADIAL, MASK_MODE_LINEAR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MaskType {
    }

    private @MaskType
    int mCurrentMode;

    private RadialGradient mMaskRadialGradient;
    private LinearGradient mMaskLinearGradient;
    private Paint mPaint;

    private final int[] mMaskRadialColors = new int[]{
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.argb(24, 0, 0, 0),
            Color.argb(128, 0, 0, 0)
    };

    private final int[] mMaskLinearColors = new int[]{
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.argb(72, 0, 0, 0)
    };

    public MaskImageView(Context context) {
        this(context, null);
    }

    public MaskImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaskRadialGradient = null;
        mMaskLinearGradient = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mCurrentMode) {
            case MASK_MODE_RADIAL:
                drawRadialMask(canvas, getWidth(), getHeight());
                break;
            case MASK_MODE_LINEAR:
                drawLinearMask(canvas, getWidth(), getHeight());
                break;
        }
    }

    private void drawRadialMask(Canvas canvas, int viewWidth, int viewHeight) {
        float centerX = viewWidth / 2f;
        float centerY = viewHeight / 2f;
        if (mMaskRadialGradient == null) {
            mMaskRadialGradient = new RadialGradient(centerX, centerY, (float) Math.sqrt(centerX * centerX + centerY * centerY) * 1.3f, mMaskRadialColors, null, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(mMaskRadialGradient);
        canvas.drawRect(0, 0, viewWidth, viewHeight, mPaint);
    }

    private void drawLinearMask(Canvas canvas, int viewWidth, int viewHeight) {
        float centerX = viewWidth / 2f;
        float centerY = viewHeight / 2f;
        if (mMaskLinearGradient == null) {
            mMaskLinearGradient = new LinearGradient(centerX, 0, centerX, viewHeight, mMaskLinearColors, null, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(mMaskLinearGradient);
        canvas.drawRect(0, 0, viewWidth, viewHeight, mPaint);
    }

    public void setCurrentMode(@MaskType int currentMode) {
        if (mCurrentMode == currentMode) {
            return;
        }
        mCurrentMode = currentMode;
        invalidate();
    }
}
