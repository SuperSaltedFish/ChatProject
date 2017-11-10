package com.yzx.chat.view.activity;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.hyphenate.chat.EMMessage;
import com.yzx.chat.R;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.presenter.ChatPresenter;
import com.yzx.chat.widget.adapter.ChatMessageAdapter;
import com.yzx.chat.base.BaseCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends BaseCompatActivity<ChatContract.Presenter> implements ChatContract.View {
    public final static String SHARED_ELEMENTS_BOTTOM_LAYOUT = "BottomLayout";
    public final static String SHARED_ELEMENTS_CONTENT = "Content";
    public final static String INTENT_CONVERSATION_ID = "ConversationID";

    private RecyclerView mRvChatView;
    private Toolbar mToolbar;
    // private Button mBtnSendMes;
    private EditText mEtContent;
    private RelativeLayout mRlBottomLayout;
    private ChatMessageAdapter mAdapter;
    private String mConversationID;

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
        setData();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.ChatActivity_mToolbar);
        mRvChatView = (RecyclerView) findViewById(R.id.ChatActivity_mRvChatView);
        //  mBtnSendMes = (Button) findViewById(R.id.ChatActivity_mBtnSendMes);
        mEtContent = (EditText) findViewById(R.id.ChatActivity_mEtContent);
        mRlBottomLayout = (RelativeLayout) findViewById(R.id.ChatActivity_mRlBottomLayout);
        mMessageList = new ArrayList<>(64);
        mAdapter = new ChatMessageAdapter(mMessageList);

        mConversationID = getIntent().getStringExtra(INTENT_CONVERSATION_ID);
    }


    private void setView() {
        mRlBottomLayout.setTransitionName(SHARED_ELEMENTS_BOTTOM_LAYOUT);

        mToolbar.setTitle(mConversationID);
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
        mRvChatView.scrollToPosition(0);
        mRvChatView.setTransitionName(SHARED_ELEMENTS_CONTENT);

        //  mBtnSendMes.setOnClickListener(mSendMesClickListener);
    }

    private void setData() {
        mPresenter.initMessage(mConversationID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final View.OnClickListener mSendMesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mAdapter.notifyItemInserted(0);
        }
    };

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public ChatContract.Presenter getPresenter() {
        return new ChatPresenter();
    }

    @Override
    public void startShow(List<EMMessage> messageList) {
        mMessageList.clear();
        mMessageList.addAll(messageList);
    }

    @Override
    public void showMore(List<EMMessage> messageList) {
        mAdapter.notifyItemRangeInserted(mMessageList.size(),messageList.size());
        mMessageList.addAll(0,messageList);
    }
}
