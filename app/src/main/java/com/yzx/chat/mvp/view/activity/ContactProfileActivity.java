package com.yzx.chat.mvp.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.ContactProfileContract;
import com.yzx.chat.mvp.presenter.ContactProfilePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.ContactProfilePagerAdapter;
import com.yzx.chat.widget.listener.AppBarStateChangeListener;
import com.yzx.chat.widget.view.ProgressDialog;

import io.rong.imlib.model.Conversation;


public class ContactProfileActivity extends BaseCompatActivity<ContactProfileContract.Presenter> implements ContactProfileContract.View {

    public static final String INTENT_EXTRA_CONTACT_ID = "ContactID";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ImageView mIvStartChat;
    private ImageView mIvAvatar;
    private ImageView mIvSexIcon;
    private TextView mTvTitle;
    private TextView mTvDescribe;
    private TextView mTvNickname;
    private TextView mTvLocationAndAge;
    private ContactProfilePagerAdapter mPagerAdapter;
    private ProgressDialog mProgressDialog;
    private AppBarLayout mAppBarLayout;

    private String mContactID;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_contact_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mContactID = getIntent().getStringExtra(INTENT_EXTRA_CONTACT_ID);
        if (TextUtils.isEmpty(mContactID)) {
            LogUtil.e("contactID==NULL");
            finish();
            return;
        }
        //    mTabLayout = findViewById(R.id.ContactProfileActivity_mTabLayout);
        mAppBarLayout = findViewById(R.id.ContactProfileActivity_mAppBarLayout);
        mViewPager = findViewById(R.id.ContactProfileActivity_mViewPager);
        mIvStartChat = findViewById(R.id.ContactProfileActivity_mIvStartChat);
        mTvNickname = findViewById(R.id.ContactProfileActivity_mTvNickname);
        mTvDescribe = findViewById(R.id.ContactProfileActivity_mTvDescribe);
        mIvAvatar = findViewById(R.id.ContactProfileActivity_mIvAvatar);
        mIvSexIcon = findViewById(R.id.ContactProfileActivity_mIvSexIcon);
        mTvTitle = findViewById(R.id.ContactProfileActivity_mTvTitle);
        mTvLocationAndAge = findViewById(R.id.ContactProfileActivity_mTvLocationAndAge);
        mPagerAdapter = new ContactProfilePagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.ContactProfilePagerTitle), mContactID);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Delete));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        setTitle(null);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTvTitle.setAlpha(0);
        mViewPager.setAdapter(mPagerAdapter);
        // mTabLayout.setupWithViewPager(mViewPager);
        mIvStartChat.setOnClickListener(mOnStartChatClickListener);
        mAppBarLayout.addOnOffsetChangedListener(mAppBarStateChangeListener);

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
                Intent intent = new Intent(this, RemarkInfoActivity.class);
                intent.putExtra(RemarkInfoActivity.INTENT_EXTRA_CONTACT, mPresenter.getContact());
                startActivityForResult(intent, 0);
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
    protected void onDestroy() {
        super.onDestroy();
    }

    private final View.OnClickListener mOnStartChatClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ContactProfileActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_ID, mContactID);
            intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_TYPE_CODE, Conversation.ConversationType.PRIVATE.getValue());
            startActivity(intent);
            finish();
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

    @Override
    public ContactProfileContract.Presenter getPresenter() {
        return new ContactProfilePresenter();
    }

    @Override
    public void updateContactInfo(ContactBean contact) {
        mTvTitle.setText(contact.getName());
        mTvNickname.setText(contact.getName());
        UserBean user = contact.getUserProfile();
        mIvSexIcon.setSelected(user.getSex() == UserBean.SEX_WOMAN);
        if (user.getAge() > 0) {
            mTvLocationAndAge.setText(user.getAge() + "岁");
        } else {
            mTvLocationAndAge.setText("年龄保密");
        }
        if (!TextUtils.isEmpty(user.getLocation())) {
            mTvLocationAndAge.setText(" · " + user.getLocation());
        }
        if (TextUtils.isEmpty(user.getSignature())) {
            mTvDescribe.setText("个性签名：无");
        } else {
            mTvDescribe.setText(user.getSignature());
        }
        GlideUtil.loadAvatarFromUrl(this, mIvAvatar, user.getAvatar());
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
}
