package com.yzx.chat.tool;


import android.content.Context;
import android.text.TextUtils;

import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.DBHelper;

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
    private Map<MessageListener, String> mMessageListenerMap;
    private List<ContactListener> mContactListenerList;
    private List<UnreadCountChangeListener> mUnreadCountChangeListenerList;

    private ContactDao mContactDao;

    private volatile int mMessageUnreadCount = -1;
    private volatile int mContactUnreadCount = -1;

    private ChatClientManager() {
        mMessageListenerMap = new HashMap<>();
        mContactListenerList = new LinkedList<>();
        mUnreadCountChangeListenerList = new LinkedList<>();
        mContactDao = DBManager.getInstance().getContactDao();
        mEMClient = EMClient.getInstance();
        mEMClient.chatManager().addMessageListener(mEMMessageListener);
        mEMClient.contactManager().setContactListener(mEMContactListener);
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

    public synchronized void setMessageUnreadCount(int messageUnreadCount) {
        if (mMessageUnreadCount != messageUnreadCount) {
            for (UnreadCountChangeListener listener : mUnreadCountChangeListenerList) {
                listener.onMessageUnreadCountChange(messageUnreadCount);
            }
        }
        mMessageUnreadCount = messageUnreadCount;
    }

    public synchronized void setContactUnreadCount(int contactUnreadCount) {
        if (mContactUnreadCount != contactUnreadCount) {
            for (UnreadCountChangeListener listener : mUnreadCountChangeListenerList) {
                listener.onContactUnreadCountChange(contactUnreadCount);
            }
        }
        mContactUnreadCount = contactUnreadCount;
    }

    public synchronized void makeAllContactAsRead() {
        mContactDao.makeAllRemindAsNoRemind(IdentityManager.getInstance().getUserID());
        setContactUnreadCount(0);
    }

    public void loadAllConversationsAndGroups() {
        mEMClient.chatManager().loadAllConversations();
        mEMClient.groupManager().loadAllGroups();
    }

    public Map<String, EMConversation> getAllConversations() {
        return mEMClient.chatManager().getAllConversations();
    }

    public List<EMMessage> loadMoreMessage(String conversationID, String startMessageID, int count) {
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
            ContactBean bean = mContactDao.loadByKey(IdentityManager.getInstance().getUserID(), username);
            if (bean != null) {
                if (!bean.isRemind()) {
                    bean.setType(ContactBean.CONTACT_TYPE_INVITED);
                    bean.setRemind(true);
                    mContactDao.update(bean);
                } else return;
            } else {
                bean = new ContactBean();
                bean.setUserTo(IdentityManager.getInstance().getUserID());
                bean.setUserFrom(username);
                bean.setType(ContactBean.CONTACT_TYPE_INVITED);
                bean.setReason(reason);
                bean.setRemind(true);
                mContactDao.insert(bean);
            }
            setContactUnreadCount(mContactUnreadCount < 0 ? 1 : mContactUnreadCount + 1);
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
        void onMessageUnreadCountChange(int unreadCount);

        void onContactUnreadCountChange(int unreadCount);
    }
}
