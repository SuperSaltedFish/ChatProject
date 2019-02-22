package com.yzx.chat.widget.listener;

import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2017年09月08日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public abstract class OnRecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

    public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
    }

    public void onItemClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
        onItemClick(position, viewHolder);
    }


    public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
    }

    public void onItemDecorationClick(float x, float y) {
    }


    private RecyclerView mRecyclerView;
    private GestureDetectorCompat mGestureDetector;

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        if (mRecyclerView == null) {
            mRecyclerView = rv;
            mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(), new ItemTouchHelperGestureListener());
        }
        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        if (mRecyclerView == null) {
            mRecyclerView = rv;
            mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(), new ItemTouchHelperGestureListener());
        }
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int MAX_CLICK_INTERVAL = 300;

        private long lastClickTime;

        public boolean onSingleTapUp(MotionEvent event) {
            long nowTime = SystemClock.elapsedRealtime();
            if (nowTime - lastClickTime >= MAX_CLICK_INTERVAL) {
                View child = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
                if (child != null) {
                    RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(child);
                    onItemClick(viewHolder.getAdapterPosition(), viewHolder, event.getX(), event.getY());
                } else {
                    onItemDecorationClick(event.getX(), event.getY());
                }
            }
            lastClickTime = nowTime;

            return true;
        }

        public void onLongPress(MotionEvent event) {
            View child = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
            if (child != null) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(child);
                onItemLongClick(viewHolder.getAdapterPosition(), viewHolder, event.getX(), event.getY());
            }
        }


    }
}