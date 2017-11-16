package com.yzx.chat.widget.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Created by YZX on 2017年05月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class CarouselView extends FrameLayout {

    private ViewPager mViewPager;
    private ViewPagerIndicator mViewPagerIndicator;
    private Context mContext;
    private ArrayList<String> mPicUrlList;
    private LinkedList<WeakReference<ImageView>> mCacheViewQueue;
    private Handler mHandler;
    private CarouseRunnable mCarouseRunnable;
    private int mIntervalTime = 5000;


    private boolean isStartCarousel = false;

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
        init();
    }


    private void init() {
        mViewPager = new ViewPager(mContext);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        int oPagerCount = mPagerAdapter.getCount();
        mViewPager.setCurrentItem(oPagerCount / 2 - (oPagerCount / 2) % 2);

        mViewPagerIndicator = new ViewPagerIndicator(mContext);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        mViewPagerIndicator.setLayoutParams(params);

        mCacheViewQueue = new LinkedList<>();
        mCarouseRunnable = new CarouseRunnable(mViewPager);

        addView(mViewPager, 0);
        addView(mViewPagerIndicator, 1);
    }

    private void initIndicator() {
        if (mPicUrlList != null && mPicUrlList.size() > 0) {
            mViewPagerIndicator.setIndicatorCount(mPicUrlList.size());
        }
    }


    private ImageView getNewItem() {
        ImageView itemView = new ImageView(mContext);
        ViewPager.LayoutParams params = new ViewPager.LayoutParams();
        itemView.setLayoutParams(params);
        return itemView;
    }

    private final PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mPicUrlList == null ? 0 : Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView itemView = (ImageView) object;
            container.removeView(itemView);
            itemView.setImageBitmap(null);
            mCacheViewQueue.offer(new WeakReference<>(itemView));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            WeakReference<ImageView> weakReference = mCacheViewQueue.poll();
            ImageView itemView;
            if (weakReference != null && weakReference.get() != null) {
                itemView = weakReference.get();
            } else {
                itemView = getNewItem();
            }
            container.addView(itemView);
            int listSize = mPicUrlList.size();
//            GlideUtil.loadFromUrl(mContext, itemView, mPicUrlList.get(position % listSize));

            if (!isStartCarousel && listSize > 1) {
                isStartCarousel = true;
                mOnPageChangeListener.onPageSelected(0);
            }
            return itemView;
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(final int position) {
            mViewPagerIndicator.setSelectedIndex(position % mPicUrlList.size());
            mHandler.removeCallbacks(mCarouseRunnable);
            mCarouseRunnable.setPosition(position + 1);
            mHandler.postDelayed(mCarouseRunnable, mIntervalTime);
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mHandler == null) {
            mHandler = getHandler();
        } else if (isStartCarousel) {
            int position = mViewPager.getCurrentItem() + 1;
            mCarouseRunnable.setPosition(position);
            mHandler.postDelayed(mCarouseRunnable, mIntervalTime);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null) {
            mHandler.removeCallbacks(mCarouseRunnable);
        }
    }

    private static class CarouseRunnable implements Runnable {
        WeakReference<ViewPager> mViewPagerWeakReference;
        private int mPosition;

        CarouseRunnable(ViewPager pViewPager) {
            mViewPagerWeakReference = new WeakReference<>(pViewPager);
        }

        void setPosition(int position) {
            mPosition = position;
        }

        @Override
        public void run() {
            ViewPager oViewPager = mViewPagerWeakReference.get();
            if (oViewPager != null && oViewPager.isAttachedToWindow()) {
                oViewPager.setCurrentItem(mPosition, true);
            }
        }
    }

    public int getIntervalTime() {
        return mIntervalTime;
    }

    public void setIntervalTime(int pIntervalTime) {
        mIntervalTime = pIntervalTime;
    }

    public ArrayList<String> getPicUrls() {
        return mPicUrlList;
    }

    public void setPicUrls(ArrayList<String> picUrls) {
        mPicUrlList = picUrls;
        initIndicator();
        mPagerAdapter.notifyDataSetChanged();
    }

}
