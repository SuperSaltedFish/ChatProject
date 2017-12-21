package com.yzx.chat.tool;


import android.content.Context;
import android.text.TextUtils;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.database.ContactDao;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatClientManager {

    private static ChatClientManager sManager;

    public static void init(Context appContext, String imAppKey) {
        RongIMClient.init(appContext, imAppKey);
        sManager = new ChatClientManager();
    }

    public static ChatClientManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("ChatClientManager is not initialized");
        }
        return sManager;
    }


    private RongIMClient mRongIMClient;
    private Map<OnChatMessageReceiveListener, String> mMessageListenerMap;
    private Map<OnMessageSendStateChangeListener, String> mMessageSendStateChangeListenerMap;
    private List<OnContactMessageReceiveListener> mOnContactMessageReceiveListenerList;
    private List<onConnectionStateChangeListener> mOnConnectionStateChangeListenerList;

    private ContactDao mContactDao;


    private ChatClientManager() {
        mMessageListenerMap = new HashMap<>();
        mMessageSendStateChangeListenerMap = new HashMap<>();
        mOnContactMessageReceiveListenerList = new LinkedList<>();
        mOnConnectionStateChangeListenerList = new LinkedList<>();

        mContactDao = DBManager.getInstance().getContactDao();
        mRongIMClient = RongIMClient.getInstance();

        RongIMClient.setOnReceiveMessageListener(mOnReceiveMessageListener);
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);
    }

    public void login(String token, RongIMClient.ConnectCallback callback) {
        RongIMClient.connect(token, callback);
    }

    public void logout() {
        mRongIMClient.logout();
    }

    public List<Conversation> getAllConversations() {
        return mRongIMClient.getConversationList();
    }

    public List<Conversation> getAllConversations(Conversation.ConversationType... type) {
        return mRongIMClient.getConversationList(type);
    }

    public Conversation getConversation(Conversation.ConversationType type, String targetId) {
        return mRongIMClient.getConversation(type, targetId);
    }

    public void clearConversationUnreadStatus(Conversation.ConversationType type, String conversationID) {
        mRongIMClient.clearMessagesUnreadStatus(type, conversationID);
    }


    public List<Message> getHistoryMessages(Conversation.ConversationType type, String conversationID, int oldestMessageId, int count) {
        return mRongIMClient.getHistoryMessages(type, conversationID, oldestMessageId, count);
    }

    public void asyncGetHistoryMessages(Conversation.ConversationType type, String conversationID, int oldestMessageId, int count, RongIMClient.ResultCallback<List<Message>> callback) {
        mRongIMClient.getHistoryMessages(type, conversationID, oldestMessageId, count, callback);
    }

    public void sendMessage(Message message) {
        mRongIMClient.sendMessage(message, null, null, mSendMessageCallback);
    }

    private final RongIMClient.ConnectionStatusListener mConnectionStatusListener = new RongIMClient.ConnectionStatusListener() {

        private boolean isConnected;

        @Override
        public void onChanged(ConnectionStatus connectionStatus) {
            boolean isConnectionSuccess;
            isConnectionSuccess = connectionStatus == ConnectionStatus.CONNECTED;
            if (isConnected == isConnectionSuccess) {
                return;
            }
            isConnected = isConnectionSuccess;
            for (onConnectionStateChangeListener listener : mOnConnectionStateChangeListenerList) {
                if (isConnected) {
                    listener.onConnected();
                } else {
                    listener.onDisconnected(connectionStatus.getMessage());
                }
            }
        }
    };

    private final RongIMClient.OnReceiveMessageListener mOnReceiveMessageListener = new RongIMClient.OnReceiveMessageListener() {
        @Override
        public boolean onReceived(Message message, int i) {
            switch (message.getObjectName()) {
                case "RC:TxtMsg":
                case "RC:VcMsg":
                    if (mMessageListenerMap.size() == 0) {
                        return false;
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
                            chatListener.onChatMessageReceived(message, i);
                        }
                    }
                    break;
                case "RC:RC:ContactNtf":
                    ContactNotificationMessage contactMessage = (ContactNotificationMessage) message.getContent();
                    ContactBean bean = new ContactBean();
                    bean.setUserTo(contactMessage.getTargetUserId());
                    bean.setUserFrom(contactMessage.getSourceUserId());
                    bean.setReason(contactMessage.getMessage());
                    bean.setRemind(true);
                    bean.setTime((int) (message.getReceivedTime() / 1000));
                    switch (contactMessage.getOperation()) {
                        case ContactNotificationMessage.CONTACT_OPERATION_REQUEST:
                            bean.setType(ContactBean.CONTACT_TYPE_REQUEST);
                            break;
                        case ContactNotificationMessage.CONTACT_OPERATION_ACCEPT_RESPONSE:
                            bean.setType(ContactBean.CONTACT_TYPE_ACCEPTED);
                            break;
                        case ContactNotificationMessage.CONTACT_OPERATION_REJECT_RESPONSE:
                            bean.setType(ContactBean.CONTACT_TYPE_DECLINED);
                            break;
                    }
                    mContactDao.replace(bean);
                    for (OnContactMessageReceiveListener contactListener : mOnContactMessageReceiveListenerList) {
                        contactListener.onContactMessageReceive(contactMessage);
                    }
                    break;
            }

            return true;
        }
    };

    private final IRongCallback.ISendMessageCallback mSendMessageCallback = new IRongCallback.ISendMessageCallback() {
        @Override
        public void onAttached(Message message) {

        }

        @Override
        public void onSuccess(Message message) {

        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {

        }
    };


//    private class MessageSendCallBack implements EMCallBack {
//
//        private EMMessage mEMMessage;
//
//        MessageSendCallBack(EMMessage message) {
//            mEMMessage = message;
//        }
//
//        @Override
//        public void onSuccess() {
//            String conversationID = mEMMessage.conversationId();
//            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
//                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
//                    entry.getKey().onSendSuccess(mEMMessage);
//                }
//            }
//        }
//
//        @Override
//        public void onError(int code, String error) {
//            LogUtil.e(error);
//            String conversationID = mEMMessage.conversationId();
//            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
//                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
//                    entry.getKey().onSendFail(mEMMessage);
//                }
//            }
//        }
//
//        @Override
//        public void onProgress(int progress, String status) {
//            String conversationID = mEMMessage.conversationId();
//            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
//                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
//                    entry.getKey().onSendProgress(mEMMessage, progress);
//                }
//            }
//        }
//    }


    public void addOnMessageReceiveListener(OnChatMessageReceiveListener listener, String conversationID) {
        if (!mMessageListenerMap.containsKey(listener)) {
            mMessageListenerMap.put(listener, conversationID);
        }
    }

    public void removeOnMessageReceiveListener(OnChatMessageReceiveListener listener) {
        mMessageListenerMap.remove(listener);
    }


    public void addContactListener(OnContactMessageReceiveListener listener) {
        if (!mOnContactMessageReceiveListenerList.contains(listener)) {
            mOnContactMessageReceiveListenerList.add(listener);
        }
    }

    public void removeContactListener(OnContactMessageReceiveListener listener) {
        mOnContactMessageReceiveListenerList.remove(listener);
    }


    public void addOnMessageSendStateChangeListener(OnMessageSendStateChangeListener listener, String conversationID) {
        if (!mMessageSendStateChangeListenerMap.containsKey(listener)) {
            mMessageSendStateChangeListenerMap.put(listener, conversationID);
        }
    }

    public void removeOnMessageSendStateChangeListener(OnMessageSendStateChangeListener listener) {
        mMessageSendStateChangeListenerMap.remove(listener);
    }

    public void addConnectionListener(onConnectionStateChangeListener listener) {
        if (!mOnConnectionStateChangeListenerList.contains(listener)) {
            mOnConnectionStateChangeListenerList.add(listener);
        }
    }

    public void removeConnectionListener(onConnectionStateChangeListener listener) {
        mOnConnectionStateChangeListenerList.remove(listener);
    }

    public interface OnMessageSendStateChangeListener {

//        void onSendProgress(EMMessage message, int progress);
//
//        void onSendSuccess(EMMessage message);
//
//        void onSendFail(EMMessage message);
    }


    public interface OnChatMessageReceiveListener {
        void onChatMessageReceived(Message message, int untreatedCount);
    }

    public interface OnContactMessageReceiveListener {
        void onContactMessageReceive(ContactNotificationMessage message);
    }

    public interface UnreadCountChangeListener {
        void onMessageUnreadCountChange(int unreadCount);

        void onContactUnreadCountChange(int unreadCount);
    }

    public interface onConnectionStateChangeListener {

        void onConnected();

        void onDisconnected(String reason);

    }
}
