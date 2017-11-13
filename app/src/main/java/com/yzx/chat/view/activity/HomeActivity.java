package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.view.fragment.ConversationFragment;
import com.yzx.chat.view.fragment.ContactFragment;
import com.yzx.chat.view.fragment.MomentsFragment;
import com.yzx.chat.view.fragment.ProfileFragment;


public class HomeActivity extends BaseCompatActivity {

    private final static int REQUEST_PERMISSIONS_CAMERA = 0x1;

    private BottomNavigationBar mBottomNavigationBar;
    private FragmentManager mFragmentManager;
    private Fragment[] mFragments;
    private TextBadgeItem mChatBadge;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setView();
    }

    private void initView() {
        mBottomNavigationBar = (BottomNavigationBar) findViewById(R.id.HomeActivity_mBottomNavigationBar);
        mFragments = new Fragment[4];
        mFragmentManager = getSupportFragmentManager();
    }

    private void setView() {
        mChatBadge = new TextBadgeItem()
                .setBackgroundColorResource(R.color.red)
                .setTextColorResource(android.R.color.white);

        mBottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_chat, R.string.HomeBottomNavigationTitle_Chat).setBadgeItem(mChatBadge))
                .addItem(new BottomNavigationItem(R.drawable.ic_friend, R.string.HomeBottomNavigationTitle_Contact))
                .addItem(new BottomNavigationItem(R.drawable.ic_moments, R.string.HomeBottomNavigationTitle_Moments))
                .addItem(new BottomNavigationItem(R.drawable.ic_setting, R.string.HomeBottomNavigationTitle_Profile))
                .initialise();

        mBottomNavigationBar.setTabSelectedListener(mOnTabSelectedListener);
        mBottomNavigationBar.selectTab(0);
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
        overridePendingTransition(R.anim.avtivity_slide_in_left,R.anim.activity_slide_out_right);
    }

    public void updateUnreadMessageCount(int count){
        if(count==0){
            mChatBadge.hide(false);
        }else {
            if(count>=10){
                mChatBadge.setText(String.valueOf(10));
            }else {
                mChatBadge.setText(" "+count+" ");
            }
            mChatBadge.show(false);
        }
    }


    private final BottomNavigationBar.OnTabSelectedListener mOnTabSelectedListener = new BottomNavigationBar.SimpleOnTabSelectedListener() {
        @Override
        public void onTabSelected(int position) {
            Fragment fragment = mFragments[position];
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (fragment == null) {
                switch (position) {
                    case 0:
                        fragment = new ConversationFragment();
                        break;
                    case 1:
                        fragment = new ContactFragment();
                        break;
                    case 2:
                        fragment = new MomentsFragment();
                        break;
                    case 3:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        return;
                }
                mFragments[position] = fragment;
                transaction.add(R.id.HomeActivity_mClContent, fragment);
            }
            if (!fragment.isVisible()) {
                for (Fragment temp : mFragments) {
                    if (temp == null) {
                        continue;
                    }
                    if (temp != fragment) {
                        transaction.hide(temp);
                    } else {
                        transaction.show(temp);
                    }
                }
            }
            transaction.commit();
        }
    };

}
