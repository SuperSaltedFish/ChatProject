package com.yzx.chat.tool;


import android.content.Context;
import android.text.TextUtils;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.util.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    public static final Conversation.ConversationType[] SUPPORT_CONVERSATION_TYPE =
            {Conversation.ConversationType.PRIVATE};

    private RongIMClient mRongIMClient;
    private Map<OnChatMessageReceiveListener, String> mMessageListenerMap;
    private Map<OnMessageSendStateChangeListener, String> mMessageSendStateChangeListenerMap;
    private List<OnContactMessageReceiveListener> mOnContactMessageReceiveListenerList;
    private List<onConnectionStateChangeListener> mOnConnectionStateChangeListenerList;
    private List<OnUnreadMessageCountChangeListener> mOnUnreadMessageCountChangeListeners;

    private ThreadPoolExecutor mWorkExecutor;

    private int mUnreadChatMessageCount;
    private int mUnreadContactMessageCount;

    private final Object mUpdateChatUnreadCountLock = new Object();
    private final Object mUpdateContactUnreadCountLock = new Object();

    private ContactDao mContactDao;


    private ChatClientManager() {
        mMessageListenerMap = new HashMap<>();
        mMessageSendStateChangeListenerMap = new HashMap<>();
        mOnContactMessageReceiveListenerList = Collections.synchronizedList(new LinkedList<OnContactMessageReceiveListener>());
        mOnConnectionStateChangeListenerList = Collections.synchronizedList(new LinkedList<onConnectionStateChangeListener>());
        mOnUnreadMessageCountChangeListeners = Collections.synchronizedList(new LinkedList<OnUnreadMessageCountChangeListener>());

        mContactDao = DBManager.getInstance().getContactDao();
        mRongIMClient = RongIMClient.getInstance();

        RongIMClient.setOnReceiveMessageListener(mOnReceiveMessageListener);
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);

        mWorkExecutor = new ThreadPoolExecutor(
                0,
                2,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(32));
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

    public void clearConversationUnreadStatus(Conversation conversation) {
        mRongIMClient.clearMessagesUnreadStatus(conversation.getConversationType(), conversation.getTargetId(), new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (!aBoolean) {
                    LogUtil.e("clearMessagesUnreadStatus error");
                } else {
                    updateChatUnreadCount();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }


    public List<Message> getHistoryMessages(Conversation conversation, int oldestMessageId, int count) {
        return mRongIMClient.getHistoryMessages(conversation.getConversationType(), conversation.getTargetId(), oldestMessageId, count);
    }

    public void asyncGetHistoryMessages(Conversation conversation, int oldestMessageId, int count, RongIMClient.ResultCallback<List<Message>> callback) {
        mRongIMClient.getHistoryMessages(conversation.getConversationType(), conversation.getTargetId(), oldestMessageId, count, callback);
    }

    public void asyncDeleteChatMessages(Conversation conversation,RongIMClient.ResultCallback<Boolean> callback){
        mRongIMClient.deleteMessages(conversation.getConversationType(), conversation.getTargetId(), callback);
    }

    public void sendMessage(Message message) {
        mRongIMClient.sendMessage(message, null, null, mSendMessageCallback);
    }

    public void asyncSetConversationTop(Conversation conversation, boolean isTop,RongIMClient.ResultCallback<Boolean> callback) {
        mRongIMClient.setConversationToTop(conversation.getConversationType(), conversation.getTargetId(), isTop,callback);
    }

    public void asyncRemoveConversation(Conversation conversation,RongIMClient.ResultCallback<Boolean> callback){
        mRongIMClient.removeConversation(conversation.getConversationType(), conversation.getTargetId(), callback);
    }

    public void saveMessageDraft(Conversation conversation,String draft){
        mRongIMClient.saveTextMessageDraft(conversation.getConversationType(), conversation.getTargetId(),draft);
    }


    public void updateChatUnreadCount() {
        mRongIMClient.getUnreadCount(new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                synchronized (mUpdateChatUnreadCountLock) {
                    if (mUnreadChatMessageCount != integer) {
                        mUnreadChatMessageCount = integer;
                        for (OnUnreadMessageCountChangeListener listener : mOnUnreadMessageCountChangeListeners) {
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

    public void updateContactUnreadCount() {
        mWorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mUpdateContactUnreadCountLock) {
                    ContactDao contactDao = DBManager.getInstance().getContactDao();
                    int count = contactDao.loadRemindCount();
                    if (count != mUnreadContactMessageCount) {
                        mUnreadContactMessageCount = count;
                        for (OnUnreadMessageCountChangeListener listener : mOnUnreadMessageCountChangeListeners) {
                            listener.onContactMessageUnreadCountChange(mUnreadChatMessageCount);
                        }
                    }
                }
            }
        });
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
                case "RC:ContactNtf":
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
            if (i == 0) {
                updateChatUnreadCount();
                updateContactUnreadCount();
            }
            return true;
        }
    };

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

    public void addUnreadMessageCountChangeListener(OnUnreadMessageCountChangeListener listener) {
        if (!mOnUnreadMessageCountChangeListeners.contains(listener)) {
            mOnUnreadMessageCountChangeListeners.add(listener);
        }
    }

    public void removeUnreadMessageCountChangeListener(OnUnreadMessageCountChangeListener listener) {
        mOnUnreadMessageCountChangeListeners.remove(listener);
    }

    public interface OnMessageSendStateChangeListener {

        void onSendProgress(Message message);

        void onSendSuccess(Message message);

        void onSendFail(Message message);
    }

    public interface OnChatMessageReceiveListener {
        void onChatMessageReceived(Message message, int untreatedCount);
    }

    public interface OnContactMessageReceiveListener {
        void onContactMessageReceive(ContactNotificationMessage message);
    }

    public interface OnUnreadMessageCountChangeListener {
        void onChatMessageUnreadCountChange(int count);

        void onContactMessageUnreadCountChange(int count);
    }

    public interface onConnectionStateChangeListener {

        void onConnected();

        void onDisconnected(String reason);

    }
}
