package com.yzx.chat.widget.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2017年11月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class KeyboardPanelSwitcher extends LinearLayout {

    private int mInitMeasureHeight;
    private int mCurrentMeasureHeight;

    private boolean isInitComplete;

    private onSoftKeyBoardSwitchListener mOnKeyBoardSwitchListener;


    public KeyboardPanelSwitcher(Context context) {
        this(context, null);
    }

    public KeyboardPanelSwitcher(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardPanelSwitcher(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (!isInitComplete) {
            mInitMeasureHeight = measureHeight;
            mCurrentMeasureHeight = measureHeight;
            return;
        }
        if (measureHeight == mCurrentMeasureHeight) {
            return;
        }
        if (mOnKeyBoardSwitchListener != null) {
            if(mInitMeasureHeight != measureHeight){
                mOnKeyBoardSwitchListener.onSoftKeyBoardOpened(mInitMeasureHeight - measureHeight);
            }else {
                mOnKeyBoardSwitchListener.onSoftKeyBoardClosed();
            }
        }
        mCurrentMeasureHeight = measureHeight;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        isInitComplete = true;
    }

    public void setOnKeyBoardSwitchListener(onSoftKeyBoardSwitchListener onKeyBoardSwitchListener) {
        mOnKeyBoardSwitchListener = onKeyBoardSwitchListener;
    }

    public interface onSoftKeyBoardSwitchListener {
        void onSoftKeyBoardOpened(int keyBoardHeight);

        void onSoftKeyBoardClosed();
    }
}
