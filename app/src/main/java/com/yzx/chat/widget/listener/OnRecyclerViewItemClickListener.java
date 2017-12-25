package com.yzx.chat.widget.listener;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by YZX on 2017年09月08日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class OnRecyclerViewItemClickListener implements OnItemTouchListener {

    private  RecyclerView mRecyclerView;
    private  GestureDetectorCompat mGestureDetector;

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        if(mRecyclerView ==null){
            mRecyclerView = rv;
            mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(), new ItemTouchHelperGestureListener());
        }
        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if(mRecyclerView ==null){
            mRecyclerView = rv;
            mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(), new ItemTouchHelperGestureListener());
        }
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    public abstract void onItemClick(int position,RecyclerView.ViewHolder viewHolder);

    public void onItemLongClick(int position,RecyclerView.ViewHolder viewHolder,float touchX,float touchY) {

    }

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {

        public boolean onSingleTapUp(MotionEvent event) {
            View child = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
            if (child != null) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(child);
                onItemClick(viewHolder.getAdapterPosition(),viewHolder);
            }
            return true;
        }

        public void onLongPress(MotionEvent event) {
            View child = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
            if (child != null) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(child);
                onItemLongClick(viewHolder.getAdapterPosition(),viewHolder,event.getX(),event.getY());
            }
        }


    }
}
