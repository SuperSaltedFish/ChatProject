package com.yzx.chat.view.activity;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.yzx.chat.R;
import com.yzx.chat.widget.adapter.ChatAdapter;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.test.ChatTestData;

import java.util.List;

import static com.yzx.chat.test.ChatTestData.getTestData;


public class ChatActivity extends BaseCompatActivity {
    public final static String SHARED_ELEMENTS_BOTTOM_LAYOUT = "BottomLayout";
    public final static String SHARED_ELEMENTS_CONTENT = "Content";
    public final static String INTENT_ID_INFO = "IdInfo";

    private RecyclerView mRvChatView;
    private Toolbar mToolbar;
    // private Button mBtnSendMes;
    private EditText mEtContent;
    private RelativeLayout mRlBottomLayout;
    private ChatAdapter mAdapter;
    private ConversationBean mConversationBean;

    private List<ChatTestData.ChatBean> mChatList = getTestData();

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConversationBean = getIntent().getParcelableExtra(INTENT_ID_INFO);
//        if (mConversationBean == null) {
//            finish();
//            return;
//        }
        initView();
        setView();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.ChatActivity_mToolbar);
        mRvChatView = (RecyclerView) findViewById(R.id.ChatActivity_mRvChatView);
        //  mBtnSendMes = (Button) findViewById(R.id.ChatActivity_mBtnSendMes);
        mEtContent = (EditText) findViewById(R.id.ChatActivity_mEtContent);
        mRlBottomLayout = (RelativeLayout) findViewById(R.id.ChatActivity_mRlBottomLayout);
        mAdapter = new ChatAdapter(mChatList);
    }


    private void setView() {
        mRlBottomLayout.setTransitionName(SHARED_ELEMENTS_BOTTOM_LAYOUT);

        //mToolbar.setTitle(mConversationBean.getName());
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
            ChatTestData.ChatBean chatBean = new ChatTestData.ChatBean(ChatTestData.ChatBean.CHAT_SEND, ChatTestData.ChatBean.TYPE_TEXT, mEtContent.getText().toString());
            mChatList.add(0, chatBean);
            mAdapter.notifyItemInserted(0);
            mRvChatView.scrollToPosition(0);
        }
    };

    @Override
    public void onBackPressed() {
        finish();
    }
}
