package com.yzx.chat.mvp.view.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
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
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.ContactInfoContract;
import com.yzx.chat.mvp.presenter.ContactInfoPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.widget.view.FlowLayout;

import java.util.List;


/**
 * Created by YZX on 2018年01月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactInfoFragment extends BaseFragment<ContactInfoContract.Presenter> implements ContactInfoContract.View {

    private static final String ARGUMENT_CONTACT_ID = "ContactID";

    public static ContactInfoFragment newInstance(String contactID) {

        Bundle args = new Bundle();

        ContactInfoFragment fragment = new ContactInfoFragment();
        args.putString(ARGUMENT_CONTACT_ID, contactID);
        fragment.setArguments(args);
        return fragment;
    }

    private ConstraintLayout mClClearMessage;
    private Switch mSwitchTop;
    private Switch mSwitchRemind;
    private TextView mTvRemarkTitle;
    private TextView mTvContentDescription;
    private ConstraintLayout mLabelLayout;
    private ConstraintLayout mTelephoneLayout;
    private ConstraintLayout mDescriptionLayout;
    private FlowLayout mFlContentLabel;
    private LinearLayout mLlContentTelephone;
    private TextView mTvContentNickname;
    private TextView mTvContentLocation;
    private TextView mTvContentBirthday;

    private String mContactID;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_info;
    }

    @Override
    protected void init(View parentView) {
        mContactID = getArguments().getString(ARGUMENT_CONTACT_ID);
        mSwitchTop = parentView.findViewById(R.id.ChatSetup_mSwitchTop);
        mSwitchRemind = parentView.findViewById(R.id.ChatSetup_mSwitchRemind);
        mClClearMessage = parentView.findViewById(R.id.ChatSetup_mClClearMessage);
        mLabelLayout = parentView.findViewById(R.id.FriendProfileActivity_mLabelLayout);
        mTelephoneLayout = parentView.findViewById(R.id.FriendProfileActivity_mTelephoneLayout);
        mDescriptionLayout = parentView.findViewById(R.id.FriendProfileActivity_mDescriptionLayout);
        mTvRemarkTitle = parentView.findViewById(R.id.FriendProfileActivity_mTvRemarkTitle);
        mFlContentLabel = parentView.findViewById(R.id.FriendProfileActivity_mFlContentLabel);
        mLlContentTelephone = parentView.findViewById(R.id.FriendProfileActivity_mLlContentTelephone);
        mTvContentDescription = parentView.findViewById(R.id.FriendProfileActivity_mTvContentDescription);
        mTvContentNickname = parentView.findViewById(R.id.Profile_mTvContentNickname);
        mTvContentLocation = parentView.findViewById(R.id.Profile_mTvContentLocation);
        mTvContentBirthday = parentView.findViewById(R.id.Profile_mTvContentBirthday);

    }

    @Override
    protected void setup() {
        mSwitchTop.setOnCheckedChangeListener(mOnTopSwitchChangeListener);
        mSwitchRemind.setOnCheckedChangeListener(mOnRemindSwitchChangeListener);
        mClClearMessage.setOnClickListener(mOnClearMessageClickListener);

        mPresenter.init(mContactID);
    }

    private void setData(ContactBean contact) {
        UserBean user = contact.getUserProfile();
        mTvContentNickname.setText(user.getNickname());
        if (TextUtils.isEmpty(user.getLocation())) {
            mTvContentLocation.setText(R.string.EditProfileActivity_NoSet);
        } else {
            mTvContentLocation.setText(user.getLocation());
        }
        String birthday = user.getBirthday();
        if (!TextUtils.isEmpty(user.getBirthday())) {
            birthday = DateUtil.isoFormatTo(getString(R.string.DateFormat_yyyyMMdd), birthday);
            if (!TextUtils.isEmpty(birthday)) {
                mTvContentBirthday.setText(birthday);
            } else {
                mTvContentBirthday.setText(R.string.EditProfileActivity_NoSet);
            }
        } else {
            mTvContentBirthday.setText(R.string.EditProfileActivity_NoSet);
        }

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
            } else {
                mLabelLayout.setVisibility(View.GONE);
            }

            mLlContentTelephone.removeAllViews();
            List<String> telephones = contactRemark.getTelephone();
            if (telephones != null && telephones.size() > 0) {
                isShowRemarkTitle = true;
                mTelephoneLayout.setVisibility(View.VISIBLE);
                for (String telephone : telephones) {
                    TextView textView = new TextView(mContext);
                    textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    textView.setTextSize(16);
                    textView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    textView.setText(telephone);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String telephone = ((TextView) v).getText().toString();
                            if (TextUtils.isEmpty(telephone)) {
                                return;
                            }
                            final String headStr = getString(R.string.ContactInfoFragment_Call);
                            final String content = headStr + "  " + telephone;
                            new MaterialDialog.Builder(mContext)
                                    .items(content)
                                    .itemsColor(Color.BLACK)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telephone));//跳转到拨号界面，同时传递电话号码
                                            startActivity(dialIntent);
                                        }
                                    })
                                    .show();
                        }
                    });
                    mLlContentTelephone.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            } else {
                mTelephoneLayout.setVisibility(View.GONE);
            }


            String description = contactRemark.getDescription();
            if (!TextUtils.isEmpty(description)) {
                isShowRemarkTitle = true;
                mDescriptionLayout.setVisibility(View.VISIBLE);
                mTvContentDescription.setText(description);
            } else {
                mDescriptionLayout.setVisibility(View.GONE);
            }

            if (isShowRemarkTitle) {
                mTvRemarkTitle.setVisibility(View.VISIBLE);
            }
        }
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
        setData(contact);
    }
}
