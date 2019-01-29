package com.yzx.chat.module.contact.view;

import android.os.Bundle;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ContactMotionFragment extends BaseFragment {

    private static final String ARGUMENT_CONTENT_ID = "ContentID";

    public static ContactMotionFragment newInstance(String contactID) {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_CONTENT_ID,contactID);
        ContactMotionFragment fragment = new ContactMotionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_motion;
    }

    @Override
    protected void init(View parentView) {

    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }
}
