package com.yzx.chat.tool;


import android.text.TextUtils;

import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;

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

    private Map<MessageListener, String> mMessageListenerMap;
    private List<ContactListener> mContactListenerList;

    public static void init() {
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

        EMClient.getInstance().chatManager().addMessageListener(mEMMessageListener);
        EMClient.getInstance().contactManager().setContactListener(mEMContactListener);
    }

    public void addMessageListener(MessageListener listener, String conversationID) {
        if (!mMessageListenerMap.containsKey(listener)) {
            mMessageListenerMap.put(listener, conversationID);
        }
    }

    public void addContactListener(ContactListener listener) {
        if (!mContactListenerList.contains(listener)) {
            mContactListenerList.add(listener);
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

    }
}
