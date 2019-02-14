package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by YZX on 2017年12月23日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class EmotionPanelLayout extends FrameLayout {

    private Context mContext;
    private ViewPager mVpMoreInput;
    private List<View> mEmotionPanelList;
    private HorizontalScrollView mTabScrollView;
    private LinearLayout mLlBottomLayout;
    private LinearLayout mTabLayout;

    private ImageView mLeftMenuView;
    private ImageView mRightMenuView;

    private Paint mDividerPaint;
    private int mTabHeight;
    private int mTabWidth;
    private int mTabItemPadding;

    private int mCurrentIndex;

    public EmotionPanelLayout(Context context) {
        this(context, null);
    }

    public EmotionPanelLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmotionPanelLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setDefaultValue();
        init();
        setWillNotDraw(false);
    }

    private void setDefaultValue() {
        mTabHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, mContext.getResources().getDisplayMetrics());
        mTabWidth = (int) (mTabHeight * 5 / 3f);
        mDividerPaint = new Paint();
        mDividerPaint.setColor(Color.parseColor("#cbcfd2"));
        mDividerPaint.setStrokeWidth(1);
        mTabItemPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, mContext.getResources().getDisplayMetrics());
    }

    private void init() {
        mVpMoreInput = new ViewPager(mContext);
        mLlBottomLayout = new LinearLayout(mContext);
        mTabLayout = new LinearLayout(mContext);
        mTabScrollView = new HorizontalScrollView(mContext);
        mLeftMenuView = createTabView();
        mRightMenuView = createTabView();

        mLeftMenuView.setVisibility(View.GONE);
        mRightMenuView.setVisibility(View.GONE);

        LayoutParams params;

        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTabHeight);
        params.gravity = Gravity.BOTTOM;
        mLlBottomLayout.setLayoutParams(params);
        mLlBottomLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLlBottomLayout.setGravity(Gravity.CENTER_VERTICAL);
        mLlBottomLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.bottomMargin = mTabHeight;
        mVpMoreInput.setLayoutParams(params);
        mVpMoreInput.setAdapter(mPagerAdapter);
        mVpMoreInput.addOnPageChangeListener(mOnPageChangeListener);

        mTabLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE|LinearLayout.SHOW_DIVIDER_END);

        addView(mVpMoreInput);
        addView(mLlBottomLayout);

        mLlBottomLayout.addView(mLeftMenuView);
        mLlBottomLayout.addView(mTabScrollView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        mLlBottomLayout.addView(mRightMenuView);

        mTabScrollView.addView(mTabLayout, new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        mEmotionPanelList = new ArrayList<>(12);
    }

    private ImageView createTabView() {
        ImageView tabView = new ImageView(mContext);
        tabView.setLayoutParams(new LinearLayout.LayoutParams(mTabWidth, mTabHeight));
        tabView.setPadding(mTabItemPadding, mTabItemPadding, mTabItemPadding, mTabItemPadding);
        return tabView;
    }

    public void setHeight(@Px int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        } else {
            if (params.height == height)
                return;
            params.height = height;
        }
        setLayoutParams(params);
    }

    public void addEmotionPanelPage(View pageView, Drawable icon) {
        mEmotionPanelList.add(pageView);
        ImageView tabView = createTabView();
        tabView.setImageDrawable(icon);
        mTabLayout.addView(tabView);
        mPagerAdapter.notifyDataSetChanged();
    }

    public void setLeftMenu(Drawable icon, View.OnClickListener listener) {
        mLeftMenuView.setImageDrawable(icon);
        mLeftMenuView.setOnClickListener(listener);
        if (icon == null && listener == null) {
            mLeftMenuView.setVisibility(View.GONE);
        } else {
            mLeftMenuView.setVisibility(View.VISIBLE);
        }
    }

    public void setRightMenu(Drawable icon, View.OnClickListener listener) {
        mRightMenuView.setImageDrawable(icon);
        mRightMenuView.setOnClickListener(listener);
        if (icon == null && listener == null) {
            mRightMenuView.setVisibility(View.GONE);
        } else {
            mRightMenuView.setVisibility(View.VISIBLE);
        }
    }

    public void setTabDividerDrawable(Drawable drawable) {
        mTabLayout.setDividerDrawable(drawable);
        mLlBottomLayout.setDividerDrawable(drawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        canvas.drawLine(0, height - mTabHeight, width, height - mTabHeight, mDividerPaint);
    }

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mTabLayout.getChildAt(mCurrentIndex).setSelected(false);
            mTabLayout.getChildAt(position).setSelected(true);
            mCurrentIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private final PagerAdapter mPagerAdapter = new PagerAdapter() {
        private boolean isFirstInstantiate = true;

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
            View panelView = mEmotionPanelList.get(position);
            container.addView(panelView);
            if (isFirstInstantiate) {
                mOnPageChangeListener.onPageSelected(position);
                isFirstInstantiate = false;
            }
            return panelView;
        }
    };
}
