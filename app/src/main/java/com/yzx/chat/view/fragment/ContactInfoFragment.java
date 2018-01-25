package com.yzx.chat.view.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.contract.ContactInfoContract;
import com.yzx.chat.presenter.ContactInfoPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.view.FlowLayout;

import java.util.ArrayList;
import java.util.List;


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
    private TextView mTvRemarkTitle;
    private TextView mTvLabelDescription;
    private ConstraintLayout mLabelLayout;
    private ConstraintLayout mTelephoneLayout;
    private ConstraintLayout mDescriptionLayout;
    private FlowLayout mFlContentLabel;
    private LinearLayout mLlContentTelephone;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_info;
    }

    @Override
    protected void init(View parentView) {
        mSwitchTop = parentView.findViewById(R.id.FriendProfileActivity_mSwitchTop);
        mSwitchRemind = parentView.findViewById(R.id.FriendProfileActivity_mSwitchRemind);
        mClClearMessage = parentView.findViewById(R.id.FriendProfileActivity_mClClearMessage);
        mLabelLayout = parentView.findViewById(R.id.FriendProfileActivity_mLabelLayout);
        mTelephoneLayout = parentView.findViewById(R.id.FriendProfileActivity_mTelephoneLayout);
        mDescriptionLayout = parentView.findViewById(R.id.FriendProfileActivity_mDescriptionLayout);
        mTvRemarkTitle = parentView.findViewById(R.id.FriendProfileActivity_mTvRemarkTitle);
        mFlContentLabel = parentView.findViewById(R.id.FriendProfileActivity_mFlContentLabel);
        mLlContentTelephone = parentView.findViewById(R.id.FriendProfileActivity_mLlContentTelephone);
        mTvLabelDescription = parentView.findViewById(R.id.FriendProfileActivity_mTvLabelDescription);
    }

    @Override
    protected void setup() {
        mSwitchTop.setOnCheckedChangeListener(mOnTopSwitchChangeListener);
        mSwitchRemind.setOnCheckedChangeListener(mOnRemindSwitchChangeListener);
        mClClearMessage.setOnClickListener(mOnClearMessageClickListener);

        setData((ContactBean) getArguments().getParcelable(ARGUMENT_CONTACT));

        mPresenter.init(mContactBean);
    }

    private void setData(ContactBean contact) {
        ContactRemarkBean contactRemark = contact.getRemark();
        boolean isShowRemarkTitle = false;
        if (contactRemark != null) {
            mFlContentLabel.removeAllViews();
            List<String> tags = contactRemark.getTags();
            if (tags != null && tags.size() != 0) {
                isShowRemarkTitle = true;
                mLabelLayout.setVisibility(View.VISIBLE);
                mFlContentLabel.setLineSpace((int) AndroidUtil.dip2px(8));
                mFlContentLabel.setItemSpace((int) AndroidUtil.dip2px(8));
                for (String tag : tags) {
                    TextView label = (TextView) getLayoutInflater().inflate(R.layout.item_label_small, mFlContentLabel, false);
                    label.setText(tag);
                    mFlContentLabel.addView(label);
                }
            }

            String telephone = contactRemark.getTelephone();
            if (!TextUtils.isEmpty(telephone)) {
                mLlContentTelephone.removeAllViews();
                isShowRemarkTitle = true;
                mTelephoneLayout.setVisibility(View.VISIBLE);
                TextView textView = new TextView(mContext);
                textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                textView.setTextSize(15);
                textView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                textView.setText(telephone);
                mLlContentTelephone.addView(textView);
            }


            String description = contactRemark.getDescription();
            if (!TextUtils.isEmpty(description)) {
                isShowRemarkTitle = true;
                mDescriptionLayout.setVisibility(View.VISIBLE);
                mTvLabelDescription.setText(description);
            }

            if (isShowRemarkTitle) {
                mTvRemarkTitle.setVisibility(View.VISIBLE);
            }
        }
        mContactBean = contact;
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
            new MaterialDialog.Builder(mContext)
                    .content("是否删除所有聊天记录？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                mPresenter.clearChatMessages();
                            }
                        }
                    })
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

    @Override
    public void updateContactInfo(ContactBean contact) {
        if (mContactBean.equals(contact)) {
            setData(contact);
        } else {
            LogUtil.e("unknown contact");
        }
    }
}
