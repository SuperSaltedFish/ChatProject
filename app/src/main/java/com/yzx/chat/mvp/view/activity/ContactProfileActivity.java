package com.yzx.chat.mvp.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.yzx.chat.mvp.contract.ContactProfileContract;
import com.yzx.chat.mvp.presenter.ContactProfilePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.ContactProfilePagerAdapter;
import com.yzx.chat.widget.view.ProgressDialog;

import io.rong.imlib.model.Conversation;


public class ContactProfileActivity extends BaseCompatActivity<ContactProfileContract.Presenter> implements ContactProfileContract.View {

    public static final String INTENT_EXTRA_CONTACT_ID = "ContactID";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ImageView mIvStartChat;
    private ImageView mIvAvatar;
    private TextView mTvExplain;
    private TextView mTvNickname;
    private ContactProfilePagerAdapter mPagerAdapter;
    private ProgressDialog mProgressDialog;

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
        mTabLayout = findViewById(R.id.ContactProfileActivity_mTabLayout);
        mViewPager = findViewById(R.id.ContactProfileActivity_mViewPager);
        mIvStartChat = findViewById(R.id.ContactProfileActivity_mIvStartChat);
        mTvNickname = findViewById(R.id.ContactProfileActivity_mTvNickname);
        mTvExplain = findViewById(R.id.ContactProfileActivity_mTvExplain);
        mIvAvatar = findViewById(R.id.ContactProfileActivity_mIvAvatar);
        mPagerAdapter = new ContactProfilePagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.ContactProfilePagerTitle), mContactID);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Delete));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mIvStartChat.setOnClickListener(mOnStartChatClickListener);

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

    @Override
    public ContactProfileContract.Presenter getPresenter() {
        return new ContactProfilePresenter();
    }

    @Override
    public void updateContactInfo(ContactBean contact) {
        setTitle(contact.getName());
        mTvNickname.setText(contact.getName());
        mTvExplain.setText(contact.getUserProfile().getSignature());
        GlideUtil.loadAvatarFromUrl(this, mIvAvatar, contact.getUserProfile().getAvatar());
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
    public void enableProgressDialog(boolean isEnable) {
        if (isEnable) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }
}
