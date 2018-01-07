package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.contract.HomeContract;
import com.yzx.chat.presenter.HomePresenter;
import com.yzx.chat.view.fragment.ConversationFragment;
import com.yzx.chat.view.fragment.ContactFragment;
import com.yzx.chat.view.fragment.MomentsFragment;
import com.yzx.chat.view.fragment.ProfileFragment;

import me.majiajie.pagerbottomtabstrip.MaterialMode;
import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageNavigationView;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

public class HomeActivity extends BaseCompatActivity<HomeContract.Presenter> implements HomeContract.View {

    private final static int REQUEST_PERMISSIONS_CAMERA = 0x1;

    private FragmentManager mFragmentManager;
    private Fragment[] mFragments;
    private NavigationController mNavigationController;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setView();
        setData();
    }

    private void initView() {
        mFragments = new Fragment[4];
        mFragments[0] = new ConversationFragment();
        mFragments[1] = new ContactFragment();
        mFragments[2] = new MomentsFragment();
        mFragments[3] = new ProfileFragment();
        mFragmentManager = getSupportFragmentManager();

        mNavigationController = ((PageNavigationView) findViewById(R.id.HomeActivity_mBottomNavigationBar)).material()
                .addItem(R.drawable.ic_conversation, getString(R.string.HomeBottomNavigationTitle_Chat), ContextCompat.getColor(this,R.color.colorAccent))
                .addItem(R.drawable.ic_friend, getString(R.string.HomeBottomNavigationTitle_Contact), ContextCompat.getColor(this,R.color.colorAccent))
                .addItem(R.drawable.ic_moments, getString(R.string.HomeBottomNavigationTitle_Moments), ContextCompat.getColor(this,R.color.colorAccent))
                .addItem(R.drawable.ic_setting, getString(R.string.HomeBottomNavigationTitle_Profile), ContextCompat.getColor(this,R.color.colorAccent))
                .setMode(MaterialMode.HIDE_TEXT)
                .build();
    }

    private void setView() {
        mNavigationController.addTabItemSelectedListener(mOnTabSelectedListener);

        mFragmentManager.beginTransaction()
                .add(R.id.HomeActivity_mClContent, mFragments[0], String.valueOf(0))
                .add(R.id.HomeActivity_mClContent, mFragments[1], String.valueOf(1))
                .add(R.id.HomeActivity_mClContent, mFragments[2], String.valueOf(2))
                .hide(mFragments[1])
                .hide(mFragments[2])
                .commit();


    }

    private void setData() {
        mPresenter.loadUnreadCount();
    }

    @Override
    public void onRequestPermissionsSuccess(int requestCode) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CAMERA:
                startActivity(new Intent(this, QrCodeScanActivity.class));
                break;
        }
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public HomeContract.Presenter getPresenter() {
        return new HomePresenter();
    }

    @Override
    public void updateMessageUnreadBadge(int count) {
        mNavigationController.setMessageNumber(0, count);

    }

    @Override
    public void updateContactUnreadBadge(int count) {
        mNavigationController.setMessageNumber(1, count);
    }

    private final OnTabItemSelectedListener mOnTabSelectedListener = new OnTabItemSelectedListener() {
        @Override
        public void onSelected(int index, int old) {
            mFragmentManager.beginTransaction()
                    .show(mFragments[index])
                    .hide(mFragments[old])
                    .commit();
        }

        @Override
        public void onRepeat(int index) {

        }
    };
}