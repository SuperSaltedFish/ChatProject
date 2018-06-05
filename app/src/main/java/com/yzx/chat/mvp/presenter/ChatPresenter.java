package com.yzx.chat.mvp.presenter;

import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.mvp.contract.ChatContract;
import com.yzx.chat.network.chat.ChatManager;
import com.yzx.chat.network.chat.ConversationManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.extra.VideoMessage;
import com.yzx.chat.util.LogUtil;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class ChatPresenter implements ChatContract.Presenter {

    public static volatile String sConversationID;

    private ChatContract.View mChatView;
    private Handler mHandler;
    private IMClient mIMClient;

    private Conversation mConversation;
    private boolean mHasMoreMessage;
    private boolean mIsLoadingMore;
    private boolean mIsConversationStateChange;

    @Override
    public void attachView(ChatContract.View view) {
        mChatView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
    }

    @Override
    public void detachView() {
        reset();
        mChatView = null;
        mHandler = null;
    }

    @Override
    public Conversation init(int ConversationTypeCode, String conversationID) {
        reset();
        Conversation.ConversationType type = Conversation.ConversationType.setValue(ConversationTypeCode);
        Conversation conversation = mIMClient.conversationManager().getConversation(type, conversationID);
        if (conversation == null) {
            conversation = new Conversation();
        }
        switch (type) {
            case PRIVATE:
                ContactBean contact = mIMClient.contactManager().getContact(conversationID);
                if (contact != null) {
                    conversation.setConversationTitle(contact.getName());
                }
                break;
            case GROUP:
                GroupBean group = mIMClient.groupManager().getGroup(conversationID);
                if (group != null) {
                    conversation.setConversationTitle(group.getNameAndMemberNumber());
                }
                break;
            default:
                conversation = null;

        }
        if (conversation != null) {
            conversation.setTargetId(conversationID);
            conversation.setConversationType(type);
            mConversation = conversation;
            sConversationID = mConversation.getTargetId();
            mChatView.clearMessage();
            if (mConversation.getUnreadMessageCount() != 0) {
                mIMClient.conversationManager().clearConversationUnreadStatus(mConversation);
            }
            List<Message> messageList = mIMClient.chatManager().getHistoryMessages(mConversation, -1, Constants.CHAT_MESSAGE_PAGE_SIZE);
            mHasMoreMessage = messageList != null && messageList.size() >= Constants.CHAT_MESSAGE_PAGE_SIZE;
            mChatView.enableLoadMoreHint(mHasMoreMessage);
            mChatView.addNewMessage(messageList);

            mIMClient.chatManager().addOnMessageReceiveListener(mOnChatMessageReceiveListener, sConversationID);
            mIMClient.chatManager().addOnMessageSendStateChangeListener(mOnMessageSendListener, sConversationID);
            mIMClient.conversationManager().addConversationStateChangeListener(mOnConversationStateChangeListener);
        }
        return conversation;
    }

    private void reset() {
        mIMClient.chatManager().removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mIMClient.chatManager().removeOnMessageSendStateChangeListener(mOnMessageSendListener);
        mIMClient.conversationManager().removeConversationStateChangeListener(mOnConversationStateChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mConversation = null;
        sConversationID = null;
        mHasMoreMessage = true;
        mIsConversationStateChange = false;
        mIsLoadingMore = false;
    }

    @Override
    public void resendMessage(Message message) {
        sendMessage(message);
    }


    @Override
    public void sendTextMessage(String content) {
        sendMessage(TextMessage.obtain(content));
    }

    @Override
    public void sendVoiceMessage(String filePath, int timeLengthSec) {
        sendMessage(VoiceMessage.obtain(Uri.fromFile(new File(filePath)), timeLengthSec));
    }

    @Override
    public void sendImageMessage(String imagePath, boolean isOriginal) {
        Uri uri = Uri.parse("file://" + imagePath);
        sendMessage(ImageMessage.obtain(uri, uri, isOriginal));
    }

    @Override
    public void sendLocationMessage(PoiItem poi) {
        LatLonPoint latLonPoint = poi.getLatLonPoint();
        double latitude = latLonPoint.getLatitude();
        double longitude = latLonPoint.getLongitude();
        String url = String.format(Locale.getDefault(), Constants.URL_MAP_IMAGE_FORMAT, longitude, latitude);
        String title = poi.getTitle();
        String address = poi.getSnippet();
        LocationMessage locationMessage = LocationMessage.obtain(latitude, longitude, title + "/" + address, Uri.parse(url));
        locationMessage.setExtra(poi.getPoiId());
        sendMessage(locationMessage);
    }

    @Override
    public void sendVideoMessage(String filePath) {
        Uri uri = Uri.parse("file://" + filePath);
        sendMessage(VideoMessage.obtain(uri));
    }

    @Override
    public void loadMoreMessage(int lastMessageID) {
        mIsLoadingMore = true;
        mIMClient.chatManager().asyncGetHistoryMessages(
                mConversation,
                lastMessageID,
                Constants.CHAT_MESSAGE_PAGE_SIZE,
                new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        if (messages == null || messages.size() < Constants.CHAT_MESSAGE_PAGE_SIZE) {
                            mHasMoreMessage = false;
                        }
                        loadMoreComplete(messages);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        LogUtil.e(errorCode.getMessage());
                        loadMoreComplete(null);
                    }
                });
    }

    @Override
    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }

    @Override
    public boolean hasMoreMessage() {
        return mHasMoreMessage;
    }

    @Override
    public void setVoiceMessageAsListened(Message message) {
        mIMClient.chatManager().setVoiceMessageAsListened(message);
    }

    @Override
    public void saveMessageDraft(String draft) {
        String oldDraft = mConversation.getDraft();
        if (((TextUtils.isEmpty(draft) && TextUtils.isEmpty(oldDraft)) || draft.equals(oldDraft)) && !mIsConversationStateChange) {
            return;
        }
        mIMClient.conversationManager().saveConversationDraft(mConversation, draft);
    }

    @Override
    public ContactBean getContact() {
        if (mConversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            return mIMClient.contactManager().getContact(mConversation.getTargetId());
        }
        return null;
    }

    @Override
    public GroupBean getGroup() {
        if (mConversation.getConversationType() == Conversation.ConversationType.GROUP) {
            return mIMClient.groupManager().getGroup(mConversation.getTargetId());
        }
        return null;
    }

    private void sendMessage(MessageContent messageContent) {
        sendMessage(Message.obtain(mConversation.getTargetId(), mConversation.getConversationType(), messageContent));
    }

    private void sendMessage(Message message) {
        mIMClient.chatManager().sendMessage(message);
    }

    private void loadNewComplete(final Message message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatView.addNewMessage(message);
            }
        });
    }

    private void loadMoreComplete(final List<Message> messages) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsLoadingMore = false;
                mChatView.addMoreMessage(messages, mHasMoreMessage);
            }
        });
    }


    private final ChatManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatManager.OnChatMessageReceiveListener() {

        @Override
        public void onChatMessageReceived(Message message, int untreatedCount) {
            loadNewComplete(message);
            mIsConversationStateChange = true;
        }
    };

    private final ChatManager.OnMessageSendListener mOnMessageSendListener = new ChatManager.OnMessageSendListener() {
        @Override
        public void onAttached(Message message) {
            mChatView.addNewMessage(message);
            mIsConversationStateChange = true;
        }

        @Override
        public void onProgress(Message message, int progress) {

        }

        @Override
        public void onSuccess(Message message) {
            mChatView.updateMessage(message);
        }

        @Override
        public void onError(Message message) {
            mChatView.updateMessage(message);
        }

        @Override
        public void onCanceled(Message message) {
            mChatView.updateMessage(message);
        }
    };

    private final ConversationManager.OnConversationStateChangeListener mOnConversationStateChangeListener = new ConversationManager.OnConversationStateChangeListener() {
        @Override
        public void onConversationStateChange(final Conversation conversation, int typeCode) {
            if (conversation.getTargetId().equals(sConversationID)) {
                switch (typeCode) {
                    case ConversationManager.UPDATE_TYPE_CLEAR_MESSAGE:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mChatView.clearMessage();
                                mChatView.enableLoadMoreHint(false);
                                mHasMoreMessage = false;
                            }
                        });
                        break;
                    case ConversationManager.UPDATE_TYPE_REMOVE:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mChatView.goBack();
                            }
                        });
                        break;
                }
            }
        }
    };

}
