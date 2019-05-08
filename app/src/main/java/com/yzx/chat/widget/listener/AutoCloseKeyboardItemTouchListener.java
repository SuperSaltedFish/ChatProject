package com.yzx.chat.widget.listener;

import android.content.Context;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2019年05月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class AutoCloseKeyboardItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        InputMethodManager manager = (InputMethodManager) rv.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(rv.getWindowToken(), 0);
        return false;
    }
}