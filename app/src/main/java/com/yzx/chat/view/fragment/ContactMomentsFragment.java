package com.yzx.chat.view.fragment;

import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.util.LogUtil;

/**
 * Created by YZX on 2018年01月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactMomentsFragment extends BaseFragment {
    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_moments;
    }

    @Override
    protected void init(View parentView) {

    }

    @Override
    protected void setView() {

    }

    @Override
    protected void onFirstVisible() {
        super.onFirstVisible();
        LogUtil.e("ContactMomentsFragment");
    }
}
