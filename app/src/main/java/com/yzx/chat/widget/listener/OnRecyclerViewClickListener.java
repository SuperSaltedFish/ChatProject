package com.yzx.chat.widget.listener;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by YZX on 2017年09月08日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class OnRecyclerViewClickListener implements OnItemTouchListener {

    private static final int TOUCH_SLOP = 10;

    private float mLastDownX;
    private float mLastDownY;
    private boolean isSingleTapUp = false;
    private boolean isLongPressUp = false;
    private boolean isMove = false;
    private long mDownTime;

    public abstract void onItemClick(int position, View itemView);

    public void onItemLongClick(int position, View itemView) {
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownX = x;
                mLastDownY = y;
                mDownTime = System.currentTimeMillis();
                isMove = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMove) {
                    if (Math.abs(x - mLastDownX) > TOUCH_SLOP || Math.abs(y - mLastDownY) > TOUCH_SLOP) {
                        isMove = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    isMove = false;
                    break;
                }
                if (System.currentTimeMillis() - mDownTime > 1000) {
                    isLongPressUp = true;
                } else {
                    isSingleTapUp = true;
                }
                View childView = rv.findChildViewUnder(x, y);
                if (childView != null) {
                    if (isSingleTapUp) {
                        onItemClick(rv.getChildAdapterPosition(childView), childView);
                    } else {
                        onItemLongClick(rv.getChildAdapterPosition(childView), childView);
                    }
                }
                isLongPressUp = false;
                isSingleTapUp = false;
                break;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
