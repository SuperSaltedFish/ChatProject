package com.yzx.chat.widget.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.view.animation.LinearInterpolator;

import com.yzx.chat.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by YZX on 2017年12月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class RecorderButton extends AppCompatImageView {

    private OnRecorderTouchListener mOnRecorderTouchListener;
    private int mDuration;

    private boolean isStarting;

    private Paint mArcPaint;
    private RectF mArcRectF;
    private float mArcWidth;
    private int mProgressArcInsideColor;
    private int mProgressArcOutsideColor;

    private float mCurrentProgress;
    private ValueAnimator mProgressValueAnimator;
    private ViewPropertyAnimator mViewPropertyAnimator;

    public RecorderButton(Context context) {
        this(context, null);
    }

    public RecorderButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public RecorderButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setClipToOutline(true);
        this.setOutlineProvider(new CircleOutlineProvider());
        setScaleX(0.85f);
        setScaleY(0.85f);

        setup();
    }

    private void setup() {
        mArcWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());
        mProgressArcInsideColor = Color.WHITE;
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        mProgressArcOutsideColor = value.data;

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStrokeWidth(mArcWidth);

        mProgressValueAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressValueAnimator.setInterpolator(new LinearInterpolator());
        mProgressValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startRecorderAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (isStarting && (x < 0 || y < 0 || x > getWidth() || y > getHeight())) {
                    stopRecorderAnimation(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                stopRecorderAnimation(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                stopRecorderAnimation(true);
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int cx = width / 2;
        int cy = height / 2;
        int radius;
        if (mArcRectF == null) {
            mArcRectF = new RectF();
        }
        if (width > height) {
            mArcRectF.left = (width - height) / 2f;
            mArcRectF.right = mArcRectF.left + height;
            mArcRectF.top = 0;
            mArcRectF.bottom = height;
            radius = height / 2;
        } else {
            mArcRectF.left = 0;
            mArcRectF.right = width;
            mArcRectF.top = (height - width) / 2f;
            mArcRectF.bottom = mArcRectF.top + width;
            radius = width / 2;
        }

        mArcPaint.setColor(mProgressArcInsideColor);
        canvas.drawCircle(cx, cy, radius, mArcPaint);
        mArcPaint.setColor(mProgressArcOutsideColor);
        canvas.drawArc(mArcRectF, 270, mCurrentProgress * 360, false, mArcPaint);
    }

    private void startRecorderAnimation() {
        if (mViewPropertyAnimator != null) {
            mViewPropertyAnimator.setListener(null);
            mViewPropertyAnimator.cancel();
        }
        mViewPropertyAnimator = animate().scaleX(1f).scaleY(1f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isStarting = true;
                mProgressValueAnimator.setDuration(mDuration);
                mProgressValueAnimator.start();
                if (mOnRecorderTouchListener != null) {
                    mOnRecorderTouchListener.onStart();
                }
            }
        });
    }

    private void stopRecorderAnimation(boolean isCancel) {
        if (mViewPropertyAnimator != null) {
            mViewPropertyAnimator.setListener(null);
            mViewPropertyAnimator.cancel();
        }
        mViewPropertyAnimator = animate().scaleX(0.85f).scaleY(0.85f).setDuration(300);
        mProgressValueAnimator.cancel();
        if (isStarting) {
            if (mOnRecorderTouchListener != null) {
                if (isCancel) {
                    mOnRecorderTouchListener.onCancel();
                } else {
                    mOnRecorderTouchListener.onFinish();
                }
            }
        }
        isStarting = false;
        mCurrentProgress = 0;
        invalidate();
    }

    public void reset() {
        stopRecorderAnimation(true);
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setArcWidth(float arcWidth) {
        mArcWidth = arcWidth;
    }

    public void setProgressArcInsideColor(int progressArcInsideColor) {
        mProgressArcInsideColor = progressArcInsideColor;
    }

    public void setProgressArcOutsideColor(int progressArcOutsideColor) {
        mProgressArcOutsideColor = progressArcOutsideColor;
    }

    public void setOnRecorderTouchListener(OnRecorderTouchListener listener) {
        mOnRecorderTouchListener = listener;
    }

    public interface OnRecorderTouchListener {
        void onStart();

        void onFinish();

        void onCancel();
    }

    private static class CircleOutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            int width = view.getWidth();
            int height = view.getHeight();
            int minSize = Math.min(width, height);
            int left = (width - minSize) / 2;
            int top = (height - minSize) / 2;
            outline.setRoundRect(left, top, left + minSize, top + minSize, minSize / 2f);
        }
    }

}
