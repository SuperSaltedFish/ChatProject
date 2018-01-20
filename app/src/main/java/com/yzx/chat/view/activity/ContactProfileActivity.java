package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.widget.adapter.ContactProfilePagerAdapter;

import io.rong.imlib.model.Conversation;

public class ContactProfileActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_CONTACT = "Contact";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ImageView mIvStartChat;
    private ContactProfilePagerAdapter mPagerAdapter;

    private ContactBean mContactBean;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_friend_profile;
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
        mPagerAdapter = new ContactProfilePagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.ContactProfilePagerTitle));
    }

    @Override
    protected void setup() {
        if(getSupportActionBar()!=null){
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
                conversation.setTargetId(mContactBean.getUserID());
                conversation.setConversationTitle(mContactBean.getName());
                intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION, conversation);
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(ContactProfileActivity.this, R.anim.avtivity_slide_in_right, R.anim.activity_slide_out_left);
                startActivity(intent, compat.toBundle());
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.ContactMenu_UpdateRemarkInfo:
                Intent intent = new Intent(this, RemarkInfoActivity.class);
                intent.putExtra(RemarkInfoActivity.INTENT_EXTRA_CONTACT, mContactBean);
                startActivityForResult(intent,0);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.avtivity_slide_in_left, R.anim.activity_slide_out_right);
    }
}
