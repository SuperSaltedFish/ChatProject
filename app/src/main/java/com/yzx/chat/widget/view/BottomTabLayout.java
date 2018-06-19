package com.yzx.chat.widget.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2018年06月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class BottomTabLayout extends LinearLayout {

    private Context mContext;
    private ViewPager mViewPager;

    private boolean isClicked;

    public BottomTabLayout(Context context) {
        this(context, null);
    }

    public BottomTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setMotionEventSplittingEnabled(false);
    }


    public void addTab(@DrawableRes int icon, String title, @ColorInt int selectedColor) {
        Drawable selectedDrawable = ContextCompat.getDrawable(mContext, icon);
        Drawable unselectedDrawable = ContextCompat.getDrawable(mContext, icon);
        selectedDrawable.setTint(selectedColor);
        addTab(selectedDrawable, unselectedDrawable, title);
    }

    public void addTab(@DrawableRes int selectedIcon, @DrawableRes int unselectedIcon, String title) {
        addTab(ContextCompat.getDrawable(mContext, selectedIcon), ContextCompat.getDrawable(mContext, unselectedIcon), title);
    }

    public void addTab(Drawable selectedIcon, Drawable unselectedIcon, String title) {
        TabView tabView = TabView.create(mContext, selectedIcon, unselectedIcon, title);
        tabView.setOnClickListener(mOnTabClickListener);
        tabView.setTag(getChildCount());
        addView(tabView, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
    }

    public void setupWithViewPager(ViewPager viewPager) {
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
            mViewPager = null;
        }
        if (viewPager != null) {
            mViewPager = viewPager;
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        }
    }

    public void setBadge(int position, int number) {
        if(position>=getChildCount()){
            return;
        }
        TabView tabView = (TabView) getChildAt(position);
        tabView.setBadge(number);
    }

    private final View.OnClickListener mOnTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int childIndex = (int) v.getTag();
            if (mViewPager != null && mViewPager.getCurrentItem() == childIndex) {
                return;
            }
            TabView tabView;
            for (int i = 0, count = getChildCount(); i < count; i++) {
                tabView = (TabView) getChildAt(i);
                if (childIndex != i) {
                    tabView.startUnselectedAnimator();
                } else {
                    tabView.startSelectionAnimator();
                }
            }
            if (mViewPager != null) {
                PagerAdapter adapter = mViewPager.getAdapter();
                if (adapter != null && adapter.getCount() > childIndex) {
                    isClicked = true;
                    mViewPager.setCurrentItem(childIndex);
                }
            }
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (!isClicked && position < getChildCount()) {
                TabView tabView = (TabView) getChildAt(position);
                if (tabView != null) {
                    tabView.setSelectionAnimatorProgress(1 - positionOffset);
                }
            }
            if (!isClicked && position + 1 < getChildCount()) {
                TabView tabView = (TabView) getChildAt(position + 1);
                if (tabView != null) {
                    tabView.setSelectionAnimatorProgress(positionOffset);
                }
            }
            if (isClicked && mViewPager != null && position == positionOffset && position == mViewPager.getCurrentItem()) {
                isClicked = false;
            }
            LogUtil.e("state   " + position + "   " + positionOffset);
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    private static class TabView extends ConstraintLayout {

        private static final long ANIMATOR_MAX_DURATION = 128;

        public static TabView create(Context context, Drawable selectedIcon, Drawable unselectedIcon, String title) {
            TabView tabView = new TabView(context);
            tabView.mIvIconSelected.setImageDrawable(selectedIcon);
            tabView.mIvIconUnselected.setImageDrawable(unselectedIcon);
            tabView.mTvTitle.setText(title);
            return tabView;
        }

        private ImageView mIvIconSelected;
        private ImageView mIvIconUnselected;
        private TextView mTvTitle;
        private BadgeView mBadgeView;

        private ValueAnimator mAnimator;
        private float mCurrentSelectionProgress;
        private final float mTranslation;

        public TabView(Context context) {
            this(context, null);
        }

        public TabView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            LayoutInflater.from(context).inflate(R.layout.view_tab_view, this, true);
            mIvIconSelected = findViewById(R.id.TabView_mIvIconSelected);
            mIvIconUnselected = findViewById(R.id.TabView_mIvIconUnselected);
            mTvTitle = findViewById(R.id.TabView_mTvTitle);
            mBadgeView = findViewById(R.id.TabView_mBadgeView);
            mTranslation = context.getResources().getDisplayMetrics().density * 2;
            setBackground(new RippleDrawable(new ColorStateList(new int[][]{{}}, new int[]{0x56000000}), null, null));
            initAnimator();
            mCurrentSelectionProgress = -1;
            setSelectionAnimatorProgress(0);
        }

        private void initAnimator() {
            mAnimator = new ValueAnimator();
            mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setSelectionAnimatorProgress((float) animation.getAnimatedValue(), false);
                }
            });
        }

        public float getCurrentSelectionProgress() {
            return mCurrentSelectionProgress;
        }

        void setSelectionAnimatorProgress(@FloatRange(from = 0, to = 1f) float progress) {
            setSelectionAnimatorProgress(progress, true);
        }

        private void setSelectionAnimatorProgress(float progress, boolean isCancelAnimator) {
            if (isCancelAnimator) {
                cancelSelectionAnimator();
            }
            if (mCurrentSelectionProgress != progress) {
                mIvIconSelected.setTranslationY(-mTranslation * progress);
                mIvIconUnselected.setTranslationY(-mTranslation * progress);
                mIvIconSelected.setImageAlpha((int) (255 * progress));
                mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f + progress * 2f);
                mCurrentSelectionProgress = progress;
            }
        }

        void startSelectionAnimator() {
            if (mCurrentSelectionProgress == 1) {
                return;
            }
            cancelSelectionAnimator();
            mAnimator.setDuration((long) (ANIMATOR_MAX_DURATION * (1 - mCurrentSelectionProgress)));
            mAnimator.setFloatValues(mCurrentSelectionProgress, 1);
            mAnimator.start();
        }

        void startUnselectedAnimator() {
            if (mCurrentSelectionProgress == 0) {
                return;
            }
            cancelSelectionAnimator();
            mAnimator.setDuration((long) (ANIMATOR_MAX_DURATION * (mCurrentSelectionProgress)));
            mAnimator.setFloatValues(mCurrentSelectionProgress, 0);
            mAnimator.start();
        }

        void setBadge(int number){
            mBadgeView.setBadgeNumber(number);
        }

        private void cancelSelectionAnimator() {
            if (mAnimator.isStarted()) {
                mAnimator.cancel();
            }
        }
    }
}
