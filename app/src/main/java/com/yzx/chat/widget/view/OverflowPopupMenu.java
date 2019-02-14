package com.yzx.chat.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;


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

    private int mPaddingStart;
    private int mPaddingEnd;
    private int mPaddingBetweenIconAndTitle;
    private int mMenuItemHeight;

    private int mIconSize;

    private int mTitleTextSize;
    private int mTitleTextColor;

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
        if (menuRes != 0) {
            inflate(menuRes);
        }
        initDefault();
    }

    private void initDefault() {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mPaddingStart = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, displayMetrics);
        mPaddingEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, displayMetrics);
        mPaddingBetweenIconAndTitle = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, displayMetrics);
        mMenuItemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, displayMetrics);
        mIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);
        mTitleTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, displayMetrics);
        mTitleTextColor = Color.BLACK;
    }


    public void inflate(@MenuRes int menuRes) {
        mMenuBuilder = new MenuBuilder(mContext);
        new SupportMenuInflater(mContext).inflate(menuRes, mMenuBuilder);
    }

    private void updateItem(){
        mMenuListView.setAdapter(null);
        mMenuListView.setAdapter(mPopupMenuAdapter);
        update();
    }

    public void setPaddingStart(int paddingStart) {
        mPaddingStart = paddingStart;
        updateItem();
    }

    public void setPaddingEnd(int paddingEnd) {
        mPaddingEnd = paddingEnd;
        updateItem();
    }

    public void setPaddingBetweenIconAndTitle(int paddingBetweenIconAndTitle) {
        mPaddingBetweenIconAndTitle = paddingBetweenIconAndTitle;
        updateItem();
    }

    public void setMenuItemHeight(int menuItemHeight) {
        mMenuItemHeight = menuItemHeight;
        updateItem();
    }

    public void setIconSize(int iconSize) {
        mIconSize = iconSize;
        updateItem();
    }

    public void setTitleTextSize(int titleTextSize) {
        mTitleTextSize = titleTextSize;
        updateItem();
    }

    public void setTitleTextColor(int titleTextColor) {
        mTitleTextColor = titleTextColor;
        updateItem();
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mOnMenuItemClickListener = listener;
    }

    public int getHeight() {
        return mMenuBuilder == null ? 0 : mMenuBuilder.size() * mMenuItemHeight;
    }

    public MenuItem findMenuById(@IdRes int menuId) {
        return mMenuBuilder.findItem(menuId);
    }

    public void notifyDataSetChanged() {
        mPopupMenuAdapter.notifyDataSetChanged();
    }

    private MenuHolder createHolder() {
        LinearLayout rootLayout = new LinearLayout(mContext);
        rootLayout.setOrientation(LinearLayout.HORIZONTAL);
        rootLayout.setPadding(mPaddingStart, 0, mPaddingEnd, 0);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mMenuItemHeight));
        rootLayout.setGravity(Gravity.CENTER_VERTICAL);

        ImageView itemIcon = new ImageView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIconSize, mIconSize);
        params.setMarginEnd(mPaddingBetweenIconAndTitle);


        TextView itemText = new TextView(mContext);
        itemText.setGravity(Gravity.CENTER_VERTICAL);
        itemText.setTextColor(mTitleTextColor);
        itemText.setTextSize(TypedValue.COMPLEX_UNIT_PX,mTitleTextSize);
        itemText.setSingleLine();
        itemText.setEllipsize(TextUtils.TruncateAt.END);

        rootLayout.addView(itemIcon, params);
        rootLayout.addView(itemText);

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
                menuHolder.mIvIcon.setImageDrawable(null);
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
        LinearLayout mLlRootLayout;
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(int position, @IdRes int menuID);
    }
}
