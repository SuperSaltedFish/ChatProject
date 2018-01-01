package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;

import io.rong.imlib.model.Conversation;

public class ContactProfileActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_CONTACT = "Contact";

    private ImageView mIvChat;

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
        mIvChat = findViewById(R.id.FriendProfileActivity_mIvChat);
    }

    private void setView() {

        mIvChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactBean bean = getIntent().getParcelableExtra(INTENT_EXTRA_CONTACT);
                Intent intent = new Intent(ContactProfileActivity.this,ChatActivity.class);
                Conversation conversation = new Conversation();
                conversation.setConversationType(Conversation.ConversationType.PRIVATE);
                conversation.setTargetId(bean.getUserID());
                conversation.setConversationTitle(bean.getName());
                intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION,conversation);
                startActivity(intent);
                finish();
            }
        });

    }

}
