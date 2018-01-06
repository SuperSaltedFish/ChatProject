package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.widget.adapter.ContactProfilePagerAdapter;

import io.rong.imlib.model.Conversation;

public class ContactProfileActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_CONTACT = "Contact";

    private ImageView mIvChat;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ContactProfilePagerAdapter mPagerAdapter;
    private Toolbar mToolbar;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_friend_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setView();
    }

    private void initView() {
//        mIvChat = findViewById(R.id.FriendProfileActivity_mIvChat);
        mTabLayout = findViewById(R.id.FriendProfileActivity_mTabLayout);
        mViewPager = findViewById(R.id.FriendProfileActivity_mViewPager);
        mToolbar = findViewById(R.id.FriendProfileActivity_mToolbar);
        mPagerAdapter = new ContactProfilePagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.ContactProfilePagerTitle));
    }

    private void setView() {
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(null);

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
//        mIvChat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ContactBean bean = getIntent().getParcelableExtra(INTENT_EXTRA_CONTACT);
//                Intent intent = new Intent(ContactProfileActivity.this,ChatActivity.class);
//                Conversation conversation = new Conversation();
//                conversation.setConversationType(Conversation.ConversationType.PRIVATE);
//                conversation.setTargetId(bean.getUserID());
//                conversation.setConversationTitle(bean.getName());
//                intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION,conversation);
//                startActivity(intent);
//                finish();
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_profile,menu);
        return true;
    }

}
