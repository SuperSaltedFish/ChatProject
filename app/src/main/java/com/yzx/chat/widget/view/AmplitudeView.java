package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by YZX on 2017年12月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class AmplitudeView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "AmplitudeView";

    private Context mContext;
    private SurfaceHolder mSurfaceHolder;
    private final Object mSurfaceLock = new Object();
    private boolean isReset;
    private DrawRunnable mDrawRunnable;
    private HandlerThread mDrawThread;
    private Handler mHandler;

    private float mMaxAmplitude;
    private float mCurrentAmplitude;
    private int mViewWidth;
    private int mViewHeight;
    private String mTimeText;
    private int mTextBaseLine;
    private int mTextPadding;
    private int mTextSize;
    private float mPtsLeft[];
    private float mPtsRight[];
    private int mLineWidth;
    private int mAmplitudeWidth;
    private int mAmplitudeColor;
    private int mLineInterval = 2;
    private Paint mPaint;
    private Random mRandom;


    public AmplitudeView(Context context) {
        this(context, null);
    }

    public AmplitudeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmplitudeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mSurfaceHolder = getHolder();
        init();
    }

    private void init() {
        setFocusable(false);
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);

        mTimeText = "0'";
        mTextPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics());
        mLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
        mLineInterval = 2;
        mAmplitudeColor = Color.RED;
        isReset = true;

        mPaint = new Paint();
        mPaint.setColor(mAmplitudeColor);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mRandom = new Random();
        mDrawRunnable = new DrawRunnable();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawThread = new HandlerThread(TAG) {
            @Override
            protected void onLooperPrepared() {
                mHandler = new Handler(mDrawThread.getLooper());
                mHandler.post(mDrawRunnable);
            }
        };
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (mSurfaceLock) {
            mHandler.removeCallbacksAndMessages(null);
            mDrawThread.quit();
        }
    }

    private void update(boolean isReset) {
        if (mHandler == null) {
            return;
        }
        synchronized (mSurfaceLock) {
            this.isReset = isReset;
            mHandler.post(mDrawRunnable);
        }
    }

    public void resetContent() {
        setCurrentAmplitude(0);
        setTime(0);
    }

    public void setMaxAmplitude(float maxAmplitude) {
        mMaxAmplitude = maxAmplitude;
        update(false);
    }

    public void setCurrentAmplitude(float currentAmplitude) {
        mCurrentAmplitude = currentAmplitude;
        update(false);
    }

    public void setTime(int second) {
        mTimeText = String.valueOf(second) + "'";
        update(false);
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        update(true);
    }

    public void setLineWidth(int lineWidth) {
        mLineWidth = lineWidth;
        update(true);
    }

    public void setAmplitudeColor(int amplitudeColor) {
        mAmplitudeColor = amplitudeColor;
        mPaint.setColor(mAmplitudeColor);
        update(false);
    }

    public void setLineInterval(int lineInterval) {
        mLineInterval = lineInterval;
        update(true);
    }

    public Looper getLooper() {
        if (mHandler != null) {
            return mHandler.getLooper();
        }
        return null;
    }

    private class DrawRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (mSurfaceLock) {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                if (canvas != null) {
                    doDraw(canvas);  //这里做真正绘制的事情
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void doDraw(Canvas canvas) {
            if (isReset) {

                mPaint.setTextSize(mTextSize);

                Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
                mTextBaseLine = (mViewHeight - fontMetrics.bottom - fontMetrics.top) / 2;

                int textWidth = (int) (mPaint.measureText(mTimeText) + 2 * mTextPadding);
                mAmplitudeWidth = (mViewWidth - textWidth) / 2;
                int lineCount = mAmplitudeWidth / (mLineWidth + mLineInterval);
                if (lineCount % 2 == 0) {
                    lineCount++;
                }

                mPtsLeft = new float[lineCount * 4];
                mPtsRight = new float[mPtsLeft.length];
                for (int i = 0, count = mPtsLeft.length; i < count; i = i + 4) {
                    mPtsLeft[i] = (i / 4 + 1) * (mLineWidth + mLineInterval);
                    mPtsLeft[i + 2] = mPtsLeft[i];
                    mPtsRight[i] = mPtsLeft[i] + mAmplitudeWidth + textWidth;
                    mPtsRight[i + 2] = mPtsRight[i];
                }
                isReset = false;
            }
            float maxLineHeight;
            for (int i = 0, count = mPtsLeft.length / 2 + 2; i < count; i = i + 4) {
                maxLineHeight = mViewHeight * mCurrentAmplitude / mMaxAmplitude * (i + 1 + count / 5) / (count + count / 5); //设置最大和最小的高度
                maxLineHeight = maxLineHeight * (mRandom.nextInt(6) + 4) / 10f;
                mPtsLeft[i + 1] = (mViewHeight - maxLineHeight) / 2;
                mPtsLeft[i + 3] = mPtsLeft[i + 1] + maxLineHeight;
                mPtsRight[i + 1] = mPtsLeft[i + 1];
                mPtsRight[i + 3] = mPtsLeft[i + 3];
            }
            for (int i = mPtsLeft.length / 2 + 2, j = mPtsLeft.length / 2 - 3, count = mPtsLeft.length - 2; i < count; i = i + 4, j = j - 4) {
                mPtsLeft[i + 1] = mPtsLeft[j - 2];
                mPtsLeft[i + 3] = mPtsLeft[j];
                mPtsRight[i + 1] = mPtsLeft[i + 1];
                mPtsRight[i + 3] = mPtsLeft[i + 3];
            }

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mPaint.setStrokeWidth(mLineWidth);
            if (mCurrentAmplitude != 0) {
                canvas.drawLines(mPtsLeft, mPaint);
                canvas.drawLines(mPtsRight, mPaint);
            }
            mPaint.setStrokeWidth(1);
            canvas.drawLine(0, mViewHeight / 2, mAmplitudeWidth, mViewHeight / 2, mPaint);
            canvas.drawLine(mViewWidth - mAmplitudeWidth, mViewHeight / 2, mViewWidth, mViewHeight / 2, mPaint);
            canvas.drawText(mTimeText, mViewWidth / 2, mTextBaseLine, mPaint);
        }
    }

}
