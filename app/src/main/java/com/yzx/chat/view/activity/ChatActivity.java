package com.yzx.chat.view.activity;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.text.emoji.widget.EmojiEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.yzx.chat.R;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.presenter.ChatPresenter;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.tool.SharePreferenceManager;
import com.yzx.chat.util.EmojiUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoiceRecorder;
import com.yzx.chat.widget.adapter.ChatMessageAdapter;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.listener.OnRecyclerViewClickListener;
import com.yzx.chat.widget.listener.OnScrollToBottomListener;
import com.yzx.chat.widget.view.AmplitudeView;
import com.yzx.chat.widget.view.EmojiRecyclerview;
import com.yzx.chat.widget.view.EmotionPanelRelativeLayout;
import com.yzx.chat.widget.view.KeyboardPanelSwitcher;
import com.yzx.chat.widget.view.RecorderButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */
public class ChatActivity extends BaseCompatActivity<ChatContract.Presenter> implements ChatContract.View {

    public static final int MAX_VOICE_RECORDER_DURATION = 60 * 1000;
    private static final int MIN_VOICE_RECORDER_DURATION = 1000;

    private static final int MORE_INPUT_TYPE_NONE = 0;
    private static final int MORE_INPUT_TYPE_EMOTICONS = 1;
    private static final int MORE_INPUT_TYPE_MICROPHONE = 2;

    private static final int REQUEST_PERMISSION_VOICE_RECORDER = 1;

    public static final String ACTION_EXIT = "Exit";
    public static final String INTENT_CONVERSATION_ID = "ConversationID";

    private ExitReceiver mExitReceiver;
    private RecyclerView mRvChatView;
    private Toolbar mToolbar;
    private ImageView mIvSendMessage;
    private ImageView mIvEmoticons;
    private ImageView mIvMicrophone;
    private EmojiEditText mEtContent;
    private LinearLayout mLlRecorderLayout;
    private KeyboardPanelSwitcher mLlInputLayout;
    private EmotionPanelRelativeLayout mEmotionPanelLayout;
    private AmplitudeView mAmplitudeView;
    private RecorderButton mBtnRecorder;
    private TextView mTvRecorderHint;
    private VoiceRecorder mVoiceRecorder;
    private ChatMessageAdapter mAdapter;
    private CountDownTimer mVoiceRecorderDownTimer;

    private List<EMMessage> mMessageList;
    private int[] mEmojis;

    private int mKeyBoardHeight;
    private boolean isOpenedKeyBoard;
    private int isShowMoreTypeAfterCloseKeyBoard;

    private boolean isHasVoiceRecorderPermission;

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
        mIvEmoticons = findViewById(R.id.ChatActivity_mIvEmoticons);
        mIvMicrophone = findViewById(R.id.ChatActivity_mIvMicrophone);
        mLlInputLayout = findViewById(R.id.ChatActivity_mLlInputLayout);
        mEmotionPanelLayout = findViewById(R.id.ChatActivity_mEmotionPanelLayout);
        mLlRecorderLayout = findViewById(R.id.ChatActivity_mLlRecorderLayout);
        mAmplitudeView = findViewById(R.id.ChatActivity_mAmplitudeView);
        mBtnRecorder = findViewById(R.id.ChatActivity_mBtnRecorder);
        mTvRecorderHint = findViewById(R.id.ChatActivity_mTvRecorderHint);
        mMessageList = new ArrayList<>(64);
        mAdapter = new ChatMessageAdapter(mMessageList);
        mExitReceiver = new ExitReceiver();
        mVoiceRecorder = new VoiceRecorder();
        mEmojis = EmojiUtil.getCommonlyUsedEmojiUnicode();
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

        setVoiceRecorder();

