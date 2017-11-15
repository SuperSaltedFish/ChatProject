package com.yzx.chat.tool;


import android.content.Context;
import android.text.TextUtils;

import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatClientManager {

    private static ChatClientManager sManager;

    private EMClient mEMClient;
    private Map<MessageListener, String> mMessageListenerMap;
    private List<ContactListener> mContactListenerList;
    private List<UnreadCountChangeListener> mUnreadCountChangeListenerList;

    private volatile int mMessageUnreadCount =-1;

    public static void init(Context context) {
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);
        EMClient.getInstance().init(context.getApplicationContext(), options);
        EMClient.getInstance().setDebugMode(false);
        sManager = new ChatClientManager();
    }

    public static ChatClientManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("ChatClientManager is not initialized");
        }
        return sManager;
    }

    private ChatClientManager() {
        mMessageListenerMap = new HashMap<>();
        mContactListenerList = new LinkedList<>();
        mUnreadCountChangeListenerList = new LinkedList<>();
        mEMClient = EMClient.getInstance();
        mEMClient.chatManager().addMessageListener(mEMMessageListener);
        mEMClient.contactManager().setContactListener(mEMContactListener);
    }

    public void markMessageAsRead(EMMessage message) {
        message.setUnread(false);
    }

    public void markConversationsAsRead(String conversationID) {
        mEMClient.chatManager().getConversation(conversationID);
    }

    public void markAllConversationsAsRead() {

    }

    public void addMessageListener(MessageListener listener, String conversationID) {
        if (!mMessageListenerMap.containsKey(listener)) {
            mMessageListenerMap.put(listener, conversationID);
        }
    }

    public void removeMessageListener(MessageListener listener) {
        mMessageListenerMap.remove(listener);
    }

    public void addContactListener(ContactListener listener) {
        if (!mContactListenerList.contains(listener)) {
            mContactListenerList.add(listener);
        }
    }

    public void removeContactListener(ContactListener listener) {
        mContactListenerList.remove(listener);
    }

    public void addUnreadCountChangeListener(UnreadCountChangeListener listener) {
        if (!mUnreadCountChangeListenerList.contains(listener)) {
            mUnreadCountChangeListenerList.add(listener);
        }
    }

    public void removeUnreadCountChangeListener(UnreadCountChangeListener listener) {
        mUnreadCountChangeListenerList.remove(listener);
    }

    public synchronized int getMessageUnreadCount() {
        return mMessageUnreadCount;
    }

    public synchronized void setMessageUnreadCount(int messageUnreadCount) {
        if (mMessageUnreadCount != messageUnreadCount) {
            for (UnreadCountChangeListener listener : mUnreadCountChangeListenerList) {
                listener.onUnreadCountChange(messageUnreadCount);
            }
        }
        mMessageUnreadCount = messageUnreadCount;
    }

    public void loadAllConversationsAndGroups() {
        mEMClient.chatManager().loadAllConversations();
        mEMClient.groupManager().loadAllGroups();
    }

    public Map<String, EMConversation> getAllConversations() {
        return mEMClient.chatManager().getAllConversations();
    }

    public List<EMMessage> loadMoreMssage(String conversationID, String startMessageID, int count) {
        EMConversation conversation = mEMClient.chatManager().getConversation(conversationID);
        if (conversation == null) {
            return null;
        } else {
            return conversation.loadMoreMsgFromDB(startMessageID, count);
        }
    }


    private final EMMessageListener mEMMessageListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            String conversationID;
            MessageListener listener;
            List<EMMessage> filterMessage;
            for (Map.Entry<MessageListener, String> entry : mMessageListenerMap.entrySet()) {
                conversationID = entry.getValue();
                listener = entry.getKey();
                if (listener == null) {
                    return;
                }
                if (TextUtils.isEmpty(conversationID)) {
                    listener.onMessageReceived(messages);
                } else {
                    filterMessage = new LinkedList<>();
                    for (EMMessage message : messages) {
                        if (conversationID.equals(message.conversationId())) {
                            filterMessage.add(message);
                        }
                    }
                    if (filterMessage.size() != 0) {
                        listener.onMessageReceived(filterMessage);
                    }
                }
            }
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {

        }

        @Override
        public void onMessageRead(List<EMMessage> messages) {

        }

        @Override
        public void onMessageDelivered(List<EMMessage> messages) {

        }

        @Override
        public void onMessageRecalled(List<EMMessage> messages) {

        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {

        }
    };

    private final EMContactListener mEMContactListener = new EMContactListener() {
        @Override
        public void onContactAdded(String username) {

        }

        @Override
        public void onContactDeleted(String username) {

        }

        @Override
        public void onContactInvited(String username, String reason) {
            for (ContactListener listener : mContactListenerList) {
                listener.onContactInvited(username, reason);
            }
        }

        @Override
        public void onFriendRequestAccepted(String username) {

        }

        @Override
        public void onFriendRequestDeclined(String username) {

        }
    };


    public interface MessageListener {
        void onMessageReceived(List<EMMessage> messages);
    }

    public interface ContactListener {
        void onContactInvited(String userID, String reason);
    }

    public interface UnreadCountChangeListener {
        void onUnreadCountChange(int unreadCount);
    }
}
