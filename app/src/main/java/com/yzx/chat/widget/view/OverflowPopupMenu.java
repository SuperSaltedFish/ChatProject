package com.yzx.chat.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.util.AndroidUtil;


/**
 * Created by YZX on 2017年09月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


@SuppressLint("RestrictedApi")
public class OverflowPopupMenu extends PopupWindow {

    private Context mContext;
    private ListView mMenuListView;
    private MenuBuilder mMenuBuilder;
    private OnMenuItemClickListener mOnMenuItemClickListener;

    private int mItemPadding;
    private int mItemHeight;
    private int mTextColor;

    public OverflowPopupMenu(@NonNull Context context) {
        this(context, 0);
    }

    public OverflowPopupMenu(@NonNull Context context, @MenuRes int menuRes) {
        super(context);
        mContext = context;
        mMenuListView = new ListView(mContext);
        mMenuListView.setDivider(null);
        mMenuListView.setOnItemClickListener(mOnItemClickListener);
        mMenuListView.setAdapter(mPopupMenuAdapter);
        this.setContentView(mMenuListView);
        this.setOutsideTouchable(false);
        this.setFocusable(true);
        if (menuRes > 0) {
            inflate(menuRes);
        }
        mItemPadding = (int) AndroidUtil.dip2px(12);
        mItemHeight = (int) AndroidUtil.dip2px(48);
        mTextColor = AndroidUtil.getColor(R.color.text_primary_color_black);
    }


    public void inflate(@MenuRes int menuRes) {
        mMenuBuilder = new MenuBuilder(mContext);
        new SupportMenuInflater(mContext).inflate(menuRes, mMenuBuilder);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mOnMenuItemClickListener = listener;
    }

    public int getHeight() {
        return mMenuBuilder == null ? 0 : mMenuBuilder.size() * mItemHeight;
    }

    public MenuItem findMenuById(@IdRes int menuId) {
        return mMenuBuilder.findItem(menuId);
    }

    public void notifyDataSetChanged() {
        mPopupMenuAdapter.notifyDataSetChanged();
    }

    private MenuHolder createHolder() {
        LinearLayout rootLayout = new LinearLayout(mContext);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(mItemPadding, mItemPadding, mItemPadding, mItemPadding);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight));
        rootLayout.setGravity(Gravity.CENTER_VERTICAL);

        ImageView itemIcon = new ImageView(mContext);

        TextView itemText = new TextView(mContext);
        itemText.setGravity(Gravity.CENTER_VERTICAL);
        itemText.setTextColor(mTextColor);

        rootLayout.addView(itemIcon);
        rootLayout.addView(itemText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        MenuHolder holder = new MenuHolder();
        holder.mLlRootLayout = rootLayout;
        holder.mIvIcon = itemIcon;
        holder.mTvTitle = itemText;
        return holder;

    }

    private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mOnMenuItemClickListener != null) {
                mOnMenuItemClickListener.onMenuItemClick(position, mMenuBuilder.getItem(position).getItemId());
            }
            view.post(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            });
        }
    };

    private final BaseAdapter mPopupMenuAdapter = new BaseAdapter() {
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
                MenuHolder holder = createHolder();
                convertView = holder.mLlRootLayout;
                convertView.setTag(holder);
            }
            MenuHolder menuHolder = (MenuHolder) convertView.getTag();
            MenuItem menuItem = mMenuBuilder.getItem(position);
            if (menuItem.getIcon() == null) {
                menuHolder.mIvIcon.setVisibility(View.GONE);
            } else {
                menuHolder.mIvIcon.setVisibility(View.VISIBLE);
                menuHolder.mIvIcon.setImageDrawable(menuItem.getIcon());
            }
            menuHolder.mIvIcon.setImageDrawable(menuItem.getIcon());
            menuHolder.mTvTitle.setText(menuItem.getTitle());
            return convertView;
        }
    };


    private final static class MenuHolder {
        ImageView mIvIcon;
        TextView mTvTitle;
        LinearLayout mLlRootLayout;
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(int position, @IdRes int menuID);
    }
}
