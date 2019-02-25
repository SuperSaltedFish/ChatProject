package com.yzx.chat.module.contact.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
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
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.module.contact.contract.ContactChatSettingContract;
import com.yzx.chat.module.contact.presenter.ContactChatSettingPresenter;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ContactChatSettingFragment extends BaseFragment<ContactChatSettingContract.Presenter> implements ContactChatSettingContract.View {

    private static final String ARGUMENT_CONTENT_ID = "ContentID";

    public static ContactChatSettingFragment newInstance(String contactID) {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_CONTENT_ID,contactID);
        ContactChatSettingFragment fragment = new ContactChatSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ConstraintLayout mNicknameLayout;
    private ConstraintLayout mTelephoneLayout;
    private ConstraintLayout mDescriptionLayout;
    private ConstraintLayout mClClearMessage;
    private TextView mTvRemarkName;
    private TextView mTvContentDescription;
    private LinearLayout mLlContentTelephone;
    private LinearLayout mLlRemarkTitleLayout;
    private Switch mSwitchTop;
    private Switch mSwitchRemind;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_chat_setting;
    }

    @Override
    protected void init(View parentView) {
        mNicknameLayout = parentView.findViewById(R.id.ContactChatSettingFragment_mRemarkNameLayout);
        mTelephoneLayout = parentView.findViewById(R.id.ContactChatSettingFragment_mTelephoneLayout);
        mDescriptionLayout = parentView.findViewById(R.id.ContactChatSettingFragment_mDescriptionLayout);
        mLlRemarkTitleLayout = parentView.findViewById(R.id.ContactChatSettingFragment_mLlRemarkTitleLayout);
        mTvRemarkName = parentView.findViewById(R.id.ContactChatSettingFragment_mTvContentRemarkName);
        mLlContentTelephone = parentView.findViewById(R.id.ContactChatSettingFragment_mLlContentTelephone);
        mTvContentDescription = parentView.findViewById(R.id.ContactChatSettingFragment_mTvContentDescription);
        mSwitchTop = parentView.findViewById(R.id.ChatSetup_mSwitchTop);
        mSwitchRemind = parentView.findViewById(R.id.ChatSetup_mSwitchRemind);
        mClClearMessage = parentView.findViewById(R.id.ChatSetup_mClClearMessage);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mClClearMessage.setOnClickListener(mOnViewClickListener);
        mNicknameLayout.setOnClickListener(mOnViewClickListener);
        mTelephoneLayout.setOnClickListener(mOnViewClickListener);
        mDescriptionLayout.setOnClickListener(mOnViewClickListener);
        mSwitchTop.setOnCheckedChangeListener(mOnTopSwitchChangeListener);
        mSwitchRemind.setOnCheckedChangeListener(mOnRemindSwitchChangeListener);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String contactID = bundle.getString(ARGUMENT_CONTENT_ID);
            if (!TextUtils.isEmpty(contactID)) {
                mPresenter.init(contactID);
                return;
            }
        }
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showDeleteChatMessageHintDialog() {
        new MaterialDialog.Builder(mContext)
                .content(R.string.ChatSetup_DeleteHint)
                .positiveText(R.string.Confirm)
                .negativeText(R.string.Cancel)
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

    private void startRemarkInfoActivity() {
        Intent intent = new Intent(mContext, RemarkInfoActivity.class);
        intent.putExtra(RemarkInfoActivity.INTENT_EXTRA_CONTACT, mPresenter.getContact());
        startActivityForResult(intent, 0);
    }

    private final View.OnClickListener mOnViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.ContactChatSettingFragment_mRemarkNameLayout:
                case R.id.ContactChatSettingFragment_mTelephoneLayout:
                case R.id.ContactChatSettingFragment_mDescriptionLayout:
                    startRemarkInfoActivity();
                    break;
                case R.id.ChatSetup_mClClearMessage:
                    showDeleteChatMessageHintDialog();
                    break;

            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnRemindSwitchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mPresenter.enableConversationNotification(!isChecked);
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnTopSwitchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mPresenter.setConversationToTop(isChecked);
        }
    };

    @Override
    public ContactChatSettingContract.Presenter getPresenter() {
        return new ContactChatSettingPresenter();
    }

    @Override
    public void updateContactInfo(ContactEntity contact) {
        boolean isShowRemarkTitle = false;
        if (!TextUtils.isEmpty(contact.getRemarkName())) {
            mNicknameLayout.setVisibility(View.VISIBLE);
            mTvRemarkName.setText(contact.getRemarkName());
            isShowRemarkTitle = true;
        } else {
            mNicknameLayout.setVisibility(View.GONE);
        }

        mLlContentTelephone.removeAllViews();
        List<String> telephones = contact.getTelephones();
        if (telephones != null && telephones.size() > 0) {
            isShowRemarkTitle = true;
            mTelephoneLayout.setVisibility(View.VISIBLE);
            for (String telephone : telephones) {
                TextView textView = new TextView(mContext);
                textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                textView.setTextSize(13.5f);
                textView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                textView.setText(telephone);
                textView.setOnClickListener(new OnOnlySingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
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

        String description = contact.getDescription();
        if (!TextUtils.isEmpty(description)) {
            isShowRemarkTitle = true;
            mDescriptionLayout.setVisibility(View.VISIBLE);
            mTvContentDescription.setText(description);
        } else {
            mDescriptionLayout.setVisibility(View.GONE);
        }

        if (isShowRemarkTitle) {
            mLlRemarkTitleLayout.setVisibility(View.VISIBLE);
        }
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
