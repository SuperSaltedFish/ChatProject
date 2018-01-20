package com.yzx.chat.view.activity;


import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.text.emoji.widget.EmojiEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.presenter.ChatPresenter;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.tool.SharePreferenceManager;
import com.yzx.chat.util.EmojiUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoicePlayer;
import com.yzx.chat.util.VoiceRecorder;
import com.yzx.chat.widget.adapter.ChatMessageAdapter;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.listener.AutoCloseKeyboardScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.Alerter;
import com.yzx.chat.widget.view.AmplitudeView;
import com.yzx.chat.widget.view.EmojiRecyclerview;
import com.yzx.chat.widget.view.EmotionPanelLayout;
import com.yzx.chat.widget.view.KeyboardPanelSwitcher;
import com.yzx.chat.widget.view.RecorderButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */
public class ChatActivity extends BaseCompatActivity<ChatContract.Presenter> implements ChatContract.View {

    public static final int MAX_VOICE_RECORDER_DURATION = 60 * 999;
    private static final int MIN_VOICE_RECORDER_DURATION = 800;

    private static final int MORE_INPUT_TYPE_NONE = 0;
    private static final int MORE_INPUT_TYPE_EMOTICONS = 1;
    private static final int MORE_INPUT_TYPE_MICROPHONE = 2;
    private static final int MORE_INPUT_TYPE_OTHER = 3;

    private static final int REQUEST_PERMISSION_VOICE_RECORDER = 1;

    public static final String INTENT_EXTRA_CONVERSATION = "Conversation";

    private RecyclerView mRvChatView;
    private ImageSwitcher mIsvSendMessage;
    private ImageView mIvEmoticons;
    private ImageView mIvMicrophone;
    private EmojiEditText mEtContent;
    private LinearLayout mLlRecorderLayout;
    private KeyboardPanelSwitcher mLlInputLayout;
    private EmotionPanelLayout mEmotionPanelLayout;
    private AmplitudeView mAmplitudeView;
    private TableLayout mTlOtherPanelLayout;
    private View mFooterView;
    private TextView mTvLoadMoreHint;
    private RecorderButton mBtnRecorder;
    private TextView mTvRecorderHint;
    private VoiceRecorder mVoiceRecorder;
    private ChatMessageAdapter mAdapter;
    private CountDownTimer mVoiceRecorderDownTimer;

    private Alerter mAlerter;
    private Animation mAlerterIconAnimation;

    private Conversation mConversation;
    private List<Message> mMessageList;
    private Message mNeedResendMessage;
    private int mNeedResendPosition;
    private int[] mEmojis;

    private int mKeyBoardHeight;
    private boolean isOpenedKeyBoard;
    private boolean isHasContentInInputBox;
    private int isShowMoreTypeAfterCloseKeyBoard;

