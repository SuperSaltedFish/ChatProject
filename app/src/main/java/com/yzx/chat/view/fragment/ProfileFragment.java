package com.yzx.chat.view.fragment;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.yzx.chat.R;
//import com.yzx.chat.adapter.ProfileNavigatorAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.AlbumPagerAdapter;
import com.yzx.chat.widget.view.CarouselView;

//import net.lucode.hackware.magicindicator.MagicIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年09月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ProfileFragment extends BaseFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private ViewPager mVpAlbum;
    private AlbumPagerAdapter mAlbumAdapter;
    private List<Object> mObjects;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_profile;
    }

    @Override
    protected void init(View parentView) {
        mVpAlbum = parentView.findViewById(R.id.ProfileFragment_mVpAlbum);
        mObjects = new ArrayList<>();
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mAlbumAdapter = new AlbumPagerAdapter(mObjects);
    }

    @Override
    protected void setup() {
        mVpAlbum.setAdapter(mAlbumAdapter);
        mVpAlbum.setPageMargin((int) AndroidUtil.dip2px(32));
    }

}
