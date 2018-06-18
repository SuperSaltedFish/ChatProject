package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.view.BottomTabLayout;

import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageNavigationView;


public class TestActivity extends BaseCompatActivity {

    private NavigationController mNavigationController;
    private ViewPager mViewPager;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mViewPager = findViewById(R.id.asd);
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view==object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                TextView textView = new TextView(container.getContext());
                textView.setText("position:"+position);
                textView.setGravity(Gravity.CENTER);
                container.addView(textView);
                return textView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        });

        BottomTabLayout bottomTabLayout = findViewById(R.id.ssss);
        bottomTabLayout.addTab(R.drawable.ic_conversation_focus,R.drawable.ic_conversation_unfocus,"朋友圈");
        bottomTabLayout.addTab(R.drawable.ic_friend,"朋友圈",ContextCompat.getColor(this,R.color.colorAccent));
        bottomTabLayout.addTab(R.drawable.ic_moments,"朋友圈",ContextCompat.getColor(this,R.color.colorAccent));
        bottomTabLayout.addTab(R.drawable.ic_setting,"朋友圈",ContextCompat.getColor(this,R.color.colorAccent));

        mNavigationController = ((PageNavigationView) findViewById(R.id.aaaa)).material()
                .addItem(R.drawable.ic_conversation, getString(R.string.HomeBottomNavigationTitle_Chat), ContextCompat.getColor(this, R.color.colorAccent))
                .addItem(R.drawable.ic_friend, getString(R.string.HomeBottomNavigationTitle_Contact), ContextCompat.getColor(this, R.color.colorAccent))
                .addItem(R.drawable.ic_moments, getString(R.string.HomeBottomNavigationTitle_Moments), ContextCompat.getColor(this, R.color.colorAccent))
                .addItem(R.drawable.ic_setting, getString(R.string.HomeBottomNavigationTitle_Profile), ContextCompat.getColor(this, R.color.colorAccent))
                .build();

        bottomTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);

    }

    private void setData() {

    }



}


