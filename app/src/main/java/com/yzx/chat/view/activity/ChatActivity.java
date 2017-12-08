package com.yzx.chat.view.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.text.emoji.widget.EmojiEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.hyphenate.chat.EMMessage;
import com.yzx.chat.R;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.presenter.ChatPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.tool.SharePreferenceManager;
import com.yzx.chat.util.EmojiUtil;
import com.yzx.chat.widget.adapter.ChatMessageAdapter;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.listener.OnScrollToBottomListener;
import com.yzx.chat.widget.view.EmojiRecyclerview;
import com.yzx.chat.widget.view.EmotionPanelRelativeLayout;
import com.yzx.chat.widget.view.KeyboardPanelSwitcher;

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
    private ImageView mIvShowMore;
    private EmojiEditText mEtContent;
    private KeyboardPanelSwitcher mLlInputLayout;
    private EmotionPanelRelativeLayout mEmotionPanelLayout;
    private ChatMessageAdapter mAdapter;

    private List<EMMessage> mMessageList;

    private int mKeyBoardHeight;
    private boolean isOpenedKeyBoard;
    private boolean isShowMoreInputAfterCloseKeyBoard;

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
        mToolbar = findViewById(R.id.ChatActivity_mToolbar);
        mRvChatView = findViewById(R.id.ChatActivity_mRvChatView);
        mIvSendMessage = findViewById(R.id.ChatActivity_mIvSendMessage);
        mEtContent = findViewById(R.id.ChatActivity_mEtContent);
        mIvShowMore = findViewById(R.id.ChatActivity_mIvShowMore);
        mLlInputLayout = findViewById(R.id.ChatActivity_mLlInputLayout);
        mEmotionPanelLayout = findViewById(R.id.ChatActivity_mEmotionPanelLayout);
        mMessageList = new ArrayList<>(64);
        mAdapter = new ChatMessageAdapter(mMessageList);
        mExitReceiver = new ExitReceiver();
        mKeyBoardHeight = SharePreferenceManager.getInstance().getConfigurePreferences().getKeyBoardHeight();
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

        setKeyBoardSwitcherListener();

        setEmotionPanel();

        if (mKeyBoardHeight > 0) {
            mEmotionPanelLayout.setHeight(mKeyBoardHeight);
        }

    }

    private void setEmotionPanel() {
        EmojiRecyclerview emojiRecyclerview = new EmojiRecyclerview(this);
        emojiRecyclerview.setHasFixedSize(true);
        emojiRecyclerview.setEmojiData(EmojiUtil.getCommonlyUsedEmojiUnicode(),7);
        emojiRecyclerview.setEmojiSize(24);
        emojiRecyclerview.setPadding((int) AndroidUtil.dip2px(8),0,(int) AndroidUtil.dip2px(8),0);
        mEmotionPanelLayout.addEmotionPanelPage(emojiRecyclerview,getDrawable(R.drawable.ic_moments));

        mEmotionPanelLayout.setRightMenu(getDrawable(R.drawable.ic_setting),null);
    }

    private void setKeyBoardSwitcherListener() {
        mLlInputLayout.setOnKeyBoardSwitchListener(new KeyboardPanelSwitcher.onSoftKeyBoardSwitchListener() {
            @Override
            public void onSoftKeyBoardOpened(int keyBoardHeight) {
                if (mKeyBoardHeight != keyBoardHeight) {
                    mKeyBoardHeight = keyBoardHeight;
                    mEmotionPanelLayout.setHeight(mKeyBoardHeight);
                    SharePreferenceManager.getInstance().getConfigurePreferences().putKeyBoardHeight(mKeyBoardHeight);
                }
                if (isShowMoreInput()) {
                    hintMoreInput();
                }
                isOpenedKeyBoard = true;
            }

            @Override
            public void onSoftKeyBoardClosed() {
                if (isShowMoreInputAfterCloseKeyBoard) {
                    showMoreInput();
                    isShowMoreInputAfterCloseKeyBoard = false;
                } else {
                    hintMoreInput();
                }
                isOpenedKeyBoard = false;
            }
        });
        mIvShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowMoreInput()) {
                    hintMoreInput();
                } else if (isOpenedKeyBoard) {
                    hideSoftKeyboard();
                    isShowMoreInputAfterCloseKeyBoard = true;
                } else {
                    showMoreInput();
                }
            }
        });
    }

    private void setData(Intent intent) {
        mEtContent.clearFocus();
        String conversationID = intent.getStringExtra(INTENT_CONVERSATION_ID);
        mMessageList.clear();
        setTitle(conversationID);
        mPresenter.init(conversationID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEtContent.clearFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mExitReceiver);
    }


    @Override
    public void onBackPressed() {
        if (isShowMoreInput()) {
            hintMoreInput();
            return;
        }
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
    public void showNewMessage(EMMessage message) {
        List<EMMessage> messageList = new ArrayList<>(1);
        messageList.add(message);
        showNewMessage(messageList);
    }

    @Override
    public void showNewMessage(List<EMMessage> messageList) {
        if (mMessageList.size() == 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemRangeInserted(0, messageList.size());
        }
        mMessageList.addAll(messageList);
        mRvChatView.scrollToPosition(0);
    }

    @Override
    public void showMoreMessage(List<EMMessage> messageList, boolean isHasMoreMessage) {
        if (messageList != null && messageList.size() != 0) {
            mAdapter.notifyItemRangeInserted(mMessageList.size(), messageList.size());
            mMessageList.addAll(0, messageList);
        }
        if (!isHasMoreMessage) {
            mAdapter.setLoadMoreHint(getString(R.string.LoadMoreHint_NoMore));
            mAdapter.notifyItemChanged(mMessageList.size());
        }
    }

    private boolean isShowMoreInput() {
        return mEmotionPanelLayout.getVisibility() == View.VISIBLE;
    }

    private void hintMoreInput() {
        mEmotionPanelLayout.setVisibility(View.GONE);
    }

    private void showMoreInput() {
        mEmotionPanelLayout.setVisibility(View.VISIBLE);
    }

    private class ExitReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ChatActivity.this.finish();
        }
    }
}
