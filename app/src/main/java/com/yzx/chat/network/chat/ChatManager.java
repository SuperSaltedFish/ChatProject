package com.yzx.chat.network.chat;

import android.text.TextUtils;

import com.yzx.chat.util.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年12月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ChatManager {

    private static final Conversation.ConversationType[] SUPPORT_CONVERSATION_TYPE = {Conversation.ConversationType.PRIVATE};

    private RongIMClient mRongIMClient;
    private IMClient.SubManagerCallback mSubManagerCallback;
    private Map<OnChatMessageReceiveListener, String> mMessageListenerMap;
    private Map<OnMessageSendStateChangeListener, String> mMessageSendStateChangeListenerMap;
    private List<OnChatMessageUnreadCountChangeListener> mChatMessageUnreadCountChangeListeners;

    private int mUnreadChatMessageCount;
    private final Object mUpdateChatUnreadCountLock = new Object();

    public ChatManager(IMClient.SubManagerCallback subManagerCallback) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
        mRongIMClient = RongIMClient.getInstance();
        mMessageListenerMap = new HashMap<>();
        mMessageSendStateChangeListenerMap = new HashMap<>();
        mChatMessageUnreadCountChangeListeners = Collections.synchronizedList(new LinkedList<OnChatMessageUnreadCountChangeListener>());
    }

    public List<Message> getHistoryMessages(Conversation conversation, int oldestMessageId, int count) {
        return mRongIMClient.getHistoryMessages(conversation.getConversationType(), conversation.getTargetId(), oldestMessageId, count);
    }

    public void asyncGetHistoryMessages(Conversation conversation, int oldestMessageId, int count, RongIMClient.ResultCallback<List<Message>> callback) {
        mRongIMClient.getHistoryMessages(conversation.getConversationType(), conversation.getTargetId(), oldestMessageId, count, callback);
    }

    public void sendMessage(Message message) {
        mRongIMClient.sendMessage(message, null, null, mSendMessageCallback);
    }

    public void setVoiceMessageAsListened(Message message) {
        Message.ReceivedStatus status = message.getReceivedStatus();
        if (status.isListened()) {
            return;
        }
        status.setListened();
        mRongIMClient.setMessageReceivedStatus(message.getMessageId(), status, null);
    }

    public void updateChatUnreadCount() {
        mRongIMClient.getUnreadCount(new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                synchronized (mUpdateChatUnreadCountLock) {
                    if (mUnreadChatMessageCount != integer) {
                        mUnreadChatMessageCount = integer;
                        for (OnChatMessageUnreadCountChangeListener listener : mChatMessageUnreadCountChangeListeners) {
                            listener.onChatMessageUnreadCountChange(mUnreadChatMessageCount);
                        }
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        }, SUPPORT_CONVERSATION_TYPE);
    }

    public void addOnMessageReceiveListener(OnChatMessageReceiveListener listener, String conversationID) {
        if (!mMessageListenerMap.containsKey(listener)) {
            mMessageListenerMap.put(listener, conversationID);
        }
    }

    public void removeOnMessageReceiveListener(OnChatMessageReceiveListener listener) {
        mMessageListenerMap.remove(listener);
    }

    public void addOnMessageSendStateChangeListener(OnMessageSendStateChangeListener listener, String conversationID) {
        if (!mMessageSendStateChangeListenerMap.containsKey(listener)) {
            mMessageSendStateChangeListenerMap.put(listener, conversationID);
        }
    }

    public void removeOnMessageSendStateChangeListener(OnMessageSendStateChangeListener listener) {
        mMessageSendStateChangeListenerMap.remove(listener);
    }

    public void addChatMessageUnreadCountChangeListener(OnChatMessageUnreadCountChangeListener listener) {
        if (!mChatMessageUnreadCountChangeListeners.contains(listener)) {
            mChatMessageUnreadCountChangeListeners.add(listener);
        }
    }

    public void removeChatMessageUnreadCountChangeListener(OnChatMessageUnreadCountChangeListener listener) {
        mChatMessageUnreadCountChangeListeners.remove(listener);
    }

    void onReceiveContactNotificationMessage(Message message, int untreatedCount) {
        if (mMessageListenerMap.size() == 0) {
            return;
        }
        OnChatMessageReceiveListener chatListener;
        String conversationID;
        for (Map.Entry<OnChatMessageReceiveListener, String> entry : mMessageListenerMap.entrySet()) {
            conversationID = entry.getValue();
            chatListener = entry.getKey();
            if (chatListener == null) {
                continue;
            }
            if (TextUtils.isEmpty(conversationID) || conversationID.equals(message.getTargetId())) {
                chatListener.onChatMessageReceived(message, untreatedCount);
            }
        }
    }

    private final IRongCallback.ISendMessageCallback mSendMessageCallback = new IRongCallback.ISendMessageCallback() {
        @Override
        public void onAttached(Message message) {
            String conversationID = message.getTargetId();
            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
                    entry.getKey().onSendProgress(message);
                }
            }
        }

        @Override
        public void onSuccess(Message message) {
            String conversationID = message.getTargetId();
            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
                    entry.getKey().onSendSuccess(message);
                }
            }
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            String conversationID = message.getTargetId();
            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
                    entry.getKey().onSendFail(message);
                }
            }
        }
    };


    public interface OnMessageSendStateChangeListener {

        void onSendProgress(Message message);

        void onSendSuccess(Message message);

        void onSendFail(Message message);
    }

    public interface OnChatMessageUnreadCountChangeListener {
        void onChatMessageUnreadCountChange(int count);
    }

    public interface OnChatMessageReceiveListener {
        void onChatMessageReceived(Message message, int untreatedCount);
    }

}
