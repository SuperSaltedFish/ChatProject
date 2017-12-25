package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.ListPopupWindow;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.HomeMenuAdapter;

/**
 * Created by YZX on 2017年09月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class OverflowPopupMenu extends ListPopupWindow implements BaseAdapter {

    private Context mContext;
    private MenuBuilder mMenuBuilder;

    public OverflowPopupMenu(@NonNull Context context, View anchorView, @MenuRes int menuRes) {
        super(context);
        mContext = context;
        this.setAdapter(new HomeMenuAdapter(context, menuRes));
        this.setAnchorView(anchorView);
        this.setDropDownGravity(Gravity.END);
        this.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.theme_main_color)));
        this.setHorizontalOffset(-(int) AndroidUtil.dip2px(16));
        this.setWidth((int) AndroidUtil.dip2px(176));
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setModal(true);
    }

    public void inflate(@MenuRes int menuRes){
        mMenuBuilder = new MenuBuilder(mContext);
        MenuInflater
        new SupportMenuInflater(mContext).inflate(menuRes, mMenu);
    }


    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_home_overflow, parent, false);
            convertView.setTag(new MenuHolder(convertView));
        }
        MenuHolder menuHolder = (MenuHolder) convertView.getTag();
        MenuItem menuItem = mMenu.getItem(position);
        menuHolder.mIvIcon.setImageDrawable(menuItem.getIcon());
        menuHolder.mTvTitle.setText(menuItem.getTitle());
        return convertView;
    }

    private final static class MenuHolder {
        ImageView mIvIcon;
        TextView mTvTitle;

        MenuHolder(View itemView) {
            mIvIcon = itemView.findViewById(R.id.HomeMenuAdapter_mIvIcon);
            mTvTitle = itemView.findViewById(R.id.HomeMenuAdapter_mTvTitle);
        }
    }
}
