package com.yzx.chat.module.me.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.me.contract.ProfileContract;
import com.yzx.chat.module.me.presenter.ProfilePresenter;
import com.yzx.chat.module.login.view.LoginActivity;
import com.yzx.chat.core.SharePreferenceManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.adapter.AlbumPagerAdapter;
import com.yzx.chat.widget.adapter.CenterCropImagePagerAdapter;
import com.yzx.chat.widget.animation.ZoomPageTransformer;
import com.yzx.chat.widget.view.MaskImageView;
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
    private ImageView mIvSetting;
    private TextView mTvLocationAndAge;
    private CenterCropImagePagerAdapter mCropImagePagerAdapter;
    private ArrayList<Object> mPicUrlList;

    private ViewPager mVpAlbum;
    private AlbumPagerAdapter mAlbumAdapter;
    private ArrayList<Object> mObjects;

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
        mIvSetting = parentView.findViewById(R.id.ProfileFragment_mIvSetting);
        mVpAlbum = parentView.findViewById(R.id.ProfileFragment_mVpAlbum);
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

        mCropImagePagerAdapter.setMaskType(MaskImageView.MASK_MODE_LINEAR);
        mVpBanner.setAdapter(mCropImagePagerAdapter);

        mIvAvatar.setOnClickListener(mOnViewClickListener);
        mIvSetting.setOnClickListener(mOnViewClickListener);

        mPresenter.initUserInfo();
    }

    private void fillTestData() {
        mPicUrlList.add(R.drawable.temp_image_1);
        mPicUrlList.add(R.drawable.temp_image_2);
        mPicUrlList.add(R.drawable.temp_image_3);

        mObjects = new ArrayList<>();
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_image_2);
        mObjects.add(R.drawable.temp_image_3);
        mObjects.add(R.drawable.temp_image_1);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mAlbumAdapter = new AlbumPagerAdapter(mObjects);

        mVpAlbum.setAdapter(mAlbumAdapter);
        //     mVpAlbum.setPageMargin((int) AndroidUtil.dip2px(32));
        mVpAlbum.setPageTransformer(true, new ZoomPageTransformer());
        mVpAlbum.setOffscreenPageLimit(2);
        mVpAlbum.setCurrentItem(1);
    }

    private final View.OnClickListener mOnViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ProfileFragment_mIvAvatar:
                    startActivity(new Intent(mContext, ProfileEditActivity.class));
                    break;
                case R.id.ProfileFragment_mIvSetting:
                    SharePreferenceManager.getConfigurePreferences().putFirstGuide(true);
                    startActivity(new Intent(mContext, LoginActivity.class));
                    getActivity().finish();
                    break;
            }
        }
    };


    @Override
    public ProfileContract.Presenter getPresenter() {
        return new ProfilePresenter();
    }


    @Override
    public void showUserInfo(UserEntity user) {
        mTvNickname.setText(user.getNickname());
        mIvSexIcon.setSelected(user.getSex() == UserEntity.SEX_WOMAN);
        StringBuilder locationAndAge = new StringBuilder();
        locationAndAge.append(user.getAge());
        if (!TextUtils.isEmpty(user.getLocation())) {
            locationAndAge.append(" · ").append(user.getLocation());
        }
        mTvLocationAndAge.setText(locationAndAge.toString());
        GlideUtil.loadAvatarFromUrl(mContext, mIvAvatar, user.getAvatar());
    }
}
