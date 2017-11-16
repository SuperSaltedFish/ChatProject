package com.yzx.chat.view.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hyphenate.chat.EMMessage;
import com.yzx.chat.R;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.presenter.ChatPresenter;
import com.yzx.chat.widget.adapter.ChatMessageAdapter;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.listener.OnScrollToBottomListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */
public class ChatActivity extends BaseCompatActivity<ChatContract.Presenter> implements ChatContract.View {

    public static final String ACTION_EXIT = "Exit";
    public static final String INTENT_CONVERSATION_ID = "ConversationID";

    private ExitReceiver mExitReceiver;
    private RecyclerView mRvChatView;
    private Toolbar mToolbar;
    private ImageView mIvSendMessage;
    private EditText mEtContent;
    private RelativeLayout mRlBottomLayout;
    private ChatMessageAdapter mAdapter;

    private List<EMMessage> mMessageList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setView();
        setData(getIntent());
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.ChatActivity_mToolbar);
        mRvChatView = (RecyclerView) findViewById(R.id.ChatActivity_mRvChatView);
        mIvSendMessage = (ImageView) findViewById(R.id.ChatActivity_mIvSendMessage);
        mEtContent = (EditText) findViewById(R.id.ChatActivity_mEtContent);
        mRlBottomLayout = (RelativeLayout) findViewById(R.id.ChatActivity_mRlBottomLayout);
        mMessageList = new ArrayList<>(64);
        mAdapter = new ChatMessageAdapter(mMessageList);
        mExitReceiver = new ExitReceiver();
    }

    private void setView() {

        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mRvChatView.setLayoutManager(layoutManager);
        mRvChatView.setAdapter(mAdapter);
        mRvChatView.setHasFixedSize(true);

        mAdapter.setScrollToBottomListener(mScrollToBottomListener);

        mIvSendMessage.setOnClickListener(mSendMesClickListener);

        LocalBroadcastManager.getInstance(this).registerReceiver(mExitReceiver, new IntentFilter(ACTION_EXIT));
    }

    private void setData(Intent intent) {
        String conversationID = intent.getStringExtra(INTENT_CONVERSATION_ID);
        mMessageList.clear();
        setTitle(conversationID);
        mPresenter.init(conversationID);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        mPresenter.reset();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.avtivity_slide_in_right, R.anim.activity_slide_out_left);
        setData(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mExitReceiver);
    }

    private final View.OnClickListener mSendMesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = mEtContent.getText().toString();
            if (TextUtils.isEmpty(message)) {
                return;
            }
            mEtContent.setText(null);
            mPresenter.sendMessage(message);
        }
    };

    private final OnScrollToBottomListener mScrollToBottomListener = new OnScrollToBottomListener() {
        @Override
        public void OnScrollToBottom() {
            if (mPresenter.isLoadingMore()) {
                return;
            }
            if (mPresenter.hasMoreMessage()) {
                mAdapter.setLoadMoreHint(getString(R.string.LoadMoreHint_Loading));
                mPresenter.loadMoreMessage(mMessageList.get(0).getMsgId());
            } else {
                mAdapter.setLoadMoreHint(getString(R.string.LoadMoreHint_NoMore));
            }
        }
    };


    @Override
    public ChatContract.Presenter getPresenter() {
        return new ChatPresenter();
    }

    @Override
    public void showNew(EMMessage message) {
        List<EMMessage> messageList = new ArrayList<>(1);
        messageList.add(message);
        showNew(messageList);
    }

    @Override
    public void showNew(List<EMMessage> messageList) {
        if (mMessageList.size() == 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemRangeInserted(0, messageList.size());
        }
        mMessageList.addAll(messageList);
        mRvChatView.scrollToPosition(0);
    }

    @Override
    public void showMore(List<EMMessage> messageList, boolean isHasMoreMessage) {
        if (messageList != null && messageList.size() != 0) {
            mAdapter.notifyItemRangeInserted(mMessageList.size(), messageList.size());
            mMessageList.addAll(0, messageList);
        }
        if (!isHasMoreMessage) {
            mAdapter.setLoadMoreHint(getString(R.string.LoadMoreHint_NoMore));
            mAdapter.notifyItemChanged(mMessageList.size());
        }
    }

    private class ExitReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ChatActivity.this.finish();
        }
    }
}
