package com.yzx.chat.widget.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by YZX on 2017年11月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class KeyboardPanelSwitcher extends LinearLayout {

    private int mInitMeasureHeight;
    private int mLastMeasureHeight;

    private boolean isInitComplete;

    private OnSoftKeyBoardSwitchListener mOnKeyBoardSwitchListener;

    private Window mWindow;

    public KeyboardPanelSwitcher(Context context) {
        this(context, null);
    }

    public KeyboardPanelSwitcher(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardPanelSwitcher(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (context instanceof Activity) {
            mWindow = ((Activity) context).getWindow();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWindow != null) {
            if (mWindow.getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {

            } else {

            }
        }
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (!isInitComplete) {
            mInitMeasureHeight = measureHeight;
            mLastMeasureHeight = measureHeight;
            return;
        }
        if (measureHeight == mLastMeasureHeight) {
            return;
        }
        if (mOnKeyBoardSwitchListener != null) {
            if (mInitMeasureHeight > measureHeight) {
                mOnKeyBoardSwitchListener.onSoftKeyBoardOpened(mInitMeasureHeight - measureHeight);
            } else {
                mOnKeyBoardSwitchListener.onSoftKeyBoardClosed();
            }
        }
        mLastMeasureHeight = measureHeight;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        isInitComplete = true;
    }

    public void setOnKeyBoardSwitchListener(OnSoftKeyBoardSwitchListener onKeyBoardSwitchListener) {
        mOnKeyBoardSwitchListener = onKeyBoardSwitchListener;
    }

    public interface OnSoftKeyBoardSwitchListener {
        void onSoftKeyBoardOpened(int keyBoardHeight);

        void onSoftKeyBoardClosed();
    }
}
