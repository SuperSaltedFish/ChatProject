package com.yzx.chat.view.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.ContactProfileContract;
import com.yzx.chat.presenter.ContactProfilePresenter;
import com.yzx.chat.widget.adapter.ContactProfilePagerAdapter;
import com.yzx.chat.widget.view.ProgressDialog;

import io.rong.imlib.model.Conversation;

public class ContactProfileActivity extends BaseCompatActivity<ContactProfileContract.Presenter> implements ContactProfileContract.View {

    public static final String INTENT_EXTRA_CONTACT = "Contact";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ImageView mIvStartChat;
    private ContactProfilePagerAdapter mPagerAdapter;
    private ProgressDialog mProgressDialog;

    private ContactBean mContactBean;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_contact_profile;
    }

    @Override
    protected void init() {
        mContactBean = getIntent().getParcelableExtra(INTENT_EXTRA_CONTACT);
        if (mContactBean == null) {
            finish();
        }
        mTabLayout = findViewById(R.id.FriendProfileActivity_mTabLayout);
        mViewPager = findViewById(R.id.FriendProfileActivity_mViewPager);
        mIvStartChat = findViewById(R.id.FriendProfileActivity_mIvStartChat);
        mPagerAdapter = new ContactProfilePagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.ContactProfilePagerTitle), mContactBean);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ContactProfileActivity_DeleteProgressHint));
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(mContactBean.getName());
        }

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mIvStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactProfileActivity.this, ChatActivity.class);
                Conversation conversation = new Conversation();
                conversation.setConversationType(Conversation.ConversationType.PRIVATE);
                conversation.setTargetId(mContactBean.getUserProfile().getUserID());
                conversation.setConversationTitle(mContactBean.getName());
                intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION, conversation);
                startActivity(intent);
                finish();
            }
        });

        mPresenter.init(mContactBean);
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
                intent.putExtra(RemarkInfoActivity.INTENT_EXTRA_CONTACT, mContactBean);
                startActivityForResult(intent, 0);
                break;
            case R.id.ContactMenu_DeleteContact:
                if (!mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                }
                mPresenter.deleteContact();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        mProgressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public ContactProfileContract.Presenter getPresenter() {
        return new ContactProfilePresenter();
    }

    @Override
    public void updateContactInfo(ContactBean contact) {
        mContactBean = contact;
    }

    @Override
    public void showError(String error) {
        mProgressDialog.dismiss();
        showToast(error);
    }

    @Override
    public void goBack() {
        finish();
    }
}
