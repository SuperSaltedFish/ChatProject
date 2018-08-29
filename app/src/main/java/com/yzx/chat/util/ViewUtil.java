package com.yzx.chat.util;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.WeakHashMap;

/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ViewUtil {

    private static WeakHashMap<View, ViewTreeObserver.OnGlobalLayoutListener> sAutoScrollMap;

    public static void registerAutoScrollAtInput(final View scrollView, final View anchor) {
        if (scrollView == null || anchor == null) {
            throw new RuntimeException("scrollView or anchor can not be empty");
        }
        if (sAutoScrollMap == null) {
            sAutoScrollMap = new WeakHashMap<>();
        }
        ViewTreeObserver.OnGlobalLayoutListener listener = sAutoScrollMap.get(scrollView);
        if (listener != null) {
            scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
        listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int scrollHeight;
                Rect rect = new Rect();
                //获取root在窗体的可视区域
                scrollView.getWindowVisibleDisplayFrame(rect);
                //获取root在窗体的不可视区域高度(被遮挡的高度)
                int rootInvisibleHeight = scrollView.getHeight() - rect.bottom;
                //若不可视区域高度大于150，则键盘显示
                if (rootInvisibleHeight > 150) {
                    //获取scrollToView在窗体的坐标,location[0]为x坐标，location[1]为y坐标
                    int[] location = new int[2];
                    anchor.getLocationInWindow(location);
                    //计算root滚动高度，使scrollToView在可见区域的底部
                    scrollHeight = (location[1] + anchor.getHeight() + (int) AndroidUtil.dip2px(8)) - rect.bottom;
                    if (scrollHeight < 0) {
                        return;
                    }
                } else {
                    scrollHeight = -scrollView.getScrollY();
                }
                scrollView.scrollBy(0, scrollHeight);
            }
        };
        sAutoScrollMap.put(scrollView, listener);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    public static void unregisterAutoScrollAtInput(View scrollView) {
        if (scrollView == null || sAutoScrollMap == null || sAutoScrollMap.size() == 0) {
            return;
        }
        sAutoScrollMap.remove(scrollView);
    }

}
