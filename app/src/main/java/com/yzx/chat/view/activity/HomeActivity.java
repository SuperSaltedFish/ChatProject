package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.view.fragment.ConversationFragment;
import com.yzx.chat.view.fragment.FriendsFragment;
import com.yzx.chat.view.fragment.MomentsFragment;
import com.yzx.chat.view.fragment.ProfileFragment;


public class HomeActivity extends BaseCompatActivity {

    private final static int REQUEST_PERMISSIONS_CAMERA = 0x1;

    private BottomNavigationView mBottomNavigationView;
    private FragmentManager mFragmentManager;
    private SparseArray<Fragment> mFragmentMap;

    private boolean isPause;

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
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.HomeActivity_mBottomNavigationView);
        mFragmentMap = new SparseArray<>();
        mFragmentManager = getSupportFragmentManager();
    }

    private void setView() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toggleFragment(R.id.HomeMenu_Chat);
    }

    @Override
    public void onRequestPermissionsSuccess(int requestCode) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CAMERA:
                startActivity(new Intent(this, QrCodeScanActivity.class));
                break;
        }
    }

    private void toggleFragment(@IdRes int menuId) {
        Fragment fragment = mFragmentMap.get(menuId);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (fragment == null) {
            switch (menuId) {
                case R.id.HomeMenu_Chat:
                    fragment = new ConversationFragment();
                    break;
                case R.id.HomeMenu_Friend:
                    fragment = new FriendsFragment();
                    break;
                case R.id.HomeMenu_Dynamic:
                    fragment = new MomentsFragment();
                    break;
                case R.id.HomeMenu_Profile:
                    fragment = new ProfileFragment();
                    break;
                default:
                    return;
            }
            mFragmentMap.put(menuId, fragment);
            transaction.add(R.id.HomeActivity_mClContent, fragment);
        }
        if (!fragment.isVisible()) {
            Fragment temp;
            for (int i = 0, size = mFragmentMap.size(); i < size; i++) {
                temp = mFragmentMap.valueAt(i);
                if (temp != fragment) {
                    transaction.hide(temp);
                } else {
                    transaction.show(temp);
                }
            }
            transaction.commit();
        }
    }


    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            toggleFragment(item.getItemId());
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }
}
