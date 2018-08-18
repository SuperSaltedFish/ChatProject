package com.yzx.chat.mvp.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.ProfileContract;
import com.yzx.chat.mvp.contract.ProfileModifyContract;
import com.yzx.chat.mvp.presenter.ProfileModifyPresenter;
import com.yzx.chat.mvp.presenter.ProfilePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.adapter.CenterCropImagePagerAdapter;
import com.yzx.chat.widget.view.PageIndicator;

import java.util.ArrayList;

/**
 * Created by YZX on 2017年09月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ProfileFragment extends BaseFragment<ProfileContract.Presenter> implements ProfileContract.View {

    private ViewPager mVpBanner;
    private PageIndicator mPageIndicator;
    private ImageView mIvAvatar;
    private TextView mTvNickname;
    private ImageView mIvSexIcon;
    private TextView mTvLocationAndAge;
    private CenterCropImagePagerAdapter mCropImagePagerAdapter;
    private ArrayList<Object> mPicUrlList;


    @Override
    protected int getLayoutID() {
        return R.layout.fragment_profile;
    }

    @Override
    protected void init(View parentView) {
        mVpBanner = parentView.findViewById(R.id.ProfileFragment_mVpBanner);
        mPageIndicator = parentView.findViewById(R.id.ProfileFragment_mPageIndicator);
        mIvAvatar = parentView.findViewById(R.id.ProfileFragment_mIvAvatar);
        mTvNickname = parentView.findViewById(R.id.ProfileFragment_mTvNickname);
        mIvSexIcon = parentView.findViewById(R.id.ProfileFragment_mIvSexIcon);
        mTvLocationAndAge = parentView.findViewById(R.id.ProfileFragment_mTvLocationAndAge);
        mPicUrlList = new ArrayList<>(6);
        mCropImagePagerAdapter = new CenterCropImagePagerAdapter(mPicUrlList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        fillTestData();

        mPageIndicator.setIndicatorColorSelected(Color.WHITE);
        mPageIndicator.setIndicatorColorUnselected(ContextCompat.getColor(mContext, R.color.backgroundColorWhiteLight));
        mPageIndicator.setIndicatorRadius((int) AndroidUtil.dip2px(3));
        mPageIndicator.setupWithViewPager(mVpBanner);

        mVpBanner.setAdapter(mCropImagePagerAdapter);

        mPresenter.initUserInfo();
    }

    private void fillTestData() {
        mPicUrlList.add(R.drawable.temp_image_1);
        mPicUrlList.add(R.drawable.temp_image_2);
        mPicUrlList.add(R.drawable.temp_image_3);
    }


    @Override
    public ProfileContract.Presenter getPresenter() {
        return new ProfilePresenter();
    }


    @Override
    public void showUserInfo(UserBean user) {
        mTvNickname.setText(user.getNickname());
        mIvSexIcon.setSelected(user.getSex() == UserBean.SEX_WOMAN);
        StringBuilder locationAndAge = new StringBuilder();
        locationAndAge.append(user.getAge());
        if (!TextUtils.isEmpty(user.getLocation())) {
            locationAndAge.append(" · ").append(user.getLocation());
        }
        mTvLocationAndAge.setText(locationAndAge.toString());
        GlideUtil.loadAvatarFromUrl(mContext, mIvAvatar, user.getSignature());
    }
}
