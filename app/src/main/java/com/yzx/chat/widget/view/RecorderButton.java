package com.yzx.chat.widget.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2017年12月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class RecorderButton extends View {

    private onRecorderTouchListener mListener;
    private boolean isCancel;

    public RecorderButton(Context context) {
        this(context, null);
    }

    public RecorderButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecorderButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mListener==null){
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mListener.onStatr();
                isCancel= false;
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) {
                    isCancel = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!isCancel){
                    mListener.onStop();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mListener.onCancel();
                break;
        }


        return true;
    }

    public interface onRecorderTouchListener{
        void onStatr();
        void onStop();
        void onCancel();
    }
}
