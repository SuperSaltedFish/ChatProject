package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.contract.HomeContract;
import com.yzx.chat.presenter.HomePresenter;
import com.yzx.chat.view.fragment.ConversationFragment;
import com.yzx.chat.view.fragment.ContactFragment;
import com.yzx.chat.view.fragment.MomentsFragment;
import com.yzx.chat.view.fragment.ProfileFragment;

public class HomeActivity extends BaseCompatActivity<HomeContract.Presenter> implements HomeContract.View {

    private final static int REQUEST_PERMISSIONS_CAMERA = 0x1;

    private BottomNavigationBar mBottomNavigationBar;
    private FragmentManager mFragmentManager;
    private Fragment[] mFragments;
    private TextBadgeItem mChatBadge;
    private TextBadgeItem mContactBadge;

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
        mBottomNavigationBar = (BottomNavigationBar) findViewById(R.id.HomeActivity_mBottomNavigationBar);
        mFragments = new Fragment[4];
        mFragments[0] = new ConversationFragment();
        mFragments[1] = new ContactFragment();
        mFragments[2] = new MomentsFragment();
        mFragments[3] = new ProfileFragment();
        mFragmentManager = getSupportFragmentManager();
    }

    private void setView() {
        mChatBadge = new TextBadgeItem()
                .setBackgroundColorResource(R.color.red)
                .setTextColorResource(android.R.color.white);

        mContactBadge = new TextBadgeItem()
                .setBackgroundColorResource(R.color.red)
                .setTextColorResource(android.R.color.white);

        mBottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_chat, R.string.HomeBottomNavigationTitle_Chat).setBadgeItem(mChatBadge))
                .addItem(new BottomNavigationItem(R.drawable.ic_friend, R.string.HomeBottomNavigationTitle_Contact).setBadgeItem(mContactBadge))
                .addItem(new BottomNavigationItem(R.drawable.ic_moments, R.string.HomeBottomNavigationTitle_Moments))
                .addItem(new BottomNavigationItem(R.drawable.ic_setting, R.string.HomeBottomNavigationTitle_Profile))
                .initialise();
        mBottomNavigationBar.setTabSelectedListener(mOnTabSelectedListener);

        mFragmentManager.beginTransaction()
                .add(R.id.HomeActivity_mClContent, mFragments[0], String.valueOf(0))
                .add(R.id.HomeActivity_mClContent, mFragments[1], String.valueOf(1))
                .hide(mFragments[1])
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.avtivity_slide_in_left, R.anim.activity_slide_out_right);
    }

    @Override
    public void onBackPressed() {
        LocalBroadcastManager.getInstance(this).sendBroadcastSync(new Intent(ChatActivity.ACTION_EXIT));
        finish();
    }

    @Override
    public HomeContract.Presenter getPresenter() {
        return new HomePresenter();
    }

    @Override
    public void updateMessageUnreadBadge(int count) {
        if (count == 0) {
            mChatBadge.hide(false);
        } else {
            if (count >= 10) {
                mChatBadge.setText(String.valueOf(count));
            } else {
                mChatBadge.setText(" " + count + " ");
            }
            mChatBadge.show(false);
        }
    }

    @Override
    public void updateContactUnreadBadge(int count) {
        if (count == 0) {
            mContactBadge.hide(false);
        } else {
            if (count >= 10) {
                mContactBadge.setText(String.valueOf(count));
            } else {
                mContactBadge.setText(" " + count + " ");
            }
            mContactBadge.show(false);
        }
    }

    private final BottomNavigationBar.OnTabSelectedListener mOnTabSelectedListener = new BottomNavigationBar.SimpleOnTabSelectedListener() {
        @Override
        public void onTabSelected(int position) {
            Fragment fragment = mFragments[position];
            if (fragment.isVisible()) {
                return;
            }
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (mFragmentManager.findFragmentByTag(String.valueOf(position)) == null) {
                transaction.add(R.id.HomeActivity_mClContent, fragment, String.valueOf(position));
            }
            for (Fragment temp : mFragments) {
                if (temp != fragment) {
                    transaction.hide(temp);
                } else {
                    transaction.show(temp);
                }
            }
            transaction.commit();
        }
    };
}
