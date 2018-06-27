package com.yzx.chat.mvp.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.ContactProfileContract;
import com.yzx.chat.mvp.presenter.ContactProfilePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.StringUtil;
import com.yzx.chat.widget.listener.AppBarStateChangeListener;
import com.yzx.chat.widget.view.FlowLayout;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;


public class ContactProfileActivity extends BaseCompatActivity<ContactProfileContract.Presenter> implements ContactProfileContract.View {

    public static final String INTENT_EXTRA_CONTACT_ID = "ContactID";

    private ImageView mIvStartChat;
    private ImageView mIvAvatar;
    private ImageView mIvSexIcon;
    private TextView mTvTitle;
    private TextView mTvDescribe;
    private TextView mTvNickname;
    private TextView mTvRemarkName;
    private TextView mTvLocationAndAge;
    private TextView mTvLastLabel;
    private ConstraintLayout mClClearMessage;
    private Switch mSwitchTop;
    private Switch mSwitchRemind;
    private TextView mTvContentDescription;
    private ConstraintLayout mNicknameLayout;
    private ConstraintLayout mTelephoneLayout;
    private ConstraintLayout mDescriptionLayout;
    private LinearLayout mLlContentTelephone;
    private LinearLayout mLlRemarkTitleLayout;
    private ProgressDialog mProgressDialog;
    private AppBarLayout mAppBarLayout;
    private FlowLayout mLabelFlowLayout;

