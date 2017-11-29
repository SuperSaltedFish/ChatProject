package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Color;
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
    private int mCarouselInterval = 0;


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
    }


    private void init() {
        mViewPager = new ViewPager(mContext);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        mViewPagerIndicator = new ViewPagerIndicator(mContext);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        mViewPagerIndicator.setLayoutParams(params);
        mViewPagerIndicator.setIndicatorColor(Color.BLACK, Color.RED);

        mCacheViewQueue = new LinkedList<>();

        addView(mViewPagerIndicator);
        addView(mViewPager);

    }

    private void initIndicator() {
        if (mPicUrlList != null && mPicUrlList.size() > 0) {
            mViewPagerIndicator.setIndicatorCount(mPicUrlList.size());
        }
    }

    private void refresh() {
        int oPagerCount = mPagerAdapter.getCount();
        mViewPager.setCurrentItem(oPagerCount / 2 - (oPagerCount / 2) % 2);
    }

    private void delayedShowNext(final int nextPosition) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(nextPosition + 1, true);
            }
        }, mCarouselInterval);
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
            GlideUtil.clear(mContext, itemView);
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
                itemView = new ImageView(mContext);
            }
            container.addView(itemView);

            int listSize = mPicUrlList.size();
            GlideUtil.loadFromUrl(mContext, itemView, mPicUrlList.get(position % listSize));


            return itemView;
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(final int position) {
            if (mCarouselInterval == 0) {
                return;
            }
            mViewPagerIndicator.setSelectedIndex(position % mPicUrlList.size());
            delayedShowNext(position + 1);
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
        if(changedView==this) {
            if (visibility == VISIBLE) {
                delayedShowNext(mViewPager.getCurrentItem() + 1);
            } else {
                mHandler.removeCallbacksAndMessages(null);
            }
        }

    }

    public int getCarouselInterval() {
        return mCarouselInterval;
    }

    public void setCarouselInterval(int carouselInterval) {
        mCarouselInterval = carouselInterval;
    }

    public List<String> getPicUrls() {
        return mPicUrlList;
    }

    public void setPicUrls(List<String> picUrls) {
        mPicUrlList = picUrls;
        refresh();
        initIndicator();
        mPagerAdapter.notifyDataSetChanged();
    }

}
