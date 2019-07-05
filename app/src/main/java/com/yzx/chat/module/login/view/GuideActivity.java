package com.yzx.chat.module.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.adapter.GuidePagerAdapter;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.view.PageIndicator;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

public class GuideActivity extends BaseCompatActivity {

    private ViewPager mGuideViewPager;
    private PageIndicator mPageIndicator;
    private TextSwitcher mTextSwitcher;
    private LinearLayout mLlTextIndicator;

    private GuidePagerAdapter mGuidePagerAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_guide;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mGuideViewPager = findViewById(R.id.mGuideViewPager);
        mPageIndicator = findViewById(R.id.mPageIndicator);
        mTextSwitcher = findViewById(R.id.mTextSwitcher);
        mLlTextIndicator = findViewById(R.id.mLlTextIndicator);
        mGuidePagerAdapter = new GuidePagerAdapter(this);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_LIGHT_BAR_STATUS);

        mGuideViewPager.setAdapter(mGuidePagerAdapter);
        mGuideViewPager.addOnPageChangeListener(mOnPageChangeListener);

        mPageIndicator.setIndicatorColorSelected(ContextCompat.getColor(this, R.color.colorAccent));
        mPageIndicator.setIndicatorColorUnselected(ContextCompat.getColor(this, R.color.colorAccentLight));
        mPageIndicator.setupWithViewPager(mGuideViewPager);

        mLlTextIndicator.setOnClickListener(mOnNextClickListener);
    }

    private void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private final View.OnClickListener mOnNextClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            int currentIndex = mGuideViewPager.getCurrentItem();
            if (currentIndex < mGuidePagerAdapter.getCount() - 1) {
                mGuideViewPager.setCurrentItem(currentIndex + 1);
            } else {
                startLoginActivity();
            }
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

        private boolean isSlideToEnd;
        private boolean isAlreadyStart;
        private int mLastPosition;

        @Override
        public void onPageSelected(int position) {
            int count = mGuidePagerAdapter.getCount();
            if (position == count - 1 && mLastPosition < count - 1) {
                mTextSwitcher.showNext();
            } else if (mLastPosition == count - 1 && position < count - 1) {
                mTextSwitcher.showPrevious();
            }
            mLastPosition = position;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int count = mGuidePagerAdapter.getCount();
            if (!isAlreadyStart && isSlideToEnd && position == count - 1) {
                startLoginActivity();
                isAlreadyStart = true;
            } else isSlideToEnd = (position == count - 1);
        }
    };

}
