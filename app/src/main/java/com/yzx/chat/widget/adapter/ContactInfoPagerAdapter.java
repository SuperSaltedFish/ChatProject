package com.yzx.chat.widget.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.yzx.chat.mvp.view.fragment.ContactChatSettingFragment;
import com.yzx.chat.mvp.view.fragment.ContactMomentsFragment;
import com.yzx.chat.mvp.view.fragment.ContactMotionFragment;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ContactInfoPagerAdapter extends FragmentPagerAdapter {

    private String mContentID;
    private String[] mTitles;

    public ContactInfoPagerAdapter(FragmentManager fm, String contactID, String[] titles) {
        super(fm);
        mContentID = contactID;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ContactChatSettingFragment.newInstance(mContentID);
            case 1:
                return ContactMomentsFragment.newInstance(mContentID);
            case 2:
                return ContactMotionFragment.newInstance(mContentID);
        }
        return null;
    }

    @Override
    public int getCount() {
        return mTitles == null ? 0 : mTitles.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
