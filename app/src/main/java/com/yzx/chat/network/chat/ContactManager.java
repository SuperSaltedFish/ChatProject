package com.yzx.chat.network.chat;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.ContactOperationDao;
import com.yzx.chat.database.UserDao;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.contact.ContactApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.AsyncUtil;

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

    public static final String CONTACT_OPERATION_REQUEST = "Request";//对方请求
    public static final String CONTACT_OPERATION_ACCEPT = "Accept";//对方同意添加
    public static final String CONTACT_OPERATION_REFUSED = "Refused";//对方拒绝添加
    public static final String CONTACT_OPERATION_DELETE = "Delete";//对方删除好友

    public static final String CONTACT_OPERATION_REQUEST_ACTIVE = "ActiveRequest";//主动请求
    public static final String CONTACT_OPERATION_ACCEPT_ACTIVE = "ActiveAccept";//主动同意添加
    public static final String CONTACT_OPERATION_REFUSED_ACTIVE = "ActiveRefused";//主动拒绝添加

    private static final Set<String> CONTACT_OPERATION_SET =  new HashSet<>(Arrays.asList(CONTACT_OPERATION_REQUEST, CONTACT_OPERATION_ACCEPT, CONTACT_OPERATION_REFUSED, CONTACT_OPERATION_DELETE));

    private Map<String, ContactBean> mContactsMap;
    private IMClient.SubManagerCallback mSubManagerCallback;
    private List<OnContactChangeListener> mContactChangeListeners;
    private List<OnContactOperationListener> mContactOperationListeners;
    private List<OnContactOperationUnreadCountChangeListener> mContactOperationUnreadCountChangeListeners;
    private ContactOperationDao mContactOperationDao;
    private ContactDao mContactDao;
    private UserDao mUserDao;
    private Gson mGson;

    private ContactApi mContactApi;
    private NetworkExecutor mNetworkExecutor;
    private Call<JsonResponse<Void>> mRequestContact;
    private Call<JsonResponse<Void>> mRejectContact;
    private Call<JsonResponse<Void>> mAcceptContact;
    private Call<JsonResponse<Void>> mDeleteContact;
    private Call<JsonResponse<Void>> mUpdateContact;

    private volatile int mContactOperationUnreadNumber;
    private final Object mUpdateContactUnreadNumberLock = new Object();

    ContactManager(IMClient.SubManagerCallback subManagerCallback, AbstractDao.ReadWriteHelper readWriteHelper) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
        mContactOperationDao = new ContactOperationDao(readWriteHelper);
        mContactDao = new ContactDao(readWriteHelper);
        mUserDao = new UserDao(readWriteHelper);
        mContactChangeListeners = Collections.synchronizedList(new LinkedList<OnContactChangeListener>());
        mContactOperationListeners = Collections.synchronizedList(new LinkedList<OnContactOperationListener>());
        mContactOperationUnreadCountChangeListeners = Collections.synchronizedList(new LinkedList<OnContactOperationUnreadCountChangeListener>());
        mGson = new GsonBuilder().serializeNulls().create();
        mContactApi = (ContactApi) ApiHelper.getProxyInstance(ContactApi.class);
        mNetworkExecutor = NetworkExecutor.getInstance();
        mContactsMap = new HashMap<>(256);
        List<ContactBean> contacts = mContactDao.loadAllContacts();
        if (contacts != null) {
            for (ContactBean contact : contacts) {
                mContactsMap.put(contact.getUserProfile().getUserID(), contact);
            }
        }
    }


    public List<ContactBean> getAllContacts() {
        if (mContactsMap == null) {
            return null;
        }
        List<ContactBean> contacts = new ArrayList<>(mContactsMap.size() + 16);
        contacts.addAll(mContactsMap.values());
        return contacts;
    }

    public ContactBean getContact(String userID) {
        return mContactsMap.get(userID);
    }


    public void requestContact(final UserBean user, final String reason, @Nullable final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mRequestContact);
        mRequestContact = mContactApi.requestContact(user.getUserID(), reason);
        mRequestContact.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                ContactOperationBean operation = new ContactOperationBean();
                operation.setReason(reason);
                operation.setUserID(user.getUserID());
                operation.setTime((int) (System.currentTimeMillis() / 1000));
                operation.setType(ContactManager.CONTACT_OPERATION_REQUEST_ACTIVE);
                operation.setRemind(false);
                boolean success = mContactOperationDao.replace(operation) & mUserDao.replace(user);
                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }
                if (!success) {
                    LogUtil.e("requestContact:Failure of operating database");
                } else {
                    for (OnContactOperationListener listener : mContactOperationListeners) {
                        listener.onContactOperationUpdate(operation);
                    }
                }
            }

            @Override
            protected void onFailure(String message) {
                if (resultCallback != null) {
                    resultCallback.onFailure(message);
                }
            }
        }, false);
        mNetworkExecutor.submit(mRequestContact);
    }

    public void rejectContact(final ContactOperationBean contactOperation, final String reason, @Nullable final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mRejectContact);
        mRejectContact = mContactApi.rejectContact(contactOperation.getUser().getUserID(), reason);
        mRejectContact.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                contactOperation.setReason(reason);
                contactOperation.setTime((int) (System.currentTimeMillis() / 1000));
                contactOperation.setType(ContactManager.CONTACT_OPERATION_REFUSED_ACTIVE);
                contactOperation.setRemind(false);
                boolean success = mContactOperationDao.replace(contactOperation);
                if (!success) {
                    LogUtil.e("rejectContact:Failure of operating database");
                }
                for (OnContactOperationListener listener : mContactOperationListeners) {
                    listener.onContactOperationUpdate(contactOperation);
                }
                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }
            }

            @Override
            protected void onFailure(String message) {
                if (resultCallback != null) {
                    resultCallback.onFailure(message);
                }
            }
        }, false);
        mNetworkExecutor.submit(mRejectContact);
    }

    public void acceptContact(final ContactOperationBean contactOperation, @Nullable final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mAcceptContact);
        mAcceptContact = mContactApi.acceptContact(contactOperation.getUser().getUserID());
        mAcceptContact.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                contactOperation.setTime((int) (System.currentTimeMillis() / 1000));
                contactOperation.setType(ContactManager.CONTACT_OPERATION_ACCEPT_ACTIVE);
                contactOperation.setRemind(false);

                ContactBean contactBean = new ContactBean();
                contactBean.setUserProfile(contactOperation.getUser());

                boolean success = mContactOperationDao.replace(contactOperation) & mContactDao.insert(contactBean);
                for (OnContactOperationListener listener : mContactOperationListeners) {
                    listener.onContactOperationUpdate(contactOperation);
                }
                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }

                if (success) {
                    ContactBean newContact = mContactDao.getContact(contactOperation.getUserID());
                    if (newContact == null) {
                        LogUtil.e("Contact is null after acceptContact");
                    } else {
                        mContactsMap.put(newContact.getUserProfile().getUserID(), newContact);


                        for (OnContactChangeListener listener : mContactChangeListeners) {
                            listener.onContactAdded(newContact);
                        }
                    }
                } else {
                    LogUtil.e("acceptContact:Failure of operating database");
                }
            }

            @Override
            protected void onFailure(String message) {
                if (resultCallback != null) {
                    resultCallback.onFailure(message);
                }
            }
        }, false);
        mNetworkExecutor.submit(mAcceptContact);
    }

    public void deleteContact(final ContactBean contact, @Nullable final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mDeleteContact);
        mDeleteContact = mContactApi.deleteContact(contact.getUserProfile().getUserID());
        mDeleteContact.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                boolean success = mContactDao.delete(contact);
                if (!success) {
                    LogUtil.e("deleteContact:Failure of operating database");
                }

                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }
                if (success) {
                    mContactsMap.remove(contact.getUserProfile().getUserID());
                    for (OnContactChangeListener listener : mContactChangeListeners) {
                        listener.onContactDeleted(contact);
                    }

                }
            }

            @Override
            protected void onFailure(String message) {
                if (resultCallback != null) {
                    resultCallback.onFailure(message);
                }
            }
        }, false);
        mNetworkExecutor.submit(mDeleteContact);
    }

    public void updateContact(final ContactBean contact, @Nullable final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mUpdateContact);
        mUpdateContact = mContactApi.updateRemark(contact.getUserProfile().getUserID(), contact.getRemark());
        mUpdateContact.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                contact.getRemark().setUploadFlag(0);
                boolean success = mContactDao.update(contact) & mUserDao.update(contact.getUserProfile());
                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }

                if (success) {
                    mContactsMap.put(contact.getUserProfile().getUserID(), contact);
                    for (OnContactChangeListener listener : mContactChangeListeners) {
                        listener.onContactUpdate(contact);
                    }
                } else {
                    LogUtil.e("updateContact:Failure of operating database");
                }
            }

            @Override
            protected void onFailure(String message) {
                contact.getRemark().setUploadFlag(1);
                if (!mContactDao.update(contact) & mUserDao.update(contact.getUserProfile())) {
                    for (OnContactChangeListener listener : mContactChangeListeners) {
                        listener.onContactUpdate(contact);
                    }
                } else {
                    LogUtil.e("updateContact:Failure of operating database");
                }
                if (resultCallback != null) {
                    resultCallback.onFailure(message);
                }
            }
        }, false);
        mNetworkExecutor.submit(mUpdateContact);
    }

    public int getContactUnreadCount() {
        return mContactOperationUnreadNumber;
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

    public ContactOperationBean getContactOperation(String ContactID) {
        return mContactOperationDao.loadByKey(ContactID);
    }

    public List<ContactOperationBean> loadAllContactOperation() {
        return mContactOperationDao.loadAllContactOperation();
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

    void destroy() {
        if (mRequestContact != null) {
            mRequestContact.cancel();
            mRequestContact = null;
        }
        if (mRejectContact != null) {
            mRejectContact.cancel();
            mRejectContact = null;
        }
        if (mAcceptContact != null) {
            mAcceptContact.cancel();
            mAcceptContact = null;
        }
        if (mDeleteContact != null) {
            mDeleteContact.cancel();
            mDeleteContact = null;
        }
        if (mUpdateContact != null) {
            mUpdateContact.cancel();
            mUpdateContact = null;
        }

        mContactsMap.clear();
        mContactChangeListeners.clear();
        mContactOperationListeners.clear();
        mContactOperationUnreadCountChangeListeners.clear();
        mContactsMap.clear();

        mContactsMap = null;
        mContactChangeListeners = null;
        mContactOperationListeners = null;
        mContactOperationUnreadCountChangeListeners = null;
        mContactsMap = null;
    }

    void onReceiveContactNotificationMessage(Message message) {
        ContactNotificationMessage contactMessage = (ContactNotificationMessage) message.getContent();
        ContactOperationBean contactOperation = new ContactOperationBean();
        String operation = contactMessage.getOperation();
        if (!CONTACT_OPERATION_SET.contains(operation)) {
            LogUtil.e("unknown contact operation:" + operation);
            return;
        }
        if (TextUtils.isEmpty(contactMessage.getExtra())) {
            LogUtil.e("contact operation extra is empty");
            return;
        }
        LogUtil.d("ContactOperationExtra" + contactMessage.getExtra());

        ContactMessageExtra extra;
        try {
            extra = mGson.fromJson(contactMessage.getExtra(), ContactMessageExtra.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            LogUtil.e("fromJson ContactMessageExtra.class fail,json content:" + contactMessage.getExtra());
            return;
        }

        if (extra.userProfile != null) {
            if (!mUserDao.replace(extra.userProfile)) {
                LogUtil.e(" ContactOperation : replace user fail");
                return;
            }
        } else {
            LogUtil.e(" ContactOperation : userProfile is empty");
            return;
        }

        contactOperation.setUser(extra.userProfile);
        contactOperation.setUserID(contactMessage.getSourceUserId());
        contactOperation.setReason(contactMessage.getMessage());
        contactOperation.setRemind(true);
        contactOperation.setTime((int) (message.getReceivedTime() / 1000));
        contactOperation.setType(operation);
        if (mContactOperationDao.isExist(contactOperation)) {
            if (!mContactOperationDao.update(contactOperation)) {
                LogUtil.e("update contact operation fail");
                return;
            }
            for (OnContactOperationListener contactListener : mContactOperationListeners) {
                contactListener.onContactOperationUpdate(contactOperation);
            }
        } else {
            if (!mContactOperationDao.insert(contactOperation)) {
                LogUtil.e("insert contact operation fail");
                return;
            }
            for (OnContactOperationListener contactListener : mContactOperationListeners) {
                contactListener.onContactOperationReceive(contactOperation);
            }
        }
        updateContactUnreadCount();
    }


     static boolean update(ArrayList<ContactBean> contacts, AbstractDao.ReadWriteHelper readWriteHelper){
        ContactDao contactDao = new ContactDao(readWriteHelper);
        contactDao.cleanTable();
        if (contactDao.insertAllContacts(contacts)) {
            return true;
        }else {
            LogUtil.e("updateAllContacts fail");
            return false;
        }
    }


    public interface OnContactOperationListener {
        void onContactOperationReceive(ContactOperationBean message);

        void onContactOperationUpdate(ContactOperationBean message);

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

    private static final class ContactMessageExtra {
        String sourceUserNickname;
        long version;
        UserBean userProfile;
    }

}
