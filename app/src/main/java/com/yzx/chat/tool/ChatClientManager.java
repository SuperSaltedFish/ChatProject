package com.yzx.chat.tool;


import android.content.Context;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.exceptions.HyphenateException;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.util.LogUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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

    public static void init(Context context) {
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);
        EMClient.getInstance().init(context.getApplicationContext(), options);
        EMClient.getInstance().setDebugMode(true);
        sManager = new ChatClientManager();
    }

    public static ChatClientManager getInstance() {
        if (sManager == null) {
            throw new RuntimeException("ChatClientManager is not initialized");
        }
        return sManager;
    }


    private EMClient mEMClient;
    private Map<OnMessageReceiveListener, String> mMessageListenerMap;
    private Map<OnMessageSendStateChangeListener, String> mMessageSendStateChangeListenerMap;
    private List<ContactListener> mContactListenerList;
    private List<UnreadCountChangeListener> mUnreadCountChangeListenerList;
    private List<ConnectionListener> mConnectionListenerList;

    private ContactDao mContactDao;

    private volatile int mMessageUnreadCount;
    private volatile int mContactUnreadCount;

    private ChatClientManager() {
        mMessageListenerMap = new HashMap<>();
        mMessageSendStateChangeListenerMap = new HashMap<>();
        mContactListenerList = new LinkedList<>();
        mUnreadCountChangeListenerList = new LinkedList<>();
        mConnectionListenerList= new LinkedList<>();

        mContactDao = DBManager.getInstance().getContactDao();
        mEMClient = EMClient.getInstance();

        mEMClient.addConnectionListener(mEMConnectionListener);
        mEMClient.chatManager().addMessageListener(mEMMessageListener);
        mEMClient.contactManager().setContactListener(mEMContactListener);
    }

    public void addOnMessageReceiveListener(OnMessageReceiveListener listener, String conversationID) {
        if (!mMessageListenerMap.containsKey(listener)) {
            mMessageListenerMap.put(listener, conversationID);
        }
    }

    public void removeOnMessageReceiveListener(OnMessageReceiveListener listener) {
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


    public void addOnMessageSendStateChangeListener(OnMessageSendStateChangeListener listener, String conversationID) {
        if (!mMessageSendStateChangeListenerMap.containsKey(listener)) {
            mMessageSendStateChangeListenerMap.put(listener, conversationID);
        }
    }

    public void removeOnMessageSendStateChangeListener(OnMessageSendStateChangeListener listener) {
        mMessageSendStateChangeListenerMap.remove(listener);
    }

    public void addConnectionListener(ConnectionListener listener) {
        if (!mConnectionListenerList.contains(listener)) {
            mConnectionListenerList.add(listener);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        mConnectionListenerList.remove(listener);
    }

    public void addUnreadCountChangeListener(UnreadCountChangeListener listener) {
        if (!mUnreadCountChangeListenerList.contains(listener)) {
            mUnreadCountChangeListenerList.add(listener);
        }
    }

    public void removeUnreadCountChangeListener(UnreadCountChangeListener listener) {
        mUnreadCountChangeListenerList.remove(listener);
    }

    public synchronized void setMessageUnreadCount(int messageUnreadCount) {
        if (mMessageUnreadCount != messageUnreadCount) {
            for (UnreadCountChangeListener listener : mUnreadCountChangeListenerList) {
                listener.onMessageUnreadCountChange(messageUnreadCount);
            }
        }
        mMessageUnreadCount = messageUnreadCount;
    }

    public synchronized void updateContactUnreadCount() {
        int contactUnreadCount = mContactDao.loadRemindCount();
        if (mContactUnreadCount != contactUnreadCount) {
            for (UnreadCountChangeListener listener : mUnreadCountChangeListenerList) {
                listener.onContactUnreadCountChange(contactUnreadCount);
            }
        }
        mContactUnreadCount = contactUnreadCount;
    }

    public synchronized void makeAllContactAsRead() {
        mContactDao.makeAllRemindAsNoRemind(IdentityManager.getInstance().getUserID());
        updateContactUnreadCount();
    }

    public int getMessageUnreadCount() {
        return mMessageUnreadCount;
    }

    public int getContactUnreadCount() {
        return mContactUnreadCount;
    }

    public void loadAllConversationsAndGroups() {
        mEMClient.chatManager().loadAllConversations();
        mEMClient.groupManager().loadAllGroups();
    }

    public EMConversation getSingleConversation(String conversationID) {
        return mEMClient.chatManager().getConversation(conversationID);
    }

    public Map<String, EMConversation> getAllConversations() {
        return mEMClient.chatManager().getAllConversations();
    }

    public void sendMessage(EMMessage message) {
        message.setMessageStatusCallback(new MessageSendCallBack(message));
        mEMClient.chatManager().sendMessage(message);
    }

    public EMMessage resendMessage(String messageID){
       EMMessage message =  mEMClient.chatManager().getMessage(messageID);
       if(message!=null){
           message.setStatus(EMMessage.Status.INPROGRESS);
           sendMessage(message);
       }
       return message;
    }


    public List<EMMessage> loadMoreMessage(String conversationID, String startMessageID, int count) {
        EMConversation conversation = mEMClient.chatManager().getConversation(conversationID);
        if (conversation == null) {
            return null;
        } else {
            return conversation.loadMoreMsgFromDB(startMessageID, count);
        }
    }


    public void requestAddContact(String contactID, String reason) throws HyphenateException {
        mEMClient.contactManager().addContact(contactID, reason);
    }

    private final EMConnectionListener mEMConnectionListener = new EMConnectionListener() {
        @Override
        public void onConnected() {
            for(ConnectionListener listener:mConnectionListenerList){
                listener.onConnected();
            }
        }

        @Override
        public void onDisconnected(int errorCode) {
            for(ConnectionListener listener:mConnectionListenerList){
                listener.onDisconnected(errorCode);
            }
        }
    };


    private final EMMessageListener mEMMessageListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            String conversationID;
            OnMessageReceiveListener listener;
            List<EMMessage> filterMessage;
            for (Map.Entry<OnMessageReceiveListener, String> entry : mMessageListenerMap.entrySet()) {
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
            ContactBean bean = new ContactBean();
            bean.setUserTo(IdentityManager.getInstance().getUserID());
            bean.setUserFrom(username);
            bean.setType(ContactBean.CONTACT_TYPE_INVITED);
            bean.setReason(reason);
            bean.setRemind(true);
            bean.setTime((int) (System.currentTimeMillis() / 1000));
            mContactDao.replace(bean);
            updateContactUnreadCount();
        }

        @Override
        public void onFriendRequestAccepted(String username) {

        }

        @Override
        public void onFriendRequestDeclined(String username) {

        }
    };

    private class MessageSendCallBack implements EMCallBack {

        private EMMessage mEMMessage;

        MessageSendCallBack(EMMessage message) {
            mEMMessage = message;
        }

        @Override
        public void onSuccess() {
            String conversationID = mEMMessage.conversationId();
            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
                    entry.getKey().onSendSuccess(mEMMessage);
                }
            }
        }

        @Override
        public void onError(int code, String error) {
            LogUtil.e(error);
            String conversationID = mEMMessage.conversationId();
            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
                    entry.getKey().onSendFail(mEMMessage);
                }
            }
        }

        @Override
        public void onProgress(int progress, String status) {
            String conversationID = mEMMessage.conversationId();
            for (Map.Entry<OnMessageSendStateChangeListener, String> entry : mMessageSendStateChangeListenerMap.entrySet()) {
                if (conversationID.equals(entry.getValue()) || entry.getValue() == null) {
                    entry.getKey().onSendProgress(mEMMessage, progress);
                }
            }
        }
    }

    public interface OnMessageSendStateChangeListener {

        void onSendProgress(EMMessage message, int progress);

        void onSendSuccess(EMMessage message);

        void onSendFail(EMMessage message);
    }


    public interface OnMessageReceiveListener {
        void onMessageReceived(List<EMMessage> messages);
    }

    public interface ContactListener {
        void onContactInvited(String userID, String reason);
    }

    public interface UnreadCountChangeListener {
        void onMessageUnreadCountChange(int unreadCount);

        void onContactUnreadCountChange(int unreadCount);
    }

    public interface ConnectionListener {

        void onConnected();

        void onDisconnected(int errorCode);

    }
}