    private String mContactID;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_contact_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mAppBarLayout = findViewById(R.id.ContactProfileActivity_mAppBarLayout);
        mIvStartChat = findViewById(R.id.ContactProfileActivity_mIvStartChat);
        mTvNickname = findViewById(R.id.ContactProfileActivity_mTvNickname);
        mTvDescribe = findViewById(R.id.ContactProfileActivity_mTvDescribe);
        mIvAvatar = findViewById(R.id.ContactProfileActivity_mIvAvatar);
        mIvSexIcon = findViewById(R.id.ContactProfileActivity_mIvSexIcon);
        mTvTitle = findViewById(R.id.ContactProfileActivity_mTvTitle);
        mTvLocationAndAge = findViewById(R.id.ContactProfileActivity_mTvLocationAndAge);
        mSwitchTop = findViewById(R.id.ChatSetup_mSwitchTop);
        mSwitchRemind = findViewById(R.id.ChatSetup_mSwitchRemind);
        mClClearMessage = findViewById(R.id.ChatSetup_mClClearMessage);
        mNicknameLayout = findViewById(R.id.ContactProfileActivity_mRemarkNameLayout);
        mTelephoneLayout = findViewById(R.id.ContactProfileActivity_mTelephoneLayout);
        mDescriptionLayout = findViewById(R.id.ContactProfileActivity_mDescriptionLayout);
        mLlRemarkTitleLayout = findViewById(R.id.ContactProfileActivity_mLlRemarkTitleLayout);
        mTvRemarkName = findViewById(R.id.ContactProfileActivity_mTvContentRemarkName);
        mLlContentTelephone = findViewById(R.id.ContactProfileActivity_mLlContentTelephone);
        mTvContentDescription = findViewById(R.id.ContactProfileActivity_mTvContentDescription);
        mLabelFlowLayout = findViewById(R.id.ContactProfileActivity_mLabelFlowLayout);
        mTvLastLabel = (TextView) getLayoutInflater().inflate(R.layout.item_label_normal, mLabelFlowLayout, false);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Delete));
        mContactID = getIntent().getStringExtra(INTENT_EXTRA_CONTACT_ID);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (TextUtils.isEmpty(mContactID)) {
            LogUtil.e("contactID==NULL");
            finish();
            return;
        }
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        setTitle(null);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLabelFlowLayout.setLineSpace((int) AndroidUtil.dip2px(8));
        mLabelFlowLayout.setItemSpace((int) AndroidUtil.dip2px(8));

        int size = (int) AndroidUtil.dip2px(10);
        Drawable drawable = getDrawable(R.drawable.ic_add);
        if (drawable != null) {
            drawable.setTint(Color.WHITE);
            drawable.setBounds(0, 0, size, size);
        }
        mTvLastLabel.setText(R.string.EditContactLabelActivity_Title);
        mTvLastLabel.setCompoundDrawables(null, null, drawable, null);
        mTvLastLabel.setPadding(size, mTvLastLabel.getPaddingTop(), size * 2 / 3, mTvLastLabel.getPaddingBottom());
        mTvLastLabel.setCompoundDrawablePadding(size / 4);

        mTvTitle.setAlpha(0);
        mIvStartChat.setOnClickListener(mOnViewClickListener);
        mClClearMessage.setOnClickListener(mOnViewClickListener);
        mNicknameLayout.setOnClickListener(mOnViewClickListener);
        mTelephoneLayout.setOnClickListener(mOnViewClickListener);
        mDescriptionLayout.setOnClickListener(mOnViewClickListener);
        mLabelFlowLayout.setOnClickListener(mOnViewClickListener);
        mAppBarLayout.addOnOffsetChangedListener(mAppBarStateChangeListener);
        mSwitchTop.setOnCheckedChangeListener(mOnTopSwitchChangeListener);
        mSwitchRemind.setOnCheckedChangeListener(mOnRemindSwitchChangeListener);
        mPresenter.init(mContactID);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ContactMenu_ReviseRemarks:
                startRemarkInfoActivity();
                break;
            case R.id.ContactMenu_DeleteContact:
                mPresenter.deleteContact();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == EditContactLabelActivity.RESULT_CODE) {
            ContactBean contact = mPresenter.getContact();
            ArrayList<String> newTags = data.getStringArrayListExtra(EditContactLabelActivity.INTENT_EXTRA_LABEL);
            ArrayList<String> oldTags = contact.getRemark().getTags();
            if (!StringUtil.isEquals(newTags, oldTags, true)) {
                contact.getRemark().setTags(newTags);
                mPresenter.saveRemarkInfo(contact);
            }
        }
    }

    private void startRemarkInfoActivity() {
        Intent intent = new Intent(this, RemarkInfoActivity.class);
        intent.putExtra(RemarkInfoActivity.INTENT_EXTRA_CONTACT, mPresenter.getContact());
        startActivityForResult(intent, 0);
    }

    private void startChatActivity() {
        Intent intent = new Intent(ContactProfileActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_ID, mContactID);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_TYPE_CODE, Conversation.ConversationType.PRIVATE.getValue());
        startActivity(intent);
        finish();
    }

    private void startEditContactLabelActivity() {
        Intent intent = new Intent(ContactProfileActivity.this, EditContactLabelActivity.class);
        intent.putExtra(EditContactLabelActivity.INTENT_EXTRA_LABEL, mPresenter.getContact().getRemark().getTags());
        intent.putExtra(EditContactLabelActivity.INTENT_EXTRA_SELECTABLE_LABEL, mPresenter.getAllTags());
        startActivityForResult(intent, 0);
    }

    private void showDeleteChatMessageHintDialog() {
        new MaterialDialog.Builder(ContactProfileActivity.this)
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

    private final View.OnClickListener mOnViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ContactProfileActivity_mRemarkNameLayout:
                case R.id.ContactProfileActivity_mTelephoneLayout:
                case R.id.ContactProfileActivity_mDescriptionLayout:
                    startRemarkInfoActivity();
                    break;
                case R.id.ContactProfileActivity_mIvStartChat:
                    startChatActivity();
                    break;
                case R.id.ChatSetup_mClClearMessage:
                    showDeleteChatMessageHintDialog();
                    break;
                case R.id.ContactProfileActivity_mLabelFlowLayout:
                    startEditContactLabelActivity();
                    break;

            }
        }
    };

    private AppBarStateChangeListener mAppBarStateChangeListener = new AppBarStateChangeListener() {
        @Override
        public void onStateChanged(AppBarLayout appBarLayout, int state) {
            if (state == AppBarStateChangeListener.STATE_COLLAPSED) {
                mTvTitle.animate().alpha(1).setDuration(200);
            } else {
                mTvTitle.animate().alpha(0).setDuration(200);
            }
        }
    };


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

    @Override
    public ContactProfileContract.Presenter getPresenter() {
        return new ContactProfilePresenter();
    }

    @Override
    public void updateContactInfo(ContactBean contact) {
        mTvTitle.setText(contact.getName());
        mTvNickname.setText(contact.getUserProfile().getNickname());
        UserBean user = contact.getUserProfile();
        mIvSexIcon.setSelected(user.getSex() == UserBean.SEX_WOMAN);
//        if (user.getAge() > 0) {
//            mTvLocationAndAge.setText(user.getAge() + "岁");
//        } else {
//            mTvLocationAndAge.setText("年龄保密");
//        }
//        if (!TextUtils.isEmpty(user.getLocation())) {
//            mTvLocationAndAge.setText(" · " + user.getLocation());
//        }
//        if (TextUtils.isEmpty(user.getSignature())) {
//            mTvDescribe.setText("个性签名：无");
//        } else {
//            mTvDescribe.setText(user.getSignature());
//        }
        GlideUtil.loadAvatarFromUrl(this, mIvAvatar, user.getAvatar());
        mLabelFlowLayout.removeAllViews();

        ContactRemarkBean contactRemark = contact.getRemark();
        boolean isShowRemarkTitle = false;
        if (!TextUtils.isEmpty(contactRemark.getRemarkName())) {
            mNicknameLayout.setVisibility(View.VISIBLE);
            mTvRemarkName.setText(contactRemark.getRemarkName());
            isShowRemarkTitle = true;
        } else {
            mNicknameLayout.setVisibility(View.GONE);
        }


        List<String> tags = contactRemark.getTags();
        if (tags != null && tags.size() != 0) {
            for (String tag : tags) {
                TextView label = (TextView) getLayoutInflater().inflate(R.layout.item_label_normal, mLabelFlowLayout, false);
                label.setText(tag);
                mLabelFlowLayout.addView(label);
            }
        }


        mLlContentTelephone.removeAllViews();
        List<String> telephones = contactRemark.getTelephone();
        if (telephones != null && telephones.size() > 0) {
            isShowRemarkTitle = true;
            mTelephoneLayout.setVisibility(View.VISIBLE);
            for (String telephone : telephones) {
                TextView textView = new TextView(ContactProfileActivity.this);
                textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                textView.setTextSize(13.5f);
                textView.setTextColor(ContextCompat.getColor(ContactProfileActivity.this, R.color.colorAccent));
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
                        new MaterialDialog.Builder(ContactProfileActivity.this)
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
            mLlRemarkTitleLayout.setVisibility(View.VISIBLE);
        }
        mLabelFlowLayout.addView(mTvLastLabel);
    }

    @Override
    public void finishChatActivity() {
        Activity activity = AndroidUtil.getLaunchActivity(ChatActivity.class);
        if (activity != null) {
            activity.finish();
        }
    }

    @Override
    public void showError(String error) {
        showToast(error);
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void setEnableProgressDialog(boolean isEnable) {
        if (isEnable) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
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
