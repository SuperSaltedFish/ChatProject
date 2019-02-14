package com.yzx.chat.widget.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class PageIndicator extends LinearLayout {

    private Context mContext;

    private int mIndicatorRadius;
    private int mIndicatorCount;
    private int mIndicatorColorSelected;
    private int mIndicatorColorUnselected;
    private int mSelectedPosition;


    private ArrayList<View> mIndicatorViewList;
    private ViewPager mViewPager;

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mIndicatorViewList = new ArrayList<>(6);

        initDefault();
        setOrientation(HORIZONTAL);
        setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        setShowDividers(SHOW_DIVIDER_MIDDLE);
    }

    private void initDefault() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        setIndicatorColorSelected(Color.WHITE);
        setIndicatorColorUnselected(Color.argb(128, 255, 255, 255));
        setIndicatorRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics));
        setIndicatorSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics));
    }

    public void setupWithViewPager(ViewPager pager) {
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
            mViewPager.removeOnAdapterChangeListener(mOnAdapterChangeListener);
            PagerAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                adapter.unregisterDataSetObserver(mPageDataSetObserver);
            }
        }
        mViewPager = pager;
        if (mViewPager != null) {
            mSelectedPosition = mViewPager.getCurrentItem();
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
            mViewPager.addOnAdapterChangeListener(mOnAdapterChangeListener);
            mOnAdapterChangeListener.onAdapterChanged(mViewPager, null, mViewPager.getAdapter());
        } else {
            removeAllViews();
            mIndicatorViewList.clear();
        }
    }

    public void updatePosition() {
        if (mIndicatorCount > 0 && mSelectedPosition >= mIndicatorCount) {
            throw new IndexOutOfBoundsException("SelectedPosition > IndicatorCount");
        }
        removeAllViews();
        for (int i = 0, count = mIndicatorViewList.size(); i < count; i++) {
            if (i == mSelectedPosition) {
                addView(mIndicatorViewList.get(0));
            } else if (i < mSelectedPosition) {
                addView(mIndicatorViewList.get(i + 1));
            } else {
                addView(mIndicatorViewList.get(i));
            }
        }
    }

    private void updateIndicatorView() {
        mIndicatorViewList.clear();
        for (int i = 0; i < mIndicatorCount; i++) {
            mIndicatorViewList.add(createIndicatorView(i == 0));
        }
        updatePosition();
    }

    public void setIndicatorRadius(int indicatorRadius) {
        mIndicatorRadius = indicatorRadius;
        updateIndicatorView();
    }

    public void setIndicatorCount(int indicatorCount) {
        mIndicatorCount = indicatorCount;
        updateIndicatorView();
    }

    public void setIndicatorSpace(int indicatorSpace) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize(indicatorSpace,0);
        setDividerDrawable(drawable);

    }

    public void setIndicatorColorSelected(int indicatorColorSelected) {
        mIndicatorColorSelected = indicatorColorSelected;
        updateIndicatorView();
    }

    public void setIndicatorColorUnselected(int indicatorColorUnselected) {
        mIndicatorColorUnselected = indicatorColorUnselected;
        updateIndicatorView();
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
        updatePosition();
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    private IndicatorView createIndicatorView(boolean isFocusIndicator) {
        IndicatorView indicatorView = new IndicatorView(mContext);
        int size = mIndicatorRadius * 2;
        if (isFocusIndicator) {
            indicatorView.setLayoutParams(new LayoutParams(size * 3, size));
            indicatorView.setBackgroundColor(mIndicatorColorSelected);
        } else {
            indicatorView.setLayoutParams(new LayoutParams(size, size));
            indicatorView.setBackgroundColor(mIndicatorColorUnselected);
        }
        return indicatorView;
    }

    private final ViewPager.OnAdapterChangeListener mOnAdapterChangeListener = new ViewPager.OnAdapterChangeListener() {

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (oldAdapter != null) {
                oldAdapter.unregisterDataSetObserver(mPageDataSetObserver);
            }
            if (newAdapter != null) {
                newAdapter.registerDataSetObserver(mPageDataSetObserver);
                mIndicatorCount = newAdapter.getCount();
                updateIndicatorView();
            }
        }
    };

    private final DataSetObserver mPageDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            if (mViewPager != null) {
                PagerAdapter adapter = mViewPager.getAdapter();
                setIndicatorCount(adapter == null ? 0 : adapter.getCount());
            }
        }

        @Override
        public void onInvalidated() {
            if (mViewPager != null) {
                PagerAdapter adapter = mViewPager.getAdapter();
                setIndicatorCount(adapter == null ? 0 : adapter.getCount());
            }
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            setSelectedPosition(position);
        }
    };

    private static class IndicatorView extends View {

        public IndicatorView(Context context) {
            super(context);
            this.setClipToOutline(true);
            this.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int width = getWidth();
                    int height = getHeight();
                    int min = Math.min(width, height);
                    int left = (width - min) / 2;
                    int top = (height - min) / 2;
                    outline.setRoundRect(0, 0, width, height, min / 2f);
                }
            });
        }

    }
}