        if (mKeyBoardHeight > 0) {
            mEmotionPanelLayout.setHeight(mKeyBoardHeight);
        }

    }

    private void setEmotionPanel() {
        EmojiRecyclerview emojiRecyclerview = new EmojiRecyclerview(this);
        emojiRecyclerview.setHasFixedSize(true);
        emojiRecyclerview.setEmojiData(mEmojis, 7);
        emojiRecyclerview.setEmojiSize(24);
        emojiRecyclerview.setPadding((int) AndroidUtil.dip2px(8), 0, (int) AndroidUtil.dip2px(8), 0);
        emojiRecyclerview.addOnItemTouchListener(new OnRecyclerViewClickListener() {
            @Override
            public void onItemClick(int position, View itemView) {
                mEtContent.getText().append(new String(Character.toChars(mEmojis[position])));
            }
        });


        mEmotionPanelLayout.addEmotionPanelPage(emojiRecyclerview, getDrawable(R.drawable.ic_moments));
        mEmotionPanelLayout.setRightMenu(getDrawable(R.drawable.ic_setting), null);
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
                if (isShowMoreTypeAfterCloseKeyBoard != MORE_INPUT_TYPE_NONE) {
                    showMoreInput(isShowMoreTypeAfterCloseKeyBoard);
                    isShowMoreTypeAfterCloseKeyBoard = MORE_INPUT_TYPE_NONE;
                } else {
                    hintMoreInput();
                }
                isOpenedKeyBoard = false;
            }
        });
        mIvEmoticons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMoreInput(MORE_INPUT_TYPE_EMOTICONS);
            }
        });
        mIvMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMoreInput(MORE_INPUT_TYPE_MICROPHONE);
            }
        });
    }

    private void setVoiceRecorder() {
        mAmplitudeView.setBackgroundColor(ContextCompat.getColor(this, R.color.theme_background_color));
        mAmplitudeView.setAmplitudeColor(ContextCompat.getColor(this, R.color.theme_main_color));
        mAmplitudeView.setMaxAmplitude(VoiceRecorder.MAX_AMPLITUDE);
        mVoiceRecorder.setMaxDuration(MAX_VOICE_RECORDER_DURATION);
        mVoiceRecorder.setOnRecorderStateListener(new VoiceRecorder.OnRecorderStateListener() {
            @Override
            public void onComplete(String filePath, long duration) {
                if (duration < MIN_VOICE_RECORDER_DURATION) {
                    mVoiceRecorder.cancelAndDelete();
                    showToast(getString(R.string.ChatActivity_VoiceRecorderVeryShort));
                }
                if (new File(filePath).exists()) {
                    mPresenter.sendVoiceRecorder(filePath, (int) (duration / 1000));
                } else {
                    showToast(getString(R.string.ChatActivity_VoiceRecorderFail));
                }
                resetVoiceState();
            }

            @Override
            public void onError(String error) {
                LogUtil.e(error);
                showToast(getString(R.string.ChatActivity_VoiceRecorderFail));
                resetVoiceState();
            }
        });
        mVoiceRecorder.setOnAmplitudeChange(new VoiceRecorder.OnAmplitudeChange() {
            @Override
            public void onAmplitudeChange(int amplitude) {
                mAmplitudeView.setCurrentAmplitude(amplitude);
            }
        }, null);

        mBtnRecorder.setOnRecorderTouchListener(new RecorderButton.onRecorderTouchListener() {
            private boolean isOutOfBounds;

            @Override
            public void onDown() {
                if (mVoiceRecorder.getAmplitudeChangeHandler() == null) {
                    mVoiceRecorder.setAmplitudeChangeHandler(new Handler(mAmplitudeView.getLooper()));
                }
                mVoiceRecorder.setSavePath(DirectoryManager.getVoiceRecorderPath() + "a.amr");
                mVoiceRecorder.prepare();
                mVoiceRecorder.start();
                mVoiceRecorderDownTimer.start();
                mTvRecorderHint.setText(R.string.ChatActivity_SlideCancelSend);
            }

            @Override
            public void onUp() {
                if (isOutOfBounds) {
                    onCancel();
                } else {
                    mVoiceRecorder.stop();
                    resetVoiceState();
                }
            }

            @Override
            public void onOutOfBoundsChange(boolean isOutOfBounds) {
                this.isOutOfBounds = isOutOfBounds;
                if (isOutOfBounds) {
                    mTvRecorderHint.setText(R.string.ChatActivity_UpCancelSend);
                } else {
                    mTvRecorderHint.setText(R.string.ChatActivity_SlideCancelSend);
                }
            }

            @Override
            public void onCancel() {
                mVoiceRecorder.cancelAndDelete();
                resetVoiceState();
            }
        });

        mVoiceRecorderDownTimer = new CountDownTimer(MAX_VOICE_RECORDER_DURATION + 2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mAmplitudeView.setTime((int) Math.round((MAX_VOICE_RECORDER_DURATION + 2000 - millisUntilFinished) / 1000.0));
            }

            @Override
            public void onFinish() {
            }
        };
    }

    private void setData(Intent intent) {
        String conversationID = intent.getStringExtra(INTENT_CONVERSATION_ID);
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
        finish();
        overridePendingTransition(R.anim.avtivity_slide_in_left, R.anim.activity_slide_out_right);
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
    public void onRequestPermissionsSuccess(int requestCode) {
        switch (requestCode) {
            case REQUEST_PERMISSION_VOICE_RECORDER:
                if (!isHasVoiceRecorderPermission) {
                    isHasVoiceRecorderPermission = true;
                    toggleMoreInput(MORE_INPUT_TYPE_MICROPHONE);
                }
                break;
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
        if (mMessageList.size() == 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemRangeInserted(0, 1);
        }
        mMessageList.add(message);
        mRvChatView.scrollToPosition(0);
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

    @Override
    public void updateMessageState(int position) {
        mAdapter.notifyItemChanged(mMessageList.size() - position - 1);
    }

    @Override
    public List<EMMessage> getAllMessage() {
        return mMessageList;
    }

    private void resetVoiceState() {
        mVoiceRecorderDownTimer.cancel();
        mAmplitudeView.resetContent();
        mTvRecorderHint.setText(R.string.ChatActivity_VoiceRecorderHint);
        mVoiceRecorder.stop();
        mBtnRecorder.reset();
    }

    private void toggleMoreInput(int mode) {
        switch (mode) {
            case MORE_INPUT_TYPE_EMOTICONS:
                if (isShowMoreInput()) {
                    if (mEmotionPanelLayout.getVisibility() == View.VISIBLE) {
                        hintMoreInput();
                    } else {
                        hintMoreInput();
                        showMoreInput(MORE_INPUT_TYPE_EMOTICONS);
                    }
                } else if (isOpenedKeyBoard) {
                    hideSoftKeyboard();
                    isShowMoreTypeAfterCloseKeyBoard = MORE_INPUT_TYPE_EMOTICONS;
                } else {
                    showMoreInput(MORE_INPUT_TYPE_EMOTICONS);
                }
                break;
            case MORE_INPUT_TYPE_MICROPHONE:
                if (!isHasVoiceRecorderPermission) {
                    requestPermissionsInCompatMode(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_VOICE_RECORDER);
                    return;
                }
                if (isShowMoreInput()) {
                    if (mLlRecorderLayout.getVisibility() == View.VISIBLE) {
                        showSoftKeyboard(mEtContent);
                    } else {
                        hintMoreInput();
                        showMoreInput(MORE_INPUT_TYPE_MICROPHONE);
                    }
                } else if (isOpenedKeyBoard) {
                    hideSoftKeyboard();
                    isShowMoreTypeAfterCloseKeyBoard = MORE_INPUT_TYPE_MICROPHONE;
                } else {
                    showMoreInput(MORE_INPUT_TYPE_MICROPHONE);
                }
                break;

        }
    }

    private boolean isShowMoreInput() {
        return mEmotionPanelLayout.getVisibility() == View.VISIBLE || mLlRecorderLayout.getVisibility() == View.VISIBLE;
    }

    private void hintMoreInput() {
        mEmotionPanelLayout.setVisibility(View.GONE);
        mLlRecorderLayout.setVisibility(View.GONE);
        mIvMicrophone.setImageResource(R.drawable.ic_microphone);
    }

    private void showMoreInput(int type) {
        switch (type) {
            case MORE_INPUT_TYPE_EMOTICONS:
                mEmotionPanelLayout.setVisibility(View.VISIBLE);
                break;
            case MORE_INPUT_TYPE_MICROPHONE:
                mIvMicrophone.setImageResource(R.drawable.ic_keyboard);
                mLlRecorderLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private class ExitReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ChatActivity.this.finish();
        }
    }
}
