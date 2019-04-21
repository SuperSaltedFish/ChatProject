package com.yzx.chat.widget.listener;

import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.yzx.chat.configure.AppApplication;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2017年09月08日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public abstract class OnRecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

    public abstract void onItemClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY);


    public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
    }

    public void onItemDecorationClick(float touchX, float touchY) {
    }

    private static final int MAX_CLICK_INTERVAL = 500;
    private RecyclerView mRecyclerView;

    @Override
    public final boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        mRecyclerView = rv;
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public final void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        mRecyclerView = rv;
        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public final void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private final GestureDetectorCompat mGestureDetector = new GestureDetectorCompat(AppApplication.getAppContext(), new GestureDetector.SimpleOnGestureListener() {
        private long lastClickTime;

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            long nowTime = SystemClock.elapsedRealtime();
            View itemView = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
            if (itemView != null) {
                itemView.drawableHotspotChanged(event.getX(), event.getY());
                itemView.setPressed(true);
                if (nowTime - lastClickTime >= MAX_CLICK_INTERVAL) {
                    RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(itemView);
                    onItemClick(viewHolder.getAdapterPosition(), viewHolder, event.getX(), event.getY());
                    lastClickTime = nowTime;
                }
            } else {
                if (nowTime - lastClickTime >= MAX_CLICK_INTERVAL) {
                    onItemDecorationClick(event.getX(), event.getY());
                    lastClickTime = nowTime;
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            View itemView = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
            if (itemView != null) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(itemView);
                onItemLongClick(viewHolder.getAdapterPosition(), viewHolder, event.getX(), event.getY());
            }
        }
    });

}