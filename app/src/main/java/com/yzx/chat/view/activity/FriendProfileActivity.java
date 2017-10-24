package com.yzx.chat.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.yzx.chat.R;
import com.yzx.chat.widget.adapter.ProfilePagerFragmentAdapter;
import com.yzx.chat.base.BaseCompatActivity;

public class FriendProfileActivity extends BaseCompatActivity {

    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    private ViewPager mViewPager;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_friend_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setView();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.FriendProfileActivity_mToolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.FriendProfileActivity_mAppBarLayout);
        mViewPager = (ViewPager) findViewById(R.id.FriendProfileActivity_mViewPager);
    }

    private void setView() {
        setSupportActionBar(mToolbar);
        setTitle(R.string.FriendProfileActivity_Title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mAppBarLayout.addOnOffsetChangedListener(mOnOffsetChangedListener);

        mViewPager.setAdapter(new ProfilePagerFragmentAdapter(getSupportFragmentManager()));

    }


    private final AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            double range = appBarLayout.getTotalScrollRange();
            int s = (int) ((1 - (range + verticalOffset) / range) * 255.0);
            mToolbar.setTitleTextColor(Color.argb(s, 255, 255, 255));
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.FriendProfileMenu_more:
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_priend_profile, menu);
        return true;
    }
}
