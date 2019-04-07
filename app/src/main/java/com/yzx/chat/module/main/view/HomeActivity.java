package com.yzx.chat.module.main.view;

import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.broadcast.BackPressedReceive;
import com.yzx.chat.module.common.view.QrCodeScanActivity;
import com.yzx.chat.module.contact.view.ContactListFragment;
import com.yzx.chat.module.conversation.view.ConversationFragment;
import com.yzx.chat.module.main.contract.HomeContract;
import com.yzx.chat.module.main.presenter.HomePresenter;
import com.yzx.chat.module.me.view.ProfileFragment;
import com.yzx.chat.module.moments.view.MomentsFragment;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.view.BottomTabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class HomeActivity extends BaseCompatActivity<HomeContract.Presenter> implements HomeContract.View {

    private final static int REQUEST_PERMISSIONS_CAMERA = 0x1;

    private FragmentManager mFragmentManager;
    private BottomTabLayout mBottomTabLayout;
    private int mCurrentFragmentIndex = -1;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_home;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mBottomTabLayout = findViewById(R.id.mBottomTabLayout);
        mFragmentManager = getSupportFragmentManager();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(null);
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        addFragment(transaction, new ConversationFragment(), "0");
        addFragment(transaction, new ContactListFragment(), "1");
        addFragment(transaction, new MomentsFragment(), "2");
        addFragment(transaction, new ProfileFragment(), "3");
        transaction.commitNow();

        mBottomTabLayout
                .addTab(R.drawable.ic_conversation_focus, R.drawable.ic_conversation_unfocus, getString(R.string.HomeBottomNavigationTitle_Chat))
                .addTab(R.drawable.ic_contacts_focus, R.drawable.ic_contacts_unfocus, getString(R.string.HomeBottomNavigationTitle_Contact))
                .addTab(R.drawable.ic_moments_focus, R.drawable.ic_moments_unfocus, getString(R.string.HomeBottomNavigationTitle_Moments))
                .addTab(R.drawable.ic_personal_focus, R.drawable.ic_personal_unfocus, getString(R.string.HomeBottomNavigationTitle_Profile))
                .setTitleTextSize(AndroidHelper.sp2px(11))
                .addOnTabItemSelectedListener(new BottomTabLayout.OnTabItemSelectedListener() {
                    @Override
                    public void onSelected(int position) {
                        Fragment show = mFragmentManager.findFragmentByTag(String.valueOf(position));
                        if (show == null) {
                            return;
                        }
                        FragmentTransaction transaction = mFragmentManager.beginTransaction();
                        if (mCurrentFragmentIndex >= 0) {
                            Fragment hide = mFragmentManager.findFragmentByTag(String.valueOf(mCurrentFragmentIndex));
                            if (hide != null) {
                                transaction.hide(hide);
                            }
                        }
                        if (show.isHidden()) {
                            transaction.show(show);
                        }
                        transaction.commit();
                        mCurrentFragmentIndex = position;
                    }

                    @Override
                    public void onRepeated(int position) {

                    }
                })
                .setSelectPosition(0, false, true);

        mBottomTabLayout.setOutlineProvider(new ViewOutlineProvider() {
            private Rect mRect = new Rect();

            @Override
            public void getOutline(View view, Outline outline) {
                Drawable background = view.getBackground();
                if (background != null) {
                    background.copyBounds(mRect);
                } else {
                    mRect.set(0, 0, view.getWidth(), view.getHeight());
                }
                mRect.offset(0, (int) -AndroidHelper.dip2px(3));
                outline.setRect(mRect);
            }
        });

        setData();
    }

    private void setData() {
        mPresenter.loadUnreadCount();
    }

    private void addFragment(FragmentTransaction transaction, Fragment fragment, String tag) {
        Fragment old = mFragmentManager.findFragmentByTag(tag);
        if (old == null) {
            old = fragment;
            transaction.add(R.id.mClContent, fragment, tag);
        }
        transaction.hide(old);
    }

    @Override
    protected void onRequestPermissionsResult(int requestCode, boolean isSuccess, String[] deniedPermissions) {
        if (isSuccess) {
            switch (requestCode) {
                case REQUEST_PERMISSIONS_CAMERA:
                    startActivity(new Intent(this, QrCodeScanActivity.class));
                    break;
            }
        } else {
            showToast(getString(R.string.PermissionMiss));
        }
    }


    @Override
    public void onBackPressed() {
        if (!BackPressedReceive.sendBackPressedEvent(HomeActivity.class.getSimpleName())) {
            moveTaskToBack(true);
        }
    }

    @Override
    public HomeContract.Presenter getPresenter() {
        return new HomePresenter();
    }

    @Override
    public void updateMessageUnreadBadge(int count) {
        mBottomTabLayout.setBadge(0, count);

    }

    @Override
    public void updateContactUnreadBadge(int count) {
        mBottomTabLayout.setBadge(1, count);
    }
}