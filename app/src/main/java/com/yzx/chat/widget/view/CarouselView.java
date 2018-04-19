package com.yzx.chat.widget.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.yzx.chat.util.GlideUtil;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by YZX on 2017年05月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class CarouselView extends ViewPager {

    private Context mContext;
    private List<Object> mPicUrlList;
    private LinkedList<WeakReference<? extends View>> mCacheViewQueue;
    private Handler mHandler;
    private Runnable mCarouselRunnable;
    private int mCarouselInterval = 0;

    private boolean isVisibility;


    public CarouselView(Context context) {
        this(context, null);
    }

    public CarouselView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mHandler = new Handler();
        init();
    }


    private void init() {
        mCacheViewQueue = new LinkedList<>();
        addOnPageChangeListener(mOnPageChangeListener);
        setAdapter(mPagerAdapter);
    }


    private void refresh() {
        if (mPicUrlList != null && mPicUrlList.size() > 0) {
            int picUrlCount = mPicUrlList.size();
            int pagerCount = mPagerAdapter.getCount();
            setCurrentItem(pagerCount / 2 - (pagerCount / 2) % picUrlCount);
        }
        mPagerAdapter.notifyDataSetChanged();
    }

    private void delayedShowNext() {
        if (mCarouselInterval > 0) {
            mHandler.removeCallbacksAndMessages(null);
            if (mCarouselRunnable == null) {
                mCarouselRunnable = new Runnable() {
                    @Override
                    public void run() {
                        setCurrentItem(getCurrentItem() + 1, true);
                    }
                };
            }
            mHandler.postDelayed(mCarouselRunnable, mCarouselInterval);
        }
    }

    public void onResume() {
        delayedShowNext();
    }

    public void onPause() {
        mHandler.removeCallbacksAndMessages(null);
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
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            ViewGroup rootView = (ViewGroup) object;
            ImageView itemView = (ImageView) rootView.getChildAt(0);
            container.removeView(rootView);
            GlideUtil.clear(mContext, itemView);
            itemView.setImageBitmap(null);
            mCacheViewQueue.offer(new WeakReference<>(rootView));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            isVisibility = true;
            WeakReference<? extends View> weakReference = mCacheViewQueue.poll();
            View itemView;
            if (weakReference != null && weakReference.get() != null) {
                itemView = weakReference.get();
            } else {
                itemView = new PhotoView(mContext);
            }
            container.addView(itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            int listSize = mPicUrlList.size();
            GlideUtil.loadFromUrl(mContext, (ImageView) itemView, mPicUrlList.get(position % listSize));
            return itemView;
        }
    };

    private final OnPageChangeListener mOnPageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(final int position) {
            delayedShowNext();
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

    public List<Object> getPicUrls() {
        return mPicUrlList;
    }

    public void setPicUrls(List<Object> picUrls) {
        mPicUrlList = picUrls;
        refresh();
    }

}