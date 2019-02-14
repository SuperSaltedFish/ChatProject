package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * Created by YZX on 2018年07月23日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VisualizerView extends View {

    private static final int DIVISIONS = 16;
    private static final float MODULATION_STRENGTH = 0.4f; // 0-1
    private static final float AGGRESIVE = 0.5f;
    private static final Matrix EMPTY_MATRIX = new Matrix();

    private byte[] mDefaultPoint;
    private Path mDefaultDisplayPath;

    private byte[] mWaveData;
    private byte[] mFFTData;
    private float[] mWavePoints;
    private float[] mFFTPoints;
    private float[] mFFTCartPoint;
    private float[] mTTFCartPoint2;
    private Paint mVisualizerPaint;
    private Paint mFadePaint;
    private Bitmap mVisualizerBitmap;
    private Canvas mVisualizerCanvas;

    private float mModulation = 0;
    private float mAngleModulation = 0;

    public VisualizerView(Context context) {
        this(context, null);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mVisualizerPaint = new Paint();
        mFadePaint = new Paint();
        mFFTCartPoint = new float[2];
        mTTFCartPoint2 = new float[2];
        setDefault(context.getResources().getDisplayMetrics());
    }

    private void setDefault(DisplayMetrics metrics) {
        mVisualizerPaint.setAntiAlias(true);
        mVisualizerPaint.setStyle(Paint.Style.STROKE);
        mFadePaint.setAntiAlias(true);
        mFadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics));
        setStrokeColor(Color.argb(255, 255, 255, 255));
    }


    public void updateWaveform(byte[] waveData) {
        mWaveData = waveData;
        invalidate();
    }

    public void updateFFT(byte[] fftData) {
        mFFTData = fftData;
        invalidate();
    }

    public void reset() {
        mWaveData = null;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mDefaultDisplayPath==null){
            mDefaultDisplayPath = new Path();
        }
        mDefaultDisplayPath.reset();
        for (int i = 0; i < 61; i++) {
            if (i == 0) {
                mDefaultDisplayPath.moveTo(0, (float) (Math.random() * h/2.5f)+((h- h/2.5f)/2f));
            } else {
                mDefaultDisplayPath.lineTo(w * i / 60, (float) (Math.random() * h/2.5f)+((h- h/2.5f)/2f));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (mWaveData != null || mFFTData != null) {
            if (mVisualizerBitmap == null || mVisualizerBitmap.getWidth() != width || mVisualizerBitmap.getHeight() != height) {
                mVisualizerBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                mVisualizerCanvas = new Canvas(mVisualizerBitmap);
            }
        } else {
          //  canvas.drawLine(0, height / 2f, width, height / 2f, mVisualizerPaint);
            canvas.drawPath(mDefaultDisplayPath,mVisualizerPaint);
            return;
        }

        drawWave(mVisualizerCanvas, width, height);
        drawTTF(mVisualizerCanvas, width, height);
        mVisualizerCanvas.drawPaint(mFadePaint);
        canvas.drawBitmap(mVisualizerBitmap, EMPTY_MATRIX, null);
    }

    private void drawWave(Canvas canvas, float width, float height) {
        float halfHeight = height / 2f;
        if (mWaveData == null) {
            canvas.drawLine(0, halfHeight, width, halfHeight, mVisualizerPaint);
            return;
        }

        if (mWavePoints == null || mWavePoints.length < mWaveData.length * 4) {
            mWavePoints = new float[mWaveData.length * 4];
        }

        for (int i = 0; i < mWaveData.length - 1; i++) {
            if (i == 0) {
                mWavePoints[i * 4] = width * i / (mWaveData.length - 1);
                mWavePoints[i * 4 + 1] = ((128 + ((byte) (mWaveData[i] + 128))) * halfHeight) / 128;
            } else {
                mWavePoints[i * 4] = mWavePoints[(i - 1) * 4 + 2];
                mWavePoints[i * 4 + 1] = mWavePoints[(i - 1) * 4 + 3];
            }
            mWavePoints[i * 4 + 2] = width * (i + 1) / (mWaveData.length - 1);
            mWavePoints[i * 4 + 3] = ((128 + ((byte) (mWaveData[i + 1] + 128))) * halfHeight) / 128;
        }

        canvas.drawLines(mWavePoints, mVisualizerPaint);
    }

    private void drawTTF(Canvas canvas, float width, float height) {
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        if (mFFTData == null) {
            return;
        }

        if (mFFTPoints == null || mFFTPoints.length < mFFTData.length / DIVISIONS * 4) {
            mFFTPoints = new float[mFFTData.length / DIVISIONS * 4];
        }

        for (int i = 0; i < mFFTData.length / DIVISIONS; i++) {
            // Calculate dbValue
            byte rfk = mFFTData[DIVISIONS * i];
            byte ifk = mFFTData[DIVISIONS * i + 1];
            float magnitude = (rfk * rfk + ifk * ifk);
            float dbValue = 75 * (float) Math.log10(magnitude);


            mFFTCartPoint[0] = (float) (i * DIVISIONS) / (mFFTData.length - 1);
            mFFTCartPoint[1] = halfHeight - dbValue / 4;
            toPolar(mFFTCartPoint, halfWidth, halfHeight);
            mFFTPoints[i * 4] = mFFTCartPoint[0];
            mFFTPoints[i * 4 + 1] = mFFTCartPoint[1];


            mTTFCartPoint2[0] = (float) (i * DIVISIONS) / (mFFTData.length - 1);
            mTTFCartPoint2[1] = halfHeight + dbValue;
            toPolar(mTTFCartPoint2, halfWidth, halfHeight);
            mFFTPoints[i * 4 + 2] = mTTFCartPoint2[0];
            mFFTPoints[i * 4 + 3] = mTTFCartPoint2[1];
        }

        canvas.drawLines(mFFTPoints, mVisualizerPaint);

        mModulation += 0.13;
        mAngleModulation += 0.28;
    }

    private void toPolar(float[] cartesian, float centerX, float centerY) {
        double angle = (cartesian[0]) * 2 * Math.PI;
        double radius = (centerX * (1 - AGGRESIVE) + AGGRESIVE * cartesian[1] / 2) * ((1 - MODULATION_STRENGTH) + MODULATION_STRENGTH * (1 + Math.sin(mModulation)) / 2);
        cartesian[0] = (float) (centerX + radius * Math.sin(angle + mAngleModulation));
        cartesian[1] = (float) (centerY + radius * Math.cos(angle + mAngleModulation));
    }

    public void setStrokeWidth(float strokeWidth) {
        mVisualizerPaint.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public void setStrokeColor(@ColorInt int strokeColor) {
        mVisualizerPaint.setColor(strokeColor);
        mFadePaint.setColor(Color.argb(200, ((strokeColor >> 16) & 0xff), ((strokeColor >> 8) & 0xff), (strokeColor & 0xff)));
        invalidate();
    }
}
