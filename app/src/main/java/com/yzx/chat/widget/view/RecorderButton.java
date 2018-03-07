package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Outline;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

/**
 * Created by YZX on 2017年12月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class RecorderButton extends android.support.v7.widget.AppCompatImageView {

    private onRecorderTouchListener mListener;
    private boolean isCancel;
    private boolean isTouchOutOfBounds;

    public RecorderButton(Context context) {
        this(context, null);
    }

    public RecorderButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecorderButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCircle();
    }

    private void setCircle() {
        this.setClipToOutline(true);
        this.setOutlineProvider(new CircleOutlineProvider());
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mListener == null) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mListener.onDown();
                isCancel = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isCancel) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) {
                        if (!isTouchOutOfBounds) {
                            isTouchOutOfBounds = true;
                            mListener.onOutOfBoundsChange(true);
                        }
                    } else {
                        if (isTouchOutOfBounds) {
                            isTouchOutOfBounds = false;
                            mListener.onOutOfBoundsChange(false);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isCancel) {
                    mListener.onUp();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!isCancel) {
                    isCancel = true;
                    mListener.onCancel();
                }
                break;
        }


        return true;
    }

    public void stop() {
        if (mListener != null) {
            reset();
            mListener.onUp();
        }
    }

    public void reset() {
        isCancel = true;
    }

    public void setOnRecorderTouchListener(onRecorderTouchListener listener) {
        mListener = listener;
    }

    public interface onRecorderTouchListener {
        void onDown();

        void onUp();

        void onOutOfBoundsChange(boolean isOutOfBounds);

        void onCancel();
    }
}
