package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.yzx.chat.R;

/**
 * Created by YZX on 2018年01月22日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class LabelView extends android.support.v7.widget.AppCompatTextView {

    private Drawable mClearDrawable;
    private boolean isShowClearDrawable;
    private OnCloseClickListener mCloseClickListener;

    public LabelView(Context context) {
        this(context, null);
    }

    public LabelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClearDrawable = context.getDrawable(R.drawable.ic_close);
        if (mClearDrawable != null) {
            mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
        if (mClearDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            // 判断触摸点是否在水平范围内
            boolean isInnerWidth = (x > (getWidth() - getTotalPaddingRight()))
                    && (x < (getWidth() - getPaddingRight()));
            // 获取删除图标的边界，返回一个Rect对象
            Rect rect = mClearDrawable.getBounds();
            // 获取删除图标的高度
            int height = rect.height();
            int y = (int) event.getY();
            // 计算图标底部到控件底部的距离
            int distance = (getHeight() - height) / 2;
            // 判断触摸点是否在竖直范围内(可能会有点误差)
            // 触摸点的纵坐标在distance到（distance+图标自身的高度）之内，则视为点中删除图标
            boolean isInnerHeight = (y > distance) && (y < (distance + height));
            if (isInnerHeight && isInnerWidth && mCloseClickListener != null) {
                mCloseClickListener.onCloseClick(this);
            }
        }
        return super.onTouchEvent(event);
    }


    public void setCloseIconVisible(boolean visible) {
        if (isShowClearDrawable == visible) {
            return;
        }
        Drawable right = visible ? mClearDrawable : null;
        Drawable[] drawables = getCompoundDrawables();
        setCompoundDrawables(drawables[0], drawables[1], right, drawables[3]);
        isShowClearDrawable = visible;
    }

    public interface OnCloseClickListener {
        void onCloseClick(View v);
    }
}
