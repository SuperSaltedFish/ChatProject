package com.yzx.chat.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ViewUtil {
    public static void autoScrollAtInput(final Activity activity, final View root, final View scrollToView) {
        if (activity == null || root == null || scrollToView == null) {
            return;
        }
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int scrollHeight;
                        Rect rect = new Rect();
                        //获取root在窗体的可视区域
                        root.getWindowVisibleDisplayFrame(rect);
                        //获取root在窗体的不可视区域高度(被遮挡的高度)
                        int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;
                        //若不可视区域高度大于150，则键盘显示
                        if (rootInvisibleHeight > 150) {
                            //获取scrollToView在窗体的坐标,location[0]为x坐标，location[1]为y坐标
                            int[] location = new int[2];
                            scrollToView.getLocationInWindow(location);
                            //计算root滚动高度，使scrollToView在可见区域的底部
                            scrollHeight = (location[1] + scrollToView.getHeight() + (int) AndroidUtil.dip2px(8)) - rect.bottom;
                        } else {
                            scrollHeight = -root.getScrollY();
                        }
                        View focusView = activity.getCurrentFocus();
                        if (focusView instanceof EditText) {
                            EditText editText = (EditText) focusView;
                            View label = root.findViewById(editText.getLabelFor());
                            if (label != null && label.getY() - rootInvisibleHeight <= 0) {
                                scrollHeight = -root.getScrollY();

                            }
                        }
                        root.scrollBy(0,scrollHeight);
                    }
                });
    }
}
