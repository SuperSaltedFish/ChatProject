package com.yzx.chat.widget.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
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
import android.view.animation.LinearInterpolator;

import com.yzx.chat.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by YZX on 2017年12月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class RecorderButton extends AppCompatImageView {

    private Context mContext;

    private OnRecorderTouchListener mOnRecorderTouchListener;
    private boolean isCancel;
    private boolean isTouchOutOfBounds;

    private Paint mArcPaint;
    private RectF mArcRectF;
    private float mArcWidth;
    private int mProgressArcInsideColor;
    private int mProgressArcOutsideColor;

    private float mCurrentProgress;
    private ValueAnimator mProgressValueAnimator;

    public RecorderButton(Context context) {
        this(context, null);
    }

    public RecorderButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public RecorderButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        this.setClipToOutline(true);
        this.setOutlineProvider(new CircleOutlineProvider());

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setAntiAlias(true);

        mArcRectF = new RectF();

        mProgressValueAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressValueAnimator.addUpdateListener(mValueAnimatorUpdateListener);
        mProgressValueAnimator.setInterpolator(new LinearInterpolator());

        setDefault();
    }

    private void setDefault() {
        mArcWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
        mProgressArcInsideColor = Color.WHITE;
        TypedValue value = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        mProgressArcOutsideColor = value.data;
        mArcPaint.setStrokeWidth(mArcWidth);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnRecorderTouchListener == null) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mOnRecorderTouchListener.onDown();
                isCancel = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isCancel) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) {
                        if (!isTouchOutOfBounds) {
                            isTouchOutOfBounds = true;
                            mOnRecorderTouchListener.onOutOfBoundsChange(true);
                        }
                    } else {
                        if (isTouchOutOfBounds) {
                            isTouchOutOfBounds = false;
                            mOnRecorderTouchListener.onOutOfBoundsChange(false);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isCancel) {
                    mOnRecorderTouchListener.onUp();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!isCancel) {
                    isCancel = true;
                    mOnRecorderTouchListener.onCancel();
                }
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
        if (width > height) {
            mArcRectF.left = (width - height) / 2;
            mArcRectF.right = mArcRectF.left + height;
            mArcRectF.top = 0;
            mArcRectF.bottom = height;
            radius = height / 2;
        } else {
            mArcRectF.left = 0;
            mArcRectF.right = width;
            mArcRectF.top = (height - width) / 2;
            mArcRectF.bottom = mArcRectF.top + width;
            radius = width / 2;
        }

        mArcPaint.setColor(mProgressArcInsideColor);
        canvas.drawCircle(cx, cy, radius, mArcPaint);
        mArcPaint.setColor(mProgressArcOutsideColor);
        canvas.drawArc(mArcRectF, 270, mCurrentProgress * 360, false, mArcPaint);
    }

    public void startRecorderAnimation(int duration, Animator.AnimatorListener listener) {
        if (mProgressValueAnimator.isStarted() || mProgressValueAnimator.isRunning()) {
            mProgressValueAnimator.cancel();
        }
        if (listener != null) {
            mProgressValueAnimator.addListener(listener);
        }
        mProgressValueAnimator.setDuration(duration);
        mProgressValueAnimator.start();
    }


    public void reset() {
        isCancel = true;
        mCurrentProgress = 0;
        mProgressValueAnimator.cancel();
        invalidate();
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

    private final ValueAnimator.AnimatorUpdateListener mValueAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentProgress = (float) animation.getAnimatedValue();
            invalidate();
        }
    };

    public interface OnRecorderTouchListener {
        void onDown();

        void onUp();

        void onOutOfBoundsChange(boolean isOutOfBounds);

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
