package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by YZX on 2017年05月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class CarouselView extends FrameLayout {

    private ViewPager mViewPager;
    private ViewPagerIndicator mViewPagerIndicator;
    private Context mContext;
    private List<String> mPicUrlList;
    private LinkedList<WeakReference<ImageView>> mCacheViewQueue;
    private Handler mHandler;
    private Runnable mCarouselRunnable;
    private int mCarouselInterval = 0;
    private ImageView.ScaleType mImageScaleType;
    private int mIndicatorGravity;

    private boolean isVisibility;


    public CarouselView(Context context) {
        this(context, null);
    }

    public CarouselView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarouselView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CarouselView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mHandler = new Handler();
        init();
        setDefaultValue();
    }


    private void init() {
        mViewPager = new ViewPager(mContext);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPagerIndicator = new ViewPagerIndicator(mContext);
        mCacheViewQueue = new LinkedList<>();

        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        addView(mViewPager);
        addView(mViewPagerIndicator);
    }

    private void setDefaultValue() {
        mIndicatorGravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = mIndicatorGravity;
        int defaultMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
        params.setMargins(defaultMargin, defaultMargin, defaultMargin, defaultMargin);
        mViewPagerIndicator.setLayoutParams(params);
        mImageScaleType = ImageView.ScaleType.CENTER_CROP;
    }


    private void refresh() {
        if (mPicUrlList != null && mPicUrlList.size() > 0) {
            int picUrlCount = mPicUrlList.size();
            mViewPagerIndicator.setIndicatorCount(picUrlCount,0);
            int pagerCount = mPagerAdapter.getCount();
            mViewPager.setCurrentItem(pagerCount / 2 - (pagerCount / 2) % picUrlCount);
        }
        mPagerAdapter.notifyDataSetChanged();
    }

    private void delayedShowNext() {
        mHandler.removeCallbacksAndMessages(null);
        if (mCarouselRunnable == null) {
            mCarouselRunnable = new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                }
            };
        }
        mHandler.postDelayed(mCarouselRunnable, mCarouselInterval);
    }

    private final PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            if (mPicUrlList == null || mPicUrlList.size() == 0) {
                return 0;
            }
            if (mPicUrlList.size() == 1) {
                return 1;
            }
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView itemView = (ImageView) object;
            container.removeView(itemView);
            GlideUtil.clear(mContext, itemView);
            itemView.setImageBitmap(null);
            mCacheViewQueue.offer(new WeakReference<>(itemView));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            isVisibility = true;
            WeakReference<ImageView> weakReference = mCacheViewQueue.poll();
            ImageView itemView;
            if (weakReference != null && weakReference.get() != null) {
                itemView = weakReference.get();
            } else {
                itemView = new ImageView(mContext);
            }
            itemView.setScaleType(mImageScaleType);
            container.addView(itemView);
            int listSize = mPicUrlList.size();
            GlideUtil.loadFromUrl(mContext, itemView, mPicUrlList.get(position % listSize));
            return itemView;
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(final int position) {
           mViewPagerIndicator.setSelectedIndex(position % mPicUrlList.size());
            if (mCarouselInterval != 0) {
                delayedShowNext();
            }
        }
    };


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            if (isVisibility) {
                delayedShowNext();
            }
        } else {
            mHandler.removeCallbacksAndMessages(null);
        }

    }

    public int getCarouselInterval() {
        return mCarouselInterval;
    }

    public void setCarouselInterval(int carouselInterval) {
        mCarouselInterval = carouselInterval;
        delayedShowNext();
    }

    public List<String> getPicUrls() {
        return mPicUrlList;
    }

    public void setPicUrls(List<String> picUrls) {
        mPicUrlList = picUrls;
        refresh();
    }

    public ImageView.ScaleType getImageScaleType() {
        return mImageScaleType;
    }

    public void setImageScaleType(ImageView.ScaleType imageScaleType) {
        mImageScaleType = imageScaleType;
    }

    public ViewPagerIndicator getViewPagerIndicator() {
        return mViewPagerIndicator;
    }

    public void setViewPagerIndicator(ViewPagerIndicator indicator) {
        if(indicator==mViewPagerIndicator){
            return;
        }
        removeView(mViewPagerIndicator);
        addView(indicator);
        refresh();
    }

    public int getIndicatorGravity() {
        return mIndicatorGravity;
    }

    public void setIndicatorGravity(int indicatorGravity) {
        mIndicatorGravity = indicatorGravity;
        LayoutParams params = (LayoutParams) mViewPagerIndicator.getLayoutParams();
        if(params.gravity==indicatorGravity){
            return;
        }else {
            params.gravity=indicatorGravity  ;
            mViewPagerIndicator.setLayoutParams(params);
        }

    }

    private static class ViewPagerIndicator extends LinearLayout {

        private Context mContext;
        private int mIndicatorCount;
        private int mSelectedColor;
        private int mUnSelectedColor;
        private int mRadiusSize;
        private int mIntervalSize;
        private int mCurrentSelectedIndex;

        public ViewPagerIndicator(Context context) {
            super(context);
            mContext = context;
            setGravity(Gravity.CENTER);
            setDefault();
        }

        private void setDefault() {
            mSelectedColor = Color.argb(196, 237, 56, 81);
            mUnSelectedColor = Color.argb(196, 168, 168, 168);
            mRadiusSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    4, mContext.getResources().getDisplayMetrics());
            mIntervalSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    4, mContext.getResources().getDisplayMetrics());
            mCurrentSelectedIndex = 0;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int newWidthMeasureSpec = widthMeasureSpec;
            int newHeightMeasureSpec = heightMeasureSpec;
            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mIndicatorCount * mRadiusSize * 2 + mIntervalSize * (mIndicatorCount - 1) + 2, MeasureSpec.EXACTLY);
            }
            if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
                newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mRadiusSize * 2 + 2, MeasureSpec.EXACTLY);
            }
            super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        }

        private void initView() {
            removeAllViews();
            for (int i = 0; i < mIndicatorCount; i++) {
                IndicatorView indicatorView = new IndicatorView(mContext);
                LayoutParams layoutParams = new LayoutParams(mRadiusSize, mRadiusSize);
                if (i > 0) {
                    layoutParams.leftMargin = mIntervalSize;
                }
                layoutParams.width = mRadiusSize * 2;
                layoutParams.height = mRadiusSize * 2;
                indicatorView.setLayoutParams(layoutParams);
                indicatorView.setIndicatorColor(mUnSelectedColor);
                addView(indicatorView);
            }
        }

        public void setSelectedIndex(int index) {
            IndicatorView currentSelectedView = (IndicatorView) getChildAt(mCurrentSelectedIndex);
            IndicatorView nextSelectedView = (IndicatorView) getChildAt(index);
            currentSelectedView.setIndicatorColor(mUnSelectedColor);
            nextSelectedView.setIndicatorColor(mSelectedColor);
            mCurrentSelectedIndex = index;
        }

        public void setIndicatorCount(int count, int defaultSelectIndex) {
            if (mIndicatorCount == count) {
                return;
            }
            mIndicatorCount = count;
            initView();
            setSelectedIndex(defaultSelectIndex);
        }


        public void setIndicatorColor(@ColorInt int selectedColor, @ColorInt int unselectedColor) {
            mSelectedColor = selectedColor;
            mUnSelectedColor = unselectedColor;
            invalidate();
        }

        public void setRadiusSize(int radiusSize) {
            mRadiusSize = radiusSize;
        }

        public void setIntervalSize(int intervalSize) {
            mIntervalSize = intervalSize;
        }

    }

    private static class IndicatorView extends View {

        private Paint mPaint;

        public IndicatorView(Context context) {
            super(context);
            this.initView();
        }

        private void initView() {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);
        }

        public void setIndicatorColor(@ColorInt int color) {
            mPaint.setColor(color);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            int minSize = Math.min(width, height);
            canvas.drawCircle(width / 2, height / 2, minSize / 2, mPaint);
        }
    }

}
