package com.yzx.chat.module.main.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.module.main.contract.SplashContract;
import com.yzx.chat.module.main.presenter.SplashPresenter;
import com.yzx.chat.module.login.view.LoginActivity;
import com.yzx.chat.core.SharePreferenceManager;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.PageIndicator;


public class SplashActivity extends BaseCompatActivity<SplashContract.Presenter> implements SplashContract.View {

    private static int PERMISSIONS_REQUEST_CODE = 1;

    private static final int GUIDE_COUNT = 3;

    private int[] mSplashImage = {R.drawable.src_splash_1, R.drawable.src_splash_2, R.drawable.src_splash_3};
    private String[] mSplashTitle;
    private String mSplashContent;

    @Override
    protected int getLayoutID() {
        return 0;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        requestPermissionsInCompatMode(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO
                },
                PERMISSIONS_REQUEST_CODE);
    }

    @Override
    protected void onRequestPermissionsResult(int requestCode, boolean isSuccess, String[] deniedPermissions) {
        if (isSuccess) {
            if (requestCode == PERMISSIONS_REQUEST_CODE) {
                mPresenter.checkLogin();
            }
        } else {
            finish();
        }
    }

    private void initGuide() {
        mSplashTitle = getResources().getStringArray(R.array.SplashActivity_Titles);
        mSplashContent = getString(R.string.SplashActivity_Content);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        setSystemUiMode(SYSTEM_UI_MODE_LIGHT_BAR);

        setContentView(R.layout.activity_splash);

        final TextSwitcher textSwitcher = findViewById(R.id.SplashActivity_mTextSwitcher);
        final ViewPager guideViewPager = findViewById(R.id.SplashActivity_mGuideViewPager);
        final PageIndicator pageIndicator = findViewById(R.id.SplashActivity_mPageIndicator);
        pageIndicator.setIndicatorColorSelected(ContextCompat.getColor(SplashActivity.this, R.color.colorAccent));
        pageIndicator.setIndicatorColorUnselected(ContextCompat.getColor(SplashActivity.this, R.color.colorAccentLight));
        pageIndicator.setupWithViewPager(guideViewPager);
        guideViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return GUIDE_COUNT;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View itemView = LayoutInflater.from(SplashActivity.this).inflate(R.layout.item_splash, container, false);
                TextView tvTitle = itemView.findViewById(R.id.SplashActivity_mTvTitle);
                TextView tvContent = itemView.findViewById(R.id.SplashActivity_mTvContent);
                ImageView ivSplashImage = itemView.findViewById(R.id.SplashActivity_mIvSplashImage);
                tvTitle.setText(mSplashTitle[position]);
                tvContent.setText(mSplashContent);
                ivSplashImage.setImageResource(mSplashImage[position]);
                container.addView(itemView);
                return itemView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                View itemView = (View) object;
                container.removeView(itemView);
                ImageView ivSplashImage = itemView.findViewById(R.id.SplashActivity_mIvSplashImage);
                ivSplashImage.setImageBitmap(null);
                GlideUtil.clear(SplashActivity.this, ivSplashImage);
            }
        });
        guideViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            private boolean isSlideToEnd;
            private int mLastPosition;

            @Override
            public void onPageSelected(int position) {
                if (position == GUIDE_COUNT - 1 && mLastPosition < GUIDE_COUNT - 1) {
                    textSwitcher.showNext();
                } else if (mLastPosition == GUIDE_COUNT - 1 && position < GUIDE_COUNT - 1) {
                    textSwitcher.showPrevious();
                }
                mLastPosition = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (isSlideToEnd && position == GUIDE_COUNT - 1) {
                    guideViewPager.removeOnPageChangeListener(this);
                    startLoginActivity();

                } else isSlideToEnd = (position == GUIDE_COUNT - 1);
            }
        });

        View textIndicator = findViewById(R.id.SplashActivity_mLlTextIndicator);
        textIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentIndex = guideViewPager.getCurrentItem();
                if (currentIndex < GUIDE_COUNT - 1) {
                    guideViewPager.setCurrentItem(currentIndex + 1);
                } else {
                    startLoginActivity();
                }
            }
        });
    }

    @Override
    public SplashContract.Presenter getPresenter() {
        return new SplashPresenter();
    }

    @Override
    public void startLoginActivity() {
        SharePreferenceManager.getConfigurePreferences().putFirstGuide(false);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void startHomeActivity() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void startGuide() {
        initGuide();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showError(String error) {
        showToast(error);
    }
}
