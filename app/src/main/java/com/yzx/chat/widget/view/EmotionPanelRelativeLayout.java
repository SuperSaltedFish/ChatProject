package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年12月01日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class EmotionPanelRelativeLayout extends RelativeLayout {

    private Context mContext;
    private ViewPager mVpMoreInput;
    private List<View> mEmotionPanelList;
    private HorizontalScrollView mTabScrollView;
    private LinearLayout mTabLinearLayout;

    private TabView mLeftMenuView;
    private TabView mRightMenuView;

    private Paint mSeparationLinePaint;
    private int mSeparationLineWidth;
    private int mSeparationLineColor;
    private int mTabHeight;
    private int mTabItemPadding;

    public EmotionPanelRelativeLayout(Context context) {
        this(context, null);
    }

    public EmotionPanelRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmotionPanelRelativeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setDefaultValue();
        init();
        setWillNotDraw(false);
    }

    private void setDefaultValue() {
        mTabHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, mContext.getResources().getDisplayMetrics());
        mSeparationLineWidth = 1;
        mSeparationLineColor = Color.parseColor("#38000000");
        mSeparationLinePaint = new Paint();
        mSeparationLinePaint.setColor(mSeparationLineColor);
        mSeparationLinePaint.setStrokeWidth(mSeparationLineWidth);
        mTabItemPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
    }

    private void init() {
        mVpMoreInput = new ViewPager(mContext);
        mTabLinearLayout = new LinearLayout(mContext);
        mTabScrollView = new HorizontalScrollView(mContext);
        mLeftMenuView = new TabView(mContext);
        mRightMenuView =  new TabView(mContext);

        LayoutParams params;

        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mTabHeight);
        params.addRule(ALIGN_PARENT_LEFT);
        params.addRule(ALIGN_PARENT_BOTTOM);
        mLeftMenuView.setLayoutParams(params);
        mLeftMenuView.setSeparationLineMode(TabView.SEPARATION_LINE_LEFT);
        mLeftMenuView.setId(mLeftMenuView.hashCode());

        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mTabHeight);
        params.addRule(ALIGN_PARENT_RIGHT);
        params.addRule(ALIGN_PARENT_BOTTOM);
        mRightMenuView.setLayoutParams(params);
        mRightMenuView.setSeparationLineMode(TabView.SEPARATION_LINE_RIGHT);
        mRightMenuView.setId(mRightMenuView.hashCode());

        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mTabHeight);
        params.addRule(ALIGN_PARENT_BOTTOM);
        params.addRule(END_OF, mLeftMenuView.getId());
        params.addRule(START_OF, mRightMenuView.getId());
        mTabScrollView.setLayoutParams(params);
        mTabScrollView.addView(mTabLinearLayout, new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mTabScrollView.setId(mTabScrollView.hashCode());

        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(ALIGN_PARENT_TOP);
        params.addRule(ABOVE, mTabScrollView.getId());
        mVpMoreInput.setLayoutParams(params);
        mVpMoreInput.setAdapter(mPagerAdapter);
        mVpMoreInput.addOnPageChangeListener(mOnPageChangeListener);

        addView(mLeftMenuView);
        addView(mRightMenuView);
        addView(mVpMoreInput);
        addView(mTabScrollView);

        mEmotionPanelList = new ArrayList<>(12);
    }

    private TabView getTabVIew() {
        TabView tabView = new TabView(mContext);
        tabView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tabView.setPadding(mTabItemPadding * 4, 0, mTabItemPadding * 4, 0);
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
        TabView tabView = getTabVIew();
        tabView.setImageDrawable(icon);
        mTabLinearLayout.addView(tabView);
        mPagerAdapter.notifyDataSetChanged();

    }

    public void setLeftMenu(Drawable icon, View.OnClickListener listener) {
        mLeftMenuView.setImageDrawable(icon);
        mLeftMenuView.setOnClickListener(listener);
        if (icon == null && listener == null) {
            mLeftMenuView.setPadding(0, 0, 0, 0);
        } else {
            mLeftMenuView.setPadding(mTabItemPadding * 4, 0, mTabItemPadding * 4, 0);
        }
    }

    public void setRightMenu(Drawable icon, View.OnClickListener listener) {
        mRightMenuView.setImageDrawable(icon);
        mRightMenuView.setOnClickListener(listener);
        if (icon == null && listener == null) {
            mRightMenuView.setPadding(0, 0, 0, 0);
        } else {
            mRightMenuView.setPadding(mTabItemPadding * 4, 0, mTabItemPadding * 4, 0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        canvas.drawLine(0, height - mTabHeight, width, height - mTabHeight, mSeparationLinePaint);
    }

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mTabLinearLayout.getChildAt(position).setSelected(true);
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

    private class TabView extends android.support.v7.widget.AppCompatImageView {

        public static final int SEPARATION_LINE_LEFT = 1;
        public static final int SEPARATION_LINE_RIGHT = 2;

        private int mSeparationLineMode;

        public TabView(Context context) {
            this(context, null);
        }

        public TabView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public TabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            mSeparationLineMode = SEPARATION_LINE_LEFT;
        }

        public void setSeparationLineMode(int separationLineMode) {
            mSeparationLineMode = separationLineMode;
        }

        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            if (selected) {
                setBackgroundColor(mSeparationLineColor);
            } else {
                setBackground(null);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (isSelected()) {
                return;
            }
            int width = getWidth();
            int height = getHeight();
            if (mSeparationLineMode == SEPARATION_LINE_LEFT) {
                canvas.drawLine(width - mSeparationLineWidth, 0, width - mSeparationLineWidth, height, mSeparationLinePaint);
            } else {
                canvas.drawLine(0, 0, 0, height, mSeparationLinePaint);
            }
        }
    }

}
