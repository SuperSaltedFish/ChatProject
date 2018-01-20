package com.yzx.chat.network.chat;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactMessageBean;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.ContactMessageDao;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by YZX on 2017年12月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactManager {

    private Map<String, ContactBean> mContactsMap;
    private IMClient.SubManagerCallback mSubManagerCallback;
    private List<OnContactMessageListener> mContactMessageListeners;
    private List<OnContactMessageUnreadCountChangeListener> mContactMessageUnreadCountChangeListeners;
    private ContactMessageDao mContactMessageDao;
    private ContactDao mContactDao;

    private int mContactMessageUnreadNumber;
    private final Object mUpdateContactUnreadNumberLock = new Object();

    public ContactManager(IMClient.SubManagerCallback subManagerCallback) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
        mContactsMap = new HashMap<>(256);
        mContactMessageDao = DBManager.getInstance().getContactMessageDao();
        mContactDao = DBManager.getInstance().getContactDao();
        mContactMessageListeners = Collections.synchronizedList(new LinkedList<OnContactMessageListener>());
        mContactMessageUnreadCountChangeListeners = Collections.synchronizedList(new LinkedList<OnContactMessageUnreadCountChangeListener>());
    }

    public void loadAllContact(String userID) {
        List<ContactBean> contacts = mContactDao.loadAllContacts(userID);
        for (ContactBean contact : contacts) {
            mContactsMap.put(contact.getUserID(), contact);
        }
    }

    public ContactBean getContact(String userID) {
        return mContactsMap.get(userID);
    }

    public boolean updateContact(ContactBean contact) {
        boolean result = mContactDao.update(contact);
        if (result) {
            mContactsMap.put(contact.getUserID(), contact);
        } else {
            LogUtil.e("update contact fail");
        }
        return result;
    }

    public boolean addContact(ContactBean contact) {
        boolean result = mContactDao.insert(contact);
        if (result) {
            mContactsMap.put(contact.getUserID(), contact);
        } else {
            LogUtil.e("update contact fail");
        }
        return result;
    }

    public boolean delectContact(ContactBean contact) {
        boolean result = mContactDao.delete(contact);
        if (result) {
            mContactsMap.remove(contact.getUserID());
        } else {
            LogUtil.e("update contact fail");
        }
        return result;
    }

    public List<ContactBean> getAllContacts() {
        List<ContactBean> contacts = new ArrayList<>(mContactsMap.size() + 16);
        contacts.addAll(mContactsMap.values());
        return contacts;
    }

    public void updateContactUnreadCount() {
        mSubManagerCallback.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mUpdateContactUnreadNumberLock) {
                    ContactMessageDao contactMessageDao = DBManager.getInstance().getContactMessageDao();
                    int count = contactMessageDao.loadRemindCount(IdentityManager.getInstance().getUserID());
                    if (count != mContactMessageUnreadNumber) {
                        mContactMessageUnreadNumber = count;
                        for (OnContactMessageUnreadCountChangeListener listener : mContactMessageUnreadCountChangeListeners) {
                            listener.onContactMessageUnreadCountChange(mContactMessageUnreadNumber);
                        }
                    }
                }
            }
        });
    }

    public void removeContactMessageAsync(final ContactMessageBean contactMessage) {
        mSubManagerCallback.execute(new Runnable() {
            @Override
            public void run() {
                if (mContactMessageDao.delete(contactMessage)) {
                    for (OnContactMessageListener contactListener : mContactMessageListeners) {
                        contactListener.onContactMessageDelete(contactMessage);
                    }
                } else {
                    LogUtil.e("delete ContactMessageFail from DB");
                }
            }
        });
    }

    public void removeContactMessage(final ContactMessageBean contactMessage) {
        if (mContactMessageDao.delete(contactMessage)) {
            for (OnContactMessageListener contactListener : mContactMessageListeners) {
                contactListener.onContactMessageDelete(contactMessage);
            }
        } else {
            LogUtil.e("delete ContactMessageFail from DB");
        }
    }

    public List<ContactMessageBean> loadMoreContactMessage(String userID, int startID, int count) {
        return mContactMessageDao.loadMoreContactMessage(userID, startID, count);
    }

    public void addContactMessageListener(OnContactMessageListener listener) {
        if (!mContactMessageListeners.contains(listener)) {
            mContactMessageListeners.add(listener);
        }
    }

    public void removeContactMessageListener(OnContactMessageListener listener) {
        mContactMessageListeners.remove(listener);
    }

    public void addContactMessageUnreadCountChangeListener(OnContactMessageUnreadCountChangeListener listener) {
        if (!mContactMessageUnreadCountChangeListeners.contains(listener)) {
            mContactMessageUnreadCountChangeListeners.add(listener);
        }
    }

    public void removeContactMessageUnreadCountChangeListener(OnContactMessageUnreadCountChangeListener listener) {
        mContactMessageUnreadCountChangeListeners.remove(listener);
    }

    void onReceiveContactNotificationMessage(Message message) {
        ContactNotificationMessage contactMessage = (ContactNotificationMessage) message.getContent();
        ContactMessageBean bean = new ContactMessageBean();
        bean.setUserTo(contactMessage.getTargetUserId());
        bean.setUserFrom(contactMessage.getSourceUserId());
        bean.setReason(contactMessage.getMessage());
        bean.setRemind(true);
        bean.setTime((int) (message.getReceivedTime() / 1000));
        switch (contactMessage.getOperation()) {
//            case ContactNotificationMessage.CONTACT_OPERATION_REQUEST:
//                 bean.setType(ContactMessageBean.TYPE_REQUESTING);
//                break;
//            case ContactNotificationMessage.CONTACT_OPERATION_ACCEPT_RESPONSE:
//                   bean.setType(ContactMessageBean.CONTACT_TYPE_ACCEPTED);
//                break;
//            case ContactNotificationMessage.CONTACT_OPERATION_REJECT_RESPONSE:
//                //   bean.setType(ContactMessageBean.CONTACT_TYPE_DECLINED);
//                break;
        }
        mContactMessageDao.delete(bean);
        mContactMessageDao.insert(bean);
        for (OnContactMessageListener contactListener : mContactMessageListeners) {
            contactListener.onContactMessageReceive(bean);
        }
    }


    public interface OnContactMessageListener {
        void onContactMessageReceive(ContactMessageBean message);

        void onContactMessageDelete(ContactMessageBean message);
    }

    public interface OnContactMessageUnreadCountChangeListener {
        void onContactMessageUnreadCountChange(int count);
    }

}
