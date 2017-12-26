package com.yzx.chat.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yzx.chat.R;


/**
 * Created by YZX on 2017年09月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

@SuppressLint("RestrictedApi")
public class OverflowPopupMenu extends PopupWindow {

    private Context mContext;
    private ListView mMenuListView;
    private MenuBuilder mMenuBuilder;

    public OverflowPopupMenu(@NonNull Context context) {
        this(context, 0);
    }

    public OverflowPopupMenu(@NonNull Context context, @MenuRes int menuRes) {
        super(context);
        mContext = context;
        mMenuListView = new ListView(mContext);
        mMenuListView.setDivider(null);
        mMenuListView.setAdapter(OverflowPopupMenuAdapter);
        this.setContentView(mMenuListView);
        this.setOutsideTouchable(false);
        this.setFocusable(true);
//        this.setAnchorView(anchorView);
//        this.setDropDownGravity(Gravity.END);
//        this.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.theme_main_color)));
//        this.setHorizontalOffset(-(int) AndroidUtil.dip2px(16));
//        this.setWidth((int) AndroidUtil.dip2px(176));
        if (menuRes > 0) {
            inflate(menuRes);
        }
    }


    public void inflate(@MenuRes int menuRes) {
        mMenuBuilder = new MenuBuilder(mContext);
        new SupportMenuInflater(mContext).inflate(menuRes, mMenuBuilder);
        OverflowPopupMenuAdapter.notifyDataSetChanged();
    }

    private final BaseAdapter OverflowPopupMenuAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mMenuBuilder == null ? 0 : mMenuBuilder.size();
        }

        @Override
        public Object getItem(int position) {
            return mMenuBuilder.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_home_overflow, parent, false);
                convertView.setTag(new MenuHolder(convertView));
            }
            MenuHolder menuHolder = (MenuHolder) convertView.getTag();
            MenuItem menuItem = mMenuBuilder.getItem(position);
            if (menuItem.getIcon() == null) {
                menuHolder.mIvIcon.setVisibility(View.GONE);
            } else {
                menuHolder.mIvIcon.setVisibility(View.VISIBLE);
                menuHolder.mIvIcon.setImageDrawable(menuItem.getIcon());
            }
            menuHolder.mTvTitle.setText(menuItem.getTitle());
            return convertView;
        }
    };


    private final static class MenuHolder {
        ImageView mIvIcon;
        TextView mTvTitle;

        MenuHolder(View itemView) {
            mIvIcon = itemView.findViewById(R.id.HomeMenuAdapter_mIvIcon);
            mTvTitle = itemView.findViewById(R.id.HomeMenuAdapter_mTvTitle);
        }
    }
}
