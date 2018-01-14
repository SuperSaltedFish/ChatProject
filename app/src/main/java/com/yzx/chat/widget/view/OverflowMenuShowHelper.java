package com.yzx.chat.widget.view;

import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.util.AndroidUtil;

/**
 * Created by YZX on 2018年01月14日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class OverflowMenuShowHelper {
    public static void show(View anchor, OverflowPopupMenu menu, int parentHeight, int touchX, int touchY) {
        int menuWidth = menu.getWidth();
        int menuHeight = menu.getHeight();
        int offsetY = -(anchor.getBottom()-touchY);
        int offsetX;
        if (touchX > AndroidUtil.getScreenWidth() / 2) {
            offsetX = touchX - menuWidth;
            if (parentHeight / 2 > touchY) {
                menu.setAnimationStyle(R.style.PopupMenuAnimation_Right_Top);
            } else {
                offsetY -= menuHeight;
                menu.setAnimationStyle(R.style.PopupMenuAnimation_Right_Bottom);
            }
        } else {
            offsetX = touchX;
            if (parentHeight / 2 > touchY) {
                menu.setAnimationStyle(R.style.PopupMenuAnimation_Left_Top);
            } else {
                offsetY -= menuHeight;
                menu.setAnimationStyle(R.style.PopupMenuAnimation_Left_Bottom);
            }
        }

        menu.showAsDropDown(anchor, offsetX, offsetY);
    }
}
