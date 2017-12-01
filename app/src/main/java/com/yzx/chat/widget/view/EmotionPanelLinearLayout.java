package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by YZX on 2017年12月01日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class EmotionPanelLinearLayout extends LinearLayout {

    private Context mContext;
    private ViewPager mVpMoreInput;
    private List<Pair<View, Drawable>> mEmotionPanelList;

    public EmotionPanelLinearLayout(Context context) {
        this(context, null);
    }

    public EmotionPanelLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmotionPanelLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.BOTTOM);
        mVpMoreInput = new ViewPager(mContext);
        addView(mVpMoreInput, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mVpMoreInput.setAdapter(mPagerAdapter);
        mEmotionPanelList = new ArrayList<>(12);
    }

    public void setHeight(@Px int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        } else {
            params.height = height;
        }
        setLayoutParams(params);
    }

    public void addEmotionPanePage(View pageView, Drawable icon) {
        mEmotionPanelList.add(new Pair<>(pageView, icon));
        mPagerAdapter.notifyDataSetChanged();
    }

    private final PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mEmotionPanelList == null ? 0 : mEmotionPanelList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View panelView = mEmotionPanelList.get(position).first;
            container.addView(panelView);
            return panelView;
        }
    };

}
