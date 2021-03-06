package com.yzx.chat.module.conversation.view;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.entity.BasicInfoProvider;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.common.view.FileSelectorActivity;
import com.yzx.chat.module.common.view.ImageMultiSelectorActivity;
import com.yzx.chat.module.common.view.LocationMapActivity;
import com.yzx.chat.module.common.view.VideoPlayActivity;
import com.yzx.chat.module.common.view.VideoRecorderActivity;
import com.yzx.chat.module.contact.view.ContactProfileActivity;
import com.yzx.chat.module.conversation.contract.ChatContract;
import com.yzx.chat.module.conversation.presenter.ChatPresenter;
import com.yzx.chat.module.group.view.GroupProfileActivity;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.EmojiUtil;
import com.yzx.chat.util.VoicePlayer;
import com.yzx.chat.util.VoiceRecorder;
import com.yzx.chat.widget.adapter.ChatMessageAdapter;
import com.yzx.chat.widget.listener.AutoCloseKeyboardItemTouchListener;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.listener.OnScrollToBottomListener;
import com.yzx.chat.widget.view.AmplitudeView;
import com.yzx.chat.widget.view.EmojiRecyclerview;
import com.yzx.chat.widget.view.EmotionPanelLayout;
import com.yzx.chat.widget.view.KeyboardPanelSwitcher;
import com.yzx.chat.widget.view.RecorderButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiEditText;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatActivity extends BaseCompatActivity<ChatContract.Presenter> implements ChatContract.View {

    private static final int MAX_VOICE_RECORDER_DURATION = Constants.MAX_VOICE_RECORDER_DURATION;
    private static final int MIN_VOICE_RECORDER_DURATION = Constants.MIN_VOICE_RECORDER_DURATION;

    private static final int MORE_INPUT_TYPE_NONE = 0;
    private static final int MORE_INPUT_TYPE_EMOTICONS = 1;
    private static final int MORE_INPUT_TYPE_MICROPHONE = 2;
    private static final int MORE_INPUT_TYPE_OTHER = 3;

    private static final int REQUEST_PERMISSION_VOICE = 1;
    private static final int REQUEST_PERMISSION_IMAGE = 2;
    private static final int REQUEST_PERMISSION_LOCATION = 3;
    private static final int REQUEST_PERMISSION_CAMERA = 4;
    private static final int REQUEST_PERMISSION_FILE = 5;

    private static final String INTENT_EXTRA_CONVERSATION_ID = "ConversationID";
    private static final String INTENT_EXTRA_CONVERSATION_TYPE = "ConversationType";


    public static void startActivity(Context context, String conversationID, Conversation.ConversationType type) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_ID, conversationID);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_TYPE, type);
        context.startActivity(intent);
    }


    private RecyclerView mRvChatView;
    private ImageSwitcher mIsvSendMessage;
    private ImageView mIvEmoticons;
    private ImageView mIvMicrophone;
    private EmojiEditText mEtContent;
    private LinearLayout mLlRecorderLayout;
    private KeyboardPanelSwitcher mLlInputLayout;
    private EmotionPanelLayout mEmotionPanelLayout;
    private AmplitudeView mAmplitudeView;
    private ConstraintLayout mClOtherPanelLayout;
    private View mFooterView;
    private RecorderButton mBtnRecorder;
    private TextView mTvRecorderHint;
    private ImageView mIvSendImage;
    private ImageView mIvSendLocation;
    private ImageView mIvSendVideo;
    private ImageView mIvSendFile;
    private VoiceRecorder mVoiceRecorder;
    private ChatMessageAdapter mAdapter;
    private CountDownTimer mVoiceRecorderDownTimer;


    private List<Message> mMessageList;
    private int[] mEmojis;

    private int mKeyBoardHeight;
    private boolean isOpenedKeyBoard;
    private boolean isHasContentInInputBox;
    private int isShowMoreTypeAfterCloseKeyBoard;
    private boolean isHasVoiceRecorderPermission;

    private String mCurrentConversationID;
    private Conversation.ConversationType mCurrentConversationType;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    protected void init(Bundle savedInstanceState) {
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
        mClOtherPanelLayout = findViewById(R.id.ChatActivity_mClOtherPanelLayout);
        mIvSendImage = findViewById(R.id.ChatActivity_mIvSendImage);
        mIvSendLocation = findViewById(R.id.ChatActivity_mIvSendLocation);
        mIvSendVideo = findViewById(R.id.ChatActivity_mIvSendVideo);
        mIvSendFile = findViewById(R.id.ChatActivity_mIvSendFile);
        mFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) getWindow().getDecorView(), false);
        mMessageList = new ArrayList<>(128);
        mAdapter = new ChatMessageAdapter(mMessageList);
        mVoiceRecorder = new VoiceRecorder();
        mEmojis = EmojiUtil.getCommonlyUsedEmojiUnicode();
        mKeyBoardHeight = mPresenter.getKeyBoardHeight();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

        setDisplayHomeAsUpEnabled(true);

        setChatRecyclerViewAndAdapter();

        setEditAndSendStateChangeListener();

        setEmotionPanel();

        setOtherPanelClickListener();

        setKeyBoardSwitcherListener();

        setVoiceRecorder();

        setData(getIntent());
    }

    private void setData(Intent intent) {
        String conversationID = intent.getStringExtra(INTENT_EXTRA_CONVERSATION_ID);
        mCurrentConversationType = (Conversation.ConversationType) intent.getSerializableExtra(INTENT_EXTRA_CONVERSATION_TYPE);
        if (TextUtils.isEmpty(conversationID) || mCurrentConversationType == null) {
            finish();
            return;
        }
        if (TextUtils.equals(conversationID, mCurrentConversationID)) {
            return;
        }
        mCurrentConversationID = conversationID;
        BasicInfoProvider basicInfoProvider = mPresenter.init(mCurrentConversationID, mCurrentConversationType);
        if (basicInfoProvider == null) {
            finish();
            return;
        }
        mAdapter.setBasicInfoProvider(basicInfoProvider);
        mAdapter.setEnableNameDisplay(mCurrentConversationType == Conversation.ConversationType.GROUP);

        mEtContent.setText(mPresenter.getMessageDraft());
    }

    private void setChatRecyclerViewAndAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mRvChatView.setLayoutManager(layoutManager);

        mRvChatView.setAdapter(mAdapter);
        mRvChatView.setHasFixedSize(true);
        ((DefaultItemAnimator) (Objects.requireNonNull(mRvChatView.getItemAnimator()))).setSupportsChangeAnimations(false);
        mRvChatView.addOnItemTouchListener(new AutoCloseKeyboardItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                super.onInterceptTouchEvent(rv, e);
                if (isShowMoreInput()) {
                    hideMoreInput();
                }
                return false;
            }
        });

        mAdapter.setMessageOperationCallback(new ChatMessageAdapter.MessageOperationCallback() {
            @Override
            public void resendMessage(Message message) {
                mPresenter.resendMessage(message);
            }

            @Override
            public void setVoiceMessageAsListened(Message message) {
                mPresenter.setVoiceMessageAsListened(message);
            }
        });
    }

    private void setEnableAutoLoadMore(boolean isEnable) {
        mRvChatView.removeOnScrollListener(mOnScrollToBottomListener);
        if (isEnable) {
            mRvChatView.addOnScrollListener(mOnScrollToBottomListener);
        }
    }

    private final OnScrollToBottomListener mOnScrollToBottomListener = new OnScrollToBottomListener() {
        @Override
        public void onScrollToBottom() {
            if (mAdapter.isHasFooterView()) {
                return;
            }
            mPresenter.loadMoreMessage(mMessageList.get(mMessageList.size() - 1).getMessageId());
        }
    };

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
        emojiRecyclerview.setPadding((int) AndroidHelper.dip2px(8), 0, (int) AndroidHelper.dip2px(8), 0);
        emojiRecyclerview.addOnItemTouchListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
                mEtContent.getText().append(new String(Character.toChars(mEmojis[position])));
            }
        });

        mEmotionPanelLayout.addEmotionPanelPage(emojiRecyclerview, getDrawable(R.drawable.ic_moments));
        mEmotionPanelLayout.setRightMenu(getDrawable(R.drawable.ic_setting), null);
    }

    private void setOtherPanelClickListener() {
        mIvSendImage.setOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                requestPermissionsInCompatMode(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_IMAGE);
            }
        });

        mIvSendLocation.setOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                requestPermissionsInCompatMode(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE}, REQUEST_PERMISSION_LOCATION);
            }
        });

        mIvSendVideo.setOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                requestPermissionsInCompatMode(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CAMERA);
            }
        });

        mIvSendFile.setOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                requestPermissionsInCompatMode(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_FILE);
            }
        });
    }

    private void setKeyBoardSwitcherListener() {
        mLlInputLayout.setOnKeyBoardSwitchListener(new KeyboardPanelSwitcher.OnSoftKeyBoardSwitchListener() {
            @Override
            public void onSoftKeyBoardOpened(int keyBoardHeight) {
                if (mKeyBoardHeight != keyBoardHeight) {
                    mKeyBoardHeight = keyBoardHeight;
                    mEmotionPanelLayout.setHeight(mKeyBoardHeight);
                    mPresenter.saveKeyBoardHeight(mKeyBoardHeight);
                }
                if (isShowMoreInput()) {
                    hideMoreInput();
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
                    hideMoreInput();
                }
                isOpenedKeyBoard = false;
            }
        });
        mIvEmoticons.setOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                toggleMoreInput(MORE_INPUT_TYPE_EMOTICONS);
            }
        });
        mIvMicrophone.setOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                toggleMoreInput(MORE_INPUT_TYPE_MICROPHONE);
            }
        });

        mIsvSendMessage.setOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
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
                    mPresenter.sendVoiceMessage(filePath, duration);
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

        mBtnRecorder.setOnRecorderTouchListener(new RecorderButton.OnRecorderTouchListener() {
            private boolean isOutOfBounds;

            @Override
            public void onStart() {
                if (mVoiceRecorder.getAmplitudeChangeHandler() == null) {
                    mVoiceRecorder.setAmplitudeChangeHandler(new Handler(mAmplitudeView.getLooper()));
                }
                mVoiceRecorder.setSavePath(DirectoryHelper.getVoiceRecorderPath() + "a.amr");
                mVoiceRecorder.prepare();
                mVoiceRecorder.start();
                mVoiceRecorderDownTimer.start();
                mTvRecorderHint.setText(R.string.ChatActivity_SlideCancelSend);
            }

            @Override
            public void onFinish() {
                if (isOutOfBounds) {
                    onCancel();
                } else {
                    mVoiceRecorder.stop();
                    resetVoiceState();
                }
            }

//            @Override
//            public void onOutOfBoundsChange(boolean isOutOfBounds) {
//                this.isOutOfBounds = isOutOfBounds;
//                if (isOutOfBounds) {
//                    mTvRecorderHint.setText(R.string.ChatActivity_UpCancelSend);
//                } else {
//                    mTvRecorderHint.setText(R.string.ChatActivity_SlideCancelSend);
//                }
//            }

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
            requestPermissionsInCompatMode(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_VOICE);
            return;
        }
        if (isShowMoreInput()) {
            if (mEmotionPanelLayout.getVisibility() == View.VISIBLE && mode == MORE_INPUT_TYPE_EMOTICONS) {
                hideMoreInput();
            } else if (mLlRecorderLayout.getVisibility() == View.VISIBLE && mode == MORE_INPUT_TYPE_MICROPHONE) {
                showSoftKeyboard(mEtContent);
            } else if (mClOtherPanelLayout.getVisibility() == View.VISIBLE && mode == MORE_INPUT_TYPE_OTHER) {
                hideMoreInput();
            } else {
                hideMoreInput();
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
                mClOtherPanelLayout.getVisibility() == View.VISIBLE;
    }

    private void hideMoreInput() {
        mEmotionPanelLayout.setVisibility(View.GONE);
        mLlRecorderLayout.setVisibility(View.GONE);
        mClOtherPanelLayout.setVisibility(View.GONE);
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
                mClOtherPanelLayout.setVisibility(View.VISIBLE);
                break;
        }
        mRvChatView.scrollToPosition(0);
    }

    private void enterProfile() {
        switch (mCurrentConversationType) {
            case PRIVATE:
                ContactProfileActivity.startActivity(ChatActivity.this, mCurrentConversationID);
                break;
            case GROUP:
                Intent intent = new Intent(ChatActivity.this, GroupProfileActivity.class);
                intent.putExtra(GroupProfileActivity.INTENT_EXTRA_GROUP_ID, mCurrentConversationID);
                startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        VoicePlayer.getInstance(this).stop();
        mEtContent.clearFocus();
        hideSoftKeyboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMessageList = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ChatMenu_Profile) {
            enterProfile();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (isShowMoreInput()) {
            hideMoreInput();
            return;
        }
        String draft = mEtContent.getText().toString();
        String oldDraft = mPresenter.getMessageDraft();
        if (!(oldDraft == null && TextUtils.isEmpty(draft)) && !TextUtils.equals(draft, mPresenter.getMessageDraft())) {
            mPresenter.saveMessageDraft(mEtContent.getText().toString());
        }
        finish();
    }

    @Override
    protected void onRequestPermissionsResult(int requestCode, boolean isSuccess, String[] deniedPermissions) {
        if (isSuccess) {
            switch (requestCode) {
                case REQUEST_PERMISSION_VOICE:
                    if (!isHasVoiceRecorderPermission) {
                        isHasVoiceRecorderPermission = true;
                        toggleMoreInput(MORE_INPUT_TYPE_MICROPHONE);
                    }
                    break;
                case REQUEST_PERMISSION_IMAGE:
                    startActivityForResult(new Intent(this, ImageMultiSelectorActivity.class), 0);
                    break;
                case REQUEST_PERMISSION_LOCATION:
                    LocationMapActivity.startForResultOfSendType(this, 0);
                    break;
                case REQUEST_PERMISSION_CAMERA:
                    startActivityForResult(new Intent(this, VideoRecorderActivity.class), 0);
                    break;
                case REQUEST_PERMISSION_FILE:
                    startActivityForResult(new Intent(this, FileSelectorActivity.class), 0);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (resultCode == ImageMultiSelectorActivity.RESULT_CODE) {
            boolean isOriginal = data.getBooleanExtra(ImageMultiSelectorActivity.INTENT_EXTRA_IS_ORIGINAL, false);
            ArrayList<String> imageList = data.getStringArrayListExtra(ImageMultiSelectorActivity.INTENT_EXTRA_IMAGE_PATH_LIST);
            if (imageList != null && imageList.size() > 0) {
                for (String path : imageList) {
                    mPresenter.sendImageMessage(path, isOriginal);
                }
            }
        } else if (resultCode == LocationMapActivity.RESULT_CODE) {
            PoiItem poi = data.getParcelableExtra(LocationMapActivity.INTENT_EXTRA_POI);
            if (poi != null) {
                mPresenter.sendLocationMessage(poi);
            }
        } else if (resultCode == VideoRecorderActivity.RESULT_CODE) {
            String videoPath = data.getStringExtra(VideoRecorderActivity.INTENT_EXTRA_SAVE_PATH);
            if (!TextUtils.isEmpty(videoPath)) {
                mPresenter.sendVideoMessage(videoPath);
            }
        } else if (resultCode == VideoPlayActivity.RESULT_CODE) {
            Message message = data.getParcelableExtra(VideoPlayActivity.INTENT_EXTRA_MESSAGE);
            if (message != null) {
                refreshMessage(message);
            }
        } else if (resultCode == FileSelectorActivity.RESULT_CODE) {
            ArrayList<String> filePathList = data.getStringArrayListExtra(FileSelectorActivity.INTENT_EXTRA_SELECTED_FILE_PATH);
            if (filePathList != null && filePathList.size() > 0) {
                for (String path : filePathList) {
                    mPresenter.sendFileMessage(path);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setData(intent);
    }

    @Override
    public ChatContract.Presenter getPresenter() {
        return new ChatPresenter();
    }

    @Override
    public void showChatTitle(String title) {
        setTitle(title);
    }

    @Override
    public void showNewMessage(Message message) {
        int position = mMessageList.indexOf(message);
        if (position >= 0) {
            mMessageList.remove(position);
            mMessageList.add(0, message);
            mAdapter.notifyItemMovedEx(position, 0);

        } else {
            mMessageList.add(0, message);
            mAdapter.notifyItemRangeInsertedEx(0, 1);
        }
        mRvChatView.scrollToPosition(0);
    }

    @Override
    public void showNewMessage(List<Message> messageList, boolean isHasMoreMessage) {
        mMessageList.addAll(0, messageList);
        mAdapter.notifyItemRangeInsertedEx(0, messageList.size());
        mRvChatView.scrollToPosition(0);
        setEnableAutoLoadMore(isHasMoreMessage);
    }


    @Override
    public void showMoreMessage(List<Message> messageList, boolean isHasMoreMessage) {
        if (messageList != null && messageList.size() != 0) {
            int oldSize = mMessageList.size();
            mMessageList.addAll(messageList);
            mAdapter.notifyItemRangeInsertedEx(oldSize, messageList.size());
            mRvChatView.scrollToPosition(oldSize - 1);
            mAdapter.notifyItemChangedEx(mMessageList.size());
        }
        setEnableAutoLoadMore(isHasMoreMessage);
    }

    @Override
    public void refreshMessage(Message message) {
        int position = mMessageList.indexOf(message);
        if (position >= 0) {
            mMessageList.set(position, message);
            mAdapter.notifyItemChangedEx(position);
        } else {
            LogUtil.e("update message fail in UI");
        }
    }

    @Override
    public void clearMessage() {
        mAdapter.notifyDataSetChanged();
        mMessageList.clear();
    }

    @Override
    public void enableLoadMoreHint(boolean isEnable) {
        if (isEnable) {
            mAdapter.setFooterView(mFooterView);
        } else {
            mAdapter.setFooterView(null);
        }
    }

    @Override
    public void goBack() {
        finish();
    }


}
