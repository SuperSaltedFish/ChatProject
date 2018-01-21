package com.yzx.chat.view.fragment;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.ContactInfoContract;
import com.yzx.chat.presenter.ContactInfoPresenter;
import com.yzx.chat.util.LogUtil;


/**
 * Created by YZX on 2018年01月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactInfoFragment extends BaseFragment<ContactInfoContract.Presenter> implements ContactInfoContract.View {

    private static final String ARGUMENT_CONTACT = "Contact";

    public static ContactInfoFragment newInstance(ContactBean contactBean) {

        Bundle args = new Bundle();

        ContactInfoFragment fragment = new ContactInfoFragment();
        args.putParcelable(ARGUMENT_CONTACT, contactBean);
        fragment.setArguments(args);
        return fragment;
    }

    private ContactBean mContactBean;
    private ConstraintLayout mClClearMessage;
    private Switch mSwitchTop;
    private Switch mSwitchRemind;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_info;
    }

    @Override
    protected void init(View parentView) {
        mSwitchTop = parentView.findViewById(R.id.FriendProfileActivity_mSwitchTop);
        mSwitchRemind = parentView.findViewById(R.id.FriendProfileActivity_mSwitchRemind);
        mClClearMessage = parentView.findViewById(R.id.FriendProfileActivity_mClClearMessage);
        mContactBean = getArguments().getParcelable(ARGUMENT_CONTACT);
    }

    @Override
    protected void setup() {
        mPresenter.init(mContactBean.getUserID());
        mSwitchTop.setOnCheckedChangeListener(mOnTopSwitchChangeListener);
        mSwitchRemind.setOnCheckedChangeListener(mOnRemindSwitchChangeListener);
        mClClearMessage.setOnClickListener(mOnClearMessageClickListener);
    }

    private final CompoundButton.OnCheckedChangeListener mOnTopSwitchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mPresenter.setConversationToTop(isChecked);
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnRemindSwitchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mPresenter.enableConversationNotification(!isChecked);
        }
    };

    private final View.OnClickListener mOnClearMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            mPresenter.clearChatMessages();
            new MaterialDialog.Builder(mContext)

                    .content("是否删除所有聊天记录？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .show();
        }
    };

    @Override
    protected void onFirstVisible() {
        super.onFirstVisible();

    }

    @Override
    public ContactInfoContract.Presenter getPresenter() {
        return new ContactInfoPresenter();
    }

    @Override
    public void switchTopState(boolean isOpen) {
        mSwitchTop.setOnCheckedChangeListener(null);
        mSwitchTop.setChecked(isOpen);
        mSwitchTop.setOnCheckedChangeListener(mOnTopSwitchChangeListener);
    }

    @Override
    public void switchRemindState(boolean isOpen) {
        mSwitchRemind.setOnCheckedChangeListener(null);
        mSwitchRemind.setChecked(isOpen);
        mSwitchRemind.setOnCheckedChangeListener(mOnRemindSwitchChangeListener);
    }
}
