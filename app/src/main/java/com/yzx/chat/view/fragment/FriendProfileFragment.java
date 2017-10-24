package com.yzx.chat.view.fragment;

import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.widget.view.NineGridImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年09月24日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class FriendProfileFragment extends BaseFragment {

    private NineGridImageView mNineGridImageView;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_friend_profile;
    }

    @Override
    protected void init(View parentView) {
        mNineGridImageView = (NineGridImageView) parentView.findViewById(R.id.FriendProfileActivity_mNineGridImageView);
    }

    @Override
    protected void setView() {
    }

    @Override
    public void onFirstVisible() {
        loadData();
    }

    private void loadData() {
        List<String> s = new ArrayList<>();
        s.add("");
        s.add("");
        s.add("");
        mNineGridImageView.setImageData(s);
    }
}
