package com.yzx.chat.view.fragment;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.yzx.chat.R;
//import com.yzx.chat.adapter.ProfileNavigatorAdapter;
import com.yzx.chat.base.BaseFragment;

//import net.lucode.hackware.magicindicator.MagicIndicator;

import java.util.List;

/**
 * Created by YZX on 2017年09月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ProfileFragment extends BaseFragment {

//    private MagicIndicator mPagerIndicator;
    private ViewPager mVpProfile;
    private List<String> mTabTitleList;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_profile;
    }

    @Override
    protected void init(View parentView) {
//        mVpProfile = (ViewPager) parentView.findViewById(R.id.ProfileFragment_mVpProfile);
//        mPagerIndicator = (MagicIndicator) parentView.findViewById(R.id.ProfileFragment_mPagerIndicator);
//        mTabTitleList = Arrays.asList(mContext.getResources().getStringArray(R.array.ProfilePagerTitle));
    }

    @Override
    protected void setView() {
//        mPagerIndicator.setBackgroundColor(Color.WHITE);
//        CommonNavigator navigator = new CommonNavigator(mContext);
//        ProfileNavigatorAdapter adapter = new ProfileNavigatorAdapter(mTabTitleList);
//        adapter.setOnItemClickListener(mOnItemClickListener);
//        navigator.setAdapter(adapter);
//        navigator.setAdjustMode(true);
//        mPagerIndicator.setNavigator(navigator);
//        ViewPagerHelper.bind(mPagerIndicator, mVpProfile);
//
//        mVpProfile.setAdapter(new ProfileViewPagerAdapter(getFragmentManager()));
    }


//    private final ProfileNavigatorAdapter.OnItemClickListener mOnItemClickListener = new ProfileNavigatorAdapter.OnItemClickListener() {
//        @Override
//        public void onItemClick(View v, int position) {
//             mVpProfile.setCurrentItem(position);
//        }
//    };
}
