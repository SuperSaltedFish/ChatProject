package com.yzx.chat.widget.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.view.fragment.ContactInfoFragment;
import com.yzx.chat.view.fragment.ContactMomentsFragment;

/**
 * Created by YZX on 2018年01月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactProfilePagerAdapter extends FragmentPagerAdapter {

    private String[] mTitle;
    private ContactBean mContactBean;

    public ContactProfilePagerAdapter(FragmentManager fm, String title[], ContactBean contactBean) {
        super(fm);
        mTitle = title;
        mContactBean = contactBean;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ContactInfoFragment.newInstance(mContactBean);
            case 1:
                return new ContactMomentsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTitle == null ? 0 : mTitle.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitle[position];
    }
}
