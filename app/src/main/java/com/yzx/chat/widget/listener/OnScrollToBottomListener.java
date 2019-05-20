package com.yzx.chat.widget.listener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2019年05月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public abstract class OnScrollToBottomListener extends RecyclerView.OnScrollListener {

    public abstract void onScrollToBottom();

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) manager;
            int totalCount = linearManager.getItemCount();
            if (linearManager.findLastVisibleItemPosition() == totalCount - 1) {
                recyclerView.post(mCallRunnable);
            }
        }
    }

    private final Runnable mCallRunnable = new Runnable() {
        @Override
        public void run() {
            onScrollToBottom();
        }
    };
}