    private boolean isHasVoiceRecorderPermission;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    protected void init() {
        mRvChatView = findViewById(R.id.ChatActivity_mRvChatView);
        mIsvSendMessage = findViewById(R.id.ChatActivity_mIsvSendMessage);
        mEtContent = findViewById(R.id.ChatActivity_mEtContent);
        mIvEmoticons = findViewById(R.id.ChatActivity_mIvEmoticons);
        mIvMicrophone = findViewById(R.id.ChatActivity_mIvMicrophone);
        mLlInputLayout = findViewById(R.id.ChatActivity_mLlInputLayout);
        mEmotionPanelLayout = findViewById(R.id.ChatActivity_mEmotionPanelLayout);
        mLlRecorderLayout = findViewById(R.id.ChatActivity_mLlRecorderLayout);
        mAmplitudeView = findViewById(R.id.ChatActivity_mAmplitudeView);
        mBtnRecorder = findViewById(R.id.ChatActivity_mBtnRecorder);
        mTvRecorderHint = findViewById(R.id.ChatActivity_mTvRecorderHint);
        mTlOtherPanelLayout = findViewById(R.id.ChatActivity_mTlOtherPanelLayout);
        mFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) getWindow().getDecorView(), false);
        mTvLoadMoreHint = mFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mMessageList = new ArrayList<>(128);
        mAdapter = new ChatMessageAdapter(mMessageList);
        mVoiceRecorder = new VoiceRecorder();
        mEmojis = EmojiUtil.getCommonlyUsedEmojiUnicode();
        mKeyBoardHeight = SharePreferenceManager.getInstance().getConfigurePreferences().getKeyBoardHeight();
    }

    @Override
    protected void setup() {
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setChatRecyclerViewAndAdapter();

        setEditAndSendStateChangeListener();

        setAlerterDialog();

        setEmotionPanel();

        setKeyBoardSwitcherListener();

        setVoiceRecorder();

        setData(getIntent());
    }

    private void setData(Intent intent) {
        mConversation = intent.getParcelableExtra(INTENT_EXTRA_CONVERSATION);
        if (mConversation == null) {
            finish();
            return;
        }
        setTitle(mConversation.getConversationTitle());
        mPresenter.init(mConversation);
        String draft = mConversation.getDraft();
        if (!TextUtils.isEmpty(draft)) {
            mEtContent.setText(draft);
        }

    }

    private void setChatRecyclerViewAndAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mRvChatView.setLayoutManager(layoutManager);
        mRvChatView.setAdapter(mAdapter);
        mRvChatView.setHasFixedSize(true);
        // mRvChatView.setItemAnimator(new NoAnimations());
        mRvChatView.addOnScrollListener(new AutoCloseKeyboardScrollListener(this));

        mAdapter.setScrollToBottomListener(new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
            @Override
            public void OnScrollToBottom() {
                if (mPresenter.isLoadingMore()) {
                    return;
                }
                if (mPresenter.hasMoreMessage()) {
                    mTvLoadMoreHint.setText(getString(R.string.LoadMoreHint_Loading));
                    mPresenter.loadMoreMessage(mMessageList.get(mMessageList.size() - 1).getMessageId());
                } else {
                    mTvLoadMoreHint.setText(getString(R.string.LoadMoreHint_NoMore));
                }
            }
        });
        mAdapter.setMessageCallback(new ChatMessageAdapter.MessageCallback() {
            @Override
            public void resendMessage(int position, Message message) {
                mNeedResendPosition = position;
                mNeedResendMessage = message;
                mAlerter.show();
            }

            @Override
            public void setVoiceMessageAsListened(Message message) {
                mPresenter.setVoiceMessageAsListened(message);
            }
        });

    }

    private void setEditAndSendStateChangeListener() {
        mIsvSendMessage.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new ImageView(ChatActivity.this);
            }
        });
        mIsvSendMessage.setAnimateFirstView(false);
        mIsvSendMessage.setImageResource(R.drawable.ic_more_input);
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (!isHasContentInInputBox) {
                        isHasContentInInputBox = true;
                        mIsvSendMessage.setImageResource(R.drawable.ic_send);
                    }
                } else {
                    isHasContentInInputBox = false;
                    mIsvSendMessage.setImageResource(R.drawable.ic_more_input);
                }
            }
        });
    }

    private void setAlerterDialog() {
        mAlerter = new Alerter(this, R.layout.alert_dialog_chat);
        final Button btnResend = mAlerter.findViewById(R.id.ChatActivity_mBtnResend);
        final Button btnCancel = mAlerter.findViewById(R.id.ChatActivity_mBtnCancel);
        final ImageView ivIcon = mAlerter.findViewById(R.id.ChatActivity_mIvIcon);
        mAlerterIconAnimation = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAlerterIconAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mAlerterIconAnimation.setDuration(1000);
        mAlerterIconAnimation.setRepeatCount(Animation.INFINITE);
        mAlerterIconAnimation.setRepeatMode(Animation.REVERSE);

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlerter.hide();
                mMessageList.remove(mNeedResendMessage);
                mAdapter.notifyItemRemovedEx(mNeedResendPosition);
                mPresenter.resendMessage(mNeedResendMessage);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlerter.hide();
            }
        });

        mAlerter.setCanceledOnTouchOutside(true);
        mAlerter.setOnShowAndHideListener(new Alerter.OnShowAndHideListener() {
            @Override
            public void onShow() {
                ivIcon.startAnimation(mAlerterIconAnimation);
            }

            @Override
            public void onHide() {
                mAlerterIconAnimation.cancel();
            }
        });
    }


    private void setEmotionPanel() {
        mEmotionPanelLayout.setTabDividerDrawable(ContextCompat.getDrawable(this, R.drawable.divider_vertical_black_light));
        if (mKeyBoardHeight > 0) {
            mEmotionPanelLayout.setHeight(mKeyBoardHeight);
        }
        EmojiRecyclerview emojiRecyclerview = new EmojiRecyclerview(this);
        emojiRecyclerview.setHasFixedSize(true);
        emojiRecyclerview.setEmojiData(mEmojis, 7);
        emojiRecyclerview.setEmojiSize(24);
        emojiRecyclerview.setOverScrollMode(View.OVER_SCROLL_NEVER);
        emojiRecyclerview.setPadding((int) AndroidUtil.dip2px(8), 0, (int) AndroidUtil.dip2px(8), 0);
        emojiRecyclerview.addOnItemTouchListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
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
                mRvChatView.scrollToPosition(0);
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

        mIsvSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHasContentInInputBox) {
                    String message = mEtContent.getText().toString();
                    if (TextUtils.isEmpty(message)) {
                        return;
                    }
                    mEtContent.setText(null);
                    mPresenter.sendTextMessage(message);
                } else {
                    toggleMoreInput(MORE_INPUT_TYPE_OTHER);
                }
            }
        });
    }

    private void setVoiceRecorder() {
        mAmplitudeView.setBackgroundColor(Color.WHITE);
        mAmplitudeView.setAmplitudeColor(ContextCompat.getColor(this, R.color.colorAccent));
        mAmplitudeView.setMaxAmplitude(VoiceRecorder.MAX_AMPLITUDE);
        mVoiceRecorder.setMaxDuration(MAX_VOICE_RECORDER_DURATION);
        mVoiceRecorder.setOnRecorderStateListener(new VoiceRecorder.OnRecorderStateListener() {
            @Override
            public void onComplete(String filePath, long duration) {
                if (duration < MIN_VOICE_RECORDER_DURATION) {
                    mVoiceRecorder.cancelAndDelete();
                    showToast(getString(R.string.ChatActivity_VoiceRecorderVeryShort));
                } else if (new File(filePath).exists()) {
                    mPresenter.sendVoiceMessage(filePath, (int) Math.ceil(duration / 1000.0));
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


    private void resetVoiceState() {
        mVoiceRecorderDownTimer.cancel();
        mAmplitudeView.resetContent();
        mTvRecorderHint.setText(R.string.ChatActivity_VoiceRecorderHint);
        mVoiceRecorder.stop();
        mBtnRecorder.reset();
    }

    private void toggleMoreInput(int mode) {
        if (!isHasVoiceRecorderPermission && mode == MORE_INPUT_TYPE_MICROPHONE) {
            requestPermissionsInCompatMode(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_VOICE_RECORDER);
            return;
        }
        if (isShowMoreInput()) {
            if (mEmotionPanelLayout.getVisibility() == View.VISIBLE && mode == MORE_INPUT_TYPE_EMOTICONS) {
                hintMoreInput();
            } else if (mLlRecorderLayout.getVisibility() == View.VISIBLE && mode == MORE_INPUT_TYPE_MICROPHONE) {
                showSoftKeyboard(mEtContent);
            } else if (mTlOtherPanelLayout.getVisibility() == View.VISIBLE && mode == MORE_INPUT_TYPE_OTHER) {
                hintMoreInput();
            } else {
                hintMoreInput();
                showMoreInput(mode);
            }
        } else if (isOpenedKeyBoard) {
            hideSoftKeyboard();
            isShowMoreTypeAfterCloseKeyBoard = mode;
        } else {
            showMoreInput(mode);
        }
    }

    private boolean isShowMoreInput() {
        return mEmotionPanelLayout.getVisibility() == View.VISIBLE ||
                mLlRecorderLayout.getVisibility() == View.VISIBLE ||
                mTlOtherPanelLayout.getVisibility() == View.VISIBLE;
    }

    private void hintMoreInput() {
        mEmotionPanelLayout.setVisibility(View.GONE);
        mLlRecorderLayout.setVisibility(View.GONE);
        mTlOtherPanelLayout.setVisibility(View.GONE);
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
            case MORE_INPUT_TYPE_OTHER:
                mTlOtherPanelLayout.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mEtContent.clearFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VoicePlayer.getInstance(this).stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlerterIconAnimation.cancel();
        mMessageList = null;
    }


    @Override
    public void onBackPressed() {
        if (isShowMoreInput()) {
            hintMoreInput();
            return;
        }
        mPresenter.saveMessageDraft(mEtContent.getText().toString());
        finish();
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


    @Override
    public ChatContract.Presenter getPresenter() {
        return new ChatPresenter();
    }

    @Override
    public void addNewMessage(Message message) {
        mMessageList.add(0, message);
        if (mMessageList.size() == 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemRangeInsertedEx(0, 1);
        }
        mRvChatView.scrollToPosition(0);
    }

    @Override
    public void addNewMessage(List<Message> messageList) {
        if (mMessageList.size() == 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemRangeInsertedEx(0, messageList.size());
        }
        mMessageList.addAll(0, messageList);
        mRvChatView.scrollToPosition(0);
    }

    @Override
    public void addMoreMessage(List<Message> messageList, boolean isHasMoreMessage) {
        if (messageList != null && messageList.size() != 0) {
            mAdapter.notifyItemRangeInsertedEx(mMessageList.size(), messageList.size());
            mRvChatView.scrollToPosition(mMessageList.size() - 1);
            mMessageList.addAll(messageList);
            mAdapter.notifyItemChangedEx(mMessageList.size());
        }
        if (!isHasMoreMessage) {
            mTvLoadMoreHint.setText(getString(R.string.LoadMoreHint_NoMore));
        } else if (messageList == null) {
            mTvLoadMoreHint.setText(getString(R.string.LoadMoreHint_LoadFail));
        }
    }

    @Override
    public void updateMessage(Message message) {
        int position = mMessageList.indexOf(message);
        if (position >= 0) {
            mAdapter.notifyItemChangedEx(position);
            mMessageList.set(position, message);
        } else {
            LogUtil.e("update message fail in UI");
        }
    }

    @Override
    public void enableLoadMoreHint(boolean isEnable) {
        if (isEnable) {
            mAdapter.addFooterView(mFooterView);
        } else {
            mAdapter.addFooterView(null);
        }
    }

}
