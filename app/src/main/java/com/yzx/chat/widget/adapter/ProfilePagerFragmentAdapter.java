package com.yzx.chat.widget.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yzx.chat.view.fragment.FriendMomentsFragment;
import com.yzx.chat.view.fragment.FriendProfileFragment;

/**
 * Created by YZX on 2017年09月02日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ProfilePagerFragmentAdapter extends FragmentPagerAdapter {

    public ProfilePagerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=null;
        switch (position){
            case 0:
                fragment = new FriendProfileFragment();
                break;
            case 1:
                fragment = new FriendMomentsFragment();
                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
