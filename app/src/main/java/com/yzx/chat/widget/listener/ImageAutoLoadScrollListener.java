package com.yzx.chat.widget.listener;

import androidx.recyclerview.widget.RecyclerView;

import com.yzx.chat.configure.GlideApp;


/**
 * Created by YZX on 2017年08月16日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ImageAutoLoadScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                //当屏幕停止滚动
            case RecyclerView.SCROLL_STATE_DRAGGING:
                //当屏幕滚动且用户使用的触碰或手指还在屏幕上
                GlideApp.with(recyclerView.getContext()).resumeRequests();
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                //由于用户的操作，屏幕产生惯性滑动，停止加载图片
                GlideApp.with(recyclerView.getContext()).pauseRequests();
                break;
        }
    }
}
