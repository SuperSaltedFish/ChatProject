package com.yzx.chat.network.chat;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.ContactOperationDao;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by YZX on 2017年12月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactManager {

    public static final String CONTACT_OPERATION_REQUEST = "Request";//被请求
    public static final String CONTACT_OPERATION_DISAGREE = "Disagree";//被拒绝
    public static final String CONTACT_OPERATION_ACCEPT = "AcceptResponse";//同意添加
    public static final String CONTACT_OPERATION_REFUSED = "RefusedResponse";//拒绝添加
    public static final String CONTACT_OPERATION_ADDED = "Added";//已经添加
    public static final String CONTACT_OPERATION_VERIFYING = "VERIFYING";//等待验证

    private Set<String> mContactOperationSet;
    private Map<String, ContactBean> mContactsMap;
    private IMClient.SubManagerCallback mSubManagerCallback;
    private List<OnContactChangeListener> mContactChangeListeners;
    private List<OnContactOperationListener> mContactOperationListeners;
    private List<OnContactOperationUnreadCountChangeListener> mContactOperationUnreadCountChangeListeners;
    private ContactOperationDao mContactOperationDao;
    private ContactDao mContactDao;
    private Gson mGson;

    private int mContactOperationUnreadNumber;
    private final Object mUpdateContactUnreadNumberLock = new Object();

    public ContactManager(IMClient.SubManagerCallback subManagerCallback) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
        mContactOperationSet = new HashSet<>(Arrays.asList(CONTACT_OPERATION_REQUEST, CONTACT_OPERATION_DISAGREE, CONTACT_OPERATION_ACCEPT, CONTACT_OPERATION_REFUSED, CONTACT_OPERATION_ADDED, CONTACT_OPERATION_VERIFYING));
        mContactsMap = new HashMap<>(256);
        mContactOperationDao = DBManager.getInstance().getContactOperationDao();
        mContactDao = DBManager.getInstance().getContactDao();
        mContactChangeListeners = Collections.synchronizedList(new LinkedList<OnContactChangeListener>());
        mContactOperationListeners = Collections.synchronizedList(new LinkedList<OnContactOperationListener>());
        mContactOperationUnreadCountChangeListeners = Collections.synchronizedList(new LinkedList<OnContactOperationUnreadCountChangeListener>());
        mGson = new GsonBuilder().serializeNulls().create();
    }

    public void initContacts(List<ContactBean> contactList) {
        if (!mContactDao.replaceAll(contactList)) {
            LogUtil.e("initContacts fail");
        }
        initContactsFromDB();
    }

    public void initContactsFromDB() {
        List<ContactBean> contacts = mContactDao.loadAllContacts();
        if (contacts != null) {
            for (ContactBean contact : contacts) {
                mContactsMap.put(contact.getUserProfile().getUserID(), contact);
            }
        }
    }

    public ContactBean getContact(String userID) {
        return mContactsMap.get(userID);
    }

    public boolean updateContact(ContactBean contact) {
        return updateContact(contact, true);
    }

    public boolean updateContact(ContactBean contact, boolean isCallListener) {
        boolean result = mContactDao.update(contact);
        if (result) {
            mContactsMap.put(contact.getUserProfile().getUserID(), contact);
            if (isCallListener) {
                for (OnContactChangeListener listener : mContactChangeListeners) {
                    listener.onContactUpdate(contact);
                }
            }
        } else {
            LogUtil.e("update contact fail");
        }
        return result;
    }

    public boolean addContact(ContactBean contact) {
        return addContact(contact, true);
    }

    public boolean addContact(ContactBean contact, boolean isCallListener) {
        boolean result = mContactDao.insert(contact);
        if (result) {
            mContactsMap.put(contact.getUserProfile().getUserID(), contact);
            if (isCallListener) {
                for (OnContactChangeListener listener : mContactChangeListeners) {
                    listener.onContactAdded(contact);
                }
            }
        } else {
            LogUtil.e("update contact fail");
        }
        return result;
    }

    public boolean deleteContact(ContactBean contact) {
        return deleteContact(contact, true);
    }

    public boolean deleteContact(ContactBean contact, boolean isCallListener) {
        boolean result = mContactDao.delete(contact);
        if (result) {
            mContactsMap.remove(contact.getUserProfile().getUserID());
            if (isCallListener) {
                for (OnContactChangeListener listener : mContactChangeListeners) {
                    listener.onContactDeleted(contact);
                }
            }
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
                    int count = mContactOperationDao.loadRemindCount();
                    if (count != mContactOperationUnreadNumber) {
                        mContactOperationUnreadNumber = count;
                        for (OnContactOperationUnreadCountChangeListener listener : mContactOperationUnreadCountChangeListeners) {
                            listener.onContactOperationUnreadCountChange(mContactOperationUnreadNumber);
                        }
                    }
                }
            }
        });
    }

    public void makeAllContactOperationAsRead() {
        mSubManagerCallback.execute(new Runnable() {
            @Override
            public void run() {
                mContactOperationDao.makeAllRemindAsRemind();
                if (mContactOperationUnreadNumber != 0) {
                    updateContactUnreadCount();
                }
            }
        });
    }

    public void replaceContactOperationAsync(final ContactOperationBean contactMessage) {
        mSubManagerCallback.execute(new Runnable() {
            @Override
            public void run() {
                if (mContactOperationDao.replace(contactMessage)) {
                    updateContactUnreadCount();
                } else {
                    LogUtil.e("delete ContactMessageFail from DB");
                }
            }
        });
    }

    public void removeContactOperationAsync(final ContactOperationBean contactMessage) {
        mSubManagerCallback.execute(new Runnable() {
            @Override
            public void run() {
                if (mContactOperationDao.delete(contactMessage)) {
                    for (OnContactOperationListener contactListener : mContactOperationListeners) {
                        contactListener.onContactOperationDelete(contactMessage);
                    }
                    updateContactUnreadCount();
                } else {
                    LogUtil.e("delete ContactMessageFail from DB");
                }
            }
        });
    }


    public List<ContactOperationBean> loadAllContactOperation() {
        return mContactOperationDao.loadAllContactOperation();
    }

    public List<ContactOperationBean> loadMoreContactOperation(int startID, int count) {
        return mContactOperationDao.loadMoreContactOperation(startID, count);
    }

    public void addContactOperationListener(OnContactOperationListener listener) {
        if (!mContactOperationListeners.contains(listener)) {
            mContactOperationListeners.add(listener);
        }
    }

    public void removeContactOperationListener(OnContactOperationListener listener) {
        mContactOperationListeners.remove(listener);
    }

    public void addContactOperationUnreadCountChangeListener(OnContactOperationUnreadCountChangeListener listener) {
        if (!mContactOperationUnreadCountChangeListeners.contains(listener)) {
            mContactOperationUnreadCountChangeListeners.add(listener);
        }
    }

    public void removeContactOperationUnreadCountChangeListener(OnContactOperationUnreadCountChangeListener listener) {
        mContactOperationUnreadCountChangeListeners.remove(listener);
    }

    public void addContactChangeListener(OnContactChangeListener listener) {
        if (!mContactChangeListeners.contains(listener)) {
            mContactChangeListeners.add(listener);
        }
    }

    public void removeContactChangeListener(OnContactChangeListener listener) {
        mContactChangeListeners.remove(listener);
    }

    void onReceiveContactNotificationMessage(Message message) {
        ContactNotificationMessage contactMessage = (ContactNotificationMessage) message.getContent();
        ContactOperationBean bean = new ContactOperationBean();
        String operation = contactMessage.getOperation();
        if (!mContactOperationSet.contains(operation)) {
            LogUtil.e("unknown contact operation:" + operation);
            return;
        }
        if (TextUtils.isEmpty(contactMessage.getExtra())) {
            LogUtil.e("contact operation extra is empty");
            return;
        }
        UserBean user = mGson.fromJson(contactMessage.getExtra(), UserBean.class);
        if (TextUtils.isEmpty(user.getNickname())) {
            LogUtil.e("Nickname is empty");
            return;
        }
        bean.setUser(user);
        bean.setUserID(contactMessage.getSourceUserId());
        bean.setRemind(true);
        bean.setTime((int) (message.getReceivedTime() / 1000));
        bean.setType(operation);
        if (!mContactOperationDao.replace(bean)) {
            LogUtil.e("replace contact operation fail");
            return;
        }
        for (OnContactOperationListener contactListener : mContactOperationListeners) {
            contactListener.onContactOperationReceive(bean);
        }
        updateContactUnreadCount();
    }


    public interface OnContactOperationListener {
        void onContactOperationReceive(ContactOperationBean message);

        void onContactOperationDelete(ContactOperationBean message);
    }

    public interface OnContactOperationUnreadCountChangeListener {
        void onContactOperationUnreadCountChange(int count);
    }

    public interface OnContactChangeListener {
        void onContactAdded(ContactBean contact);

        void onContactDeleted(ContactBean contact);

        void onContactUpdate(ContactBean contact);
    }

}
