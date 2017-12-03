package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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

/**
 * Created by YZX on 2017年12月01日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class EmotionPanelFrameLayout extends FrameLayout {

    private Context mContext;
    private ViewPager mVpMoreInput;
    private List<View> mEmotionPanelList;
    private HorizontalScrollView mTabScrollView;
    private LinearLayout mTabLinearLayout;

    private Paint mSeparationLinePaint ;
    private int mSeparationLineHeight;
    private int mSeparationLineColor;
    private int mTabHeight;
    private int mTabItemPadding;

    public EmotionPanelFrameLayout(Context context) {
        this(context, null);
    }

    public EmotionPanelFrameLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmotionPanelFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setDefaultValue();
        init();
        setWillNotDraw(false);
    }

    private void setDefaultValue() {
        mTabHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, mContext.getResources().getDisplayMetrics());
        mSeparationLineHeight = 1;
        mSeparationLineColor = Color.parseColor("#38000000");
        mSeparationLinePaint = new Paint();
        mSeparationLinePaint.setColor(mSeparationLineColor);
        mSeparationLinePaint.setStrokeWidth(mSeparationLineHeight);
        mTabItemPadding =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
    }

    private void init() {
        FrameLayout.LayoutParams params;

        mVpMoreInput = new ViewPager(mContext);
        mVpMoreInput.setAdapter(mPagerAdapter);
        mVpMoreInput.addOnPageChangeListener(mOnPageChangeListener);
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.TOP;
        params.bottomMargin = mTabHeight;
        mVpMoreInput.setLayoutParams(params);

        mTabLinearLayout = new LinearLayout(mContext);
        mTabLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        mTabScrollView = new HorizontalScrollView(mContext);
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTabHeight);
        params.gravity = Gravity.BOTTOM;
        mTabScrollView.setLayoutParams(params);
        mTabScrollView.addView(mTabLinearLayout, new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

//         mSeparationLineView = new View(mContext);
//         mSeparationLineView.setBackgroundColor(mSeparationLineColor);
//
        addView(mVpMoreInput);
        addView(mTabScrollView);


        mEmotionPanelList = new ArrayList<>(12);
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
        TabView tabView = new TabView(mContext);
        tabView.setImageDrawable(icon);
        tabView.setPadding(mTabItemPadding*4,mTabItemPadding,mTabItemPadding*4,mTabItemPadding);
        mTabLinearLayout.addView(tabView);
        mPagerAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        canvas.drawLine(0, 0, width, 0,mSeparationLinePaint);
        canvas.drawLine(0, height-mTabHeight, width, height-mTabHeight,mSeparationLinePaint);
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
        private boolean isFirstinstantiate = true;
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
            if(isFirstinstantiate){
                mOnPageChangeListener.onPageSelected(position);
                isFirstinstantiate = false;
            }
            return panelView;
        }
    };

    private class TabView extends android.support.v7.widget.AppCompatImageView {

        public TabView(Context context) {
            this(context, null);
        }

        public TabView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public TabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            if(selected){
                setBackgroundColor(mSeparationLineColor);
            }else {
                setBackground(null);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            canvas.drawLine(width-mSeparationLineHeight, 0, width-mSeparationLineHeight, height,mSeparationLinePaint);
        }
    }

}
