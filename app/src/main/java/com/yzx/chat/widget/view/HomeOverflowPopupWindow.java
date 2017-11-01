package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ListPopupWindow;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import com.yzx.chat.R;
import com.yzx.chat.tool.AndroidTool;
import com.yzx.chat.widget.adapter.HomeMenuAdapter;

/**
 * Created by YZX on 2017年09月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class HomeOverflowPopupWindow extends ListPopupWindow {

    private ListView mListView;
    private Context mContext;

    public HomeOverflowPopupWindow(@NonNull Context context, View anchorView, @MenuRes int menuRes) {
        super(context);
        mContext = context;
        this.setAdapter(new HomeMenuAdapter(context, menuRes));
        this.setAnchorView(anchorView);
        this.setDropDownGravity(Gravity.END);
        this.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.theme_main_color)));
        this.setHorizontalOffset(-(int) AndroidTool.dip2px(16));
        this.setWidth((int) AndroidTool.dip2px(176));
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setModal(true);
    }

}
