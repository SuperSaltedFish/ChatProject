package com.yzx.chat.widget.adapter;

import android.content.Context;
import android.support.annotation.MenuRes;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;

/**
 * Created by YZX on 2017年09月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class HomeMenuAdapter extends BaseAdapter {

    private MenuBuilder mMenu;

    private Context mContext;

    public HomeMenuAdapter(Context context, @MenuRes int menuRes) {
        mContext = context;
        mMenu = new MenuBuilder(mContext);
        new SupportMenuInflater(mContext).inflate(menuRes, mMenu);
    }

    @Override
    public int getCount() {
        return mMenu.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_home_overflow, parent, false);
            convertView.setTag(new ItemViewHolder(convertView));
        }
        ItemViewHolder viewHolder = (ItemViewHolder) convertView.getTag();
        MenuItem menuItem = mMenu.getItem(position);
        viewHolder.mIvIcon.setImageDrawable(menuItem.getIcon());
        viewHolder.mTvTitle.setText(menuItem.getTitle());
        return convertView;

    }

    private final static class ItemViewHolder {
        ImageView mIvIcon;
        TextView mTvTitle;

        ItemViewHolder(View itemView) {
            mIvIcon = (ImageView) itemView.findViewById(R.id.HomeMenuAdapter_mIvIcon);
            mTvTitle = (TextView) itemView.findViewById(R.id.HomeMenuAdapter_mTvTitle);
        }
    }

}
