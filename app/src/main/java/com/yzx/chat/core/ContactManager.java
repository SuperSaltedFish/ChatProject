package com.yzx.chat.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.core.database.AbstractDao;
import com.yzx.chat.core.database.ContactDao;
import com.yzx.chat.core.database.ContactOperationDao;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.core.entity.TagEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.extra.ContactNotificationMessageEx;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.core.net.api.ContactApi;
import com.yzx.chat.core.util.CallbackUtil;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.core.util.ResourcesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by YZX on 2017年12月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactManager {

    public static final String CONTACT_OPERATION_REQUEST = "Request";//对方请求添加
    public static final String CONTACT_OPERATION_ACCEPT = "Accept";//对方同意添加
    public static final String CONTACT_OPERATION_REJECT = "Reject";//对方拒绝添加
    public static final String CONTACT_OPERATION_DELETE = "Delete";//对方删除好友

    public static final String CONTACT_OPERATION_REQUEST_ACTIVE = "ActiveRequest";//主动请求添加
    public static final String CONTACT_OPERATION_ACCEPT_ACTIVE = "ActiveAccept";//主动同意添加
    public static final String CONTACT_OPERATION_REJECT_ACTIVE = "ActiveReject";//主动拒绝添加
    public static final String CONTACT_OPERATION_DELETE_ACTIVE = "ActiveDelete";//主动删除好友

    private AppClient mAppClient;
    private RongIMClient mRongIMClient;
    private Handler mUIHandler;

    private Map<String, ContactEntity> mContactsMap;
    private Set<String> mContactTags;
    private List<OnContactChangeListener> mContactChangeListeners;
    private List<OnContactOperationListener> mContactOperationListeners;
    private List<OnContactOperationUnreadCountChangeListener> mContactOperationUnreadCountChangeListeners;
    private List<OnContactTagChangeListener> mContactTagChangeListeners;
    private ContactOperationDao mContactOperationDao;
    private ContactDao mContactDao;


    private ContactApi mContactApi;

    private volatile int mContactOperationUnreadNumber;

    ContactManager(AppClient appClient) {
        mAppClient = appClient;
        mRongIMClient = mAppClient.getRongIMClient();
        mUIHandler = new Handler(Looper.getMainLooper());
        mContactsMap = new HashMap<>(256);

        mContactChangeListeners = Collections.synchronizedList(new LinkedList<OnContactChangeListener>());
        mContactOperationListeners = Collections.synchronizedList(new LinkedList<OnContactOperationListener>());
        mContactOperationUnreadCountChangeListeners = Collections.synchronizedList(new LinkedList<OnContactOperationUnreadCountChangeListener>());
        mContactTagChangeListeners = Collections.synchronizedList(new LinkedList<OnContactTagChangeListener>());

        mContactApi = ApiHelper.getProxyInstance(ContactApi.class);
    }


    void init(AbstractDao.ReadWriteHelper helper) {
        mContactOperationDao = new ContactOperationDao(helper);
        mContactDao = new ContactDao(helper);
        mContactTags = mContactDao.getAllTagsType();
        List<ContactEntity> contacts = mContactDao.loadAllContacts();
        if (contacts != null) {
            for (ContactEntity contact : contacts) {
                mContactsMap.put(contact.getUserInfo().getUserID(), contact);
            }
        }
    }

    void destroy() {
        mContactsMap.clear();
        mContactChangeListeners.clear();
        mContactOperationListeners.clear();
        mContactOperationUnreadCountChangeListeners.clear();
        mContactsMap.clear();
    }


    public List<ContactEntity> getAllContacts() {
        if (mContactsMap == null) {
            return null;
        }
        List<ContactEntity> contacts = new ArrayList<>(mContactsMap.size() + 4);
        Parcel parcel = Parcel.obtain();
        for (ContactEntity contact : mContactsMap.values()) {
            contact.writeToParcel(parcel, 0);
            contacts.add(ContactEntity.CREATOR.createFromParcel(parcel));
            parcel.setDataSize(0);
            parcel.setDataPosition(0);
        }
        parcel.recycle();
        return contacts;
    }

    public ContactEntity getContact(String userID) {
        return mContactsMap.get(userID);
    }

    public void requestContact(final String userID, final String reason, final ResultCallback<Void> callback) {
        UserEntity user = mAppClient.getUserManager().findUserInfoFromLocal(userID);
        if (user != null) {
            requestContact(user, reason, callback);
        } else {
            mAppClient.getUserManager().findUserInfoByID(userID, new ResultCallback<UserEntity>() {
                @Override
                public void onResult(final UserEntity profile) {
                    requestContact(profile, reason, callback);
                }

                @Override
                public void onFailure(int code, String error) {
                    CallbackUtil.callFailure(code, error, callback);
                }
            });
        }
    }

    private void requestContact(final UserEntity user, final String reason, final ResultCallback<Void> callback) {
        mContactApi.requestContact(user.getUserID(), reason)
                .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        final ContactOperationEntity operation = new ContactOperationEntity();
                        operation.setReason(reason);
                        operation.setUserInfo(user);
                        operation.setTime((int) (System.currentTimeMillis() / 1000));
                        operation.setType(ContactManager.CONTACT_OPERATION_REQUEST_ACTIVE);
                        operation.setRemind(false);
                        if (mContactOperationDao.replace(operation)) {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    for (OnContactOperationListener listener : mContactOperationListeners) {
                                        listener.onContactOperationUpdate(operation);
                                    }
                                }
                            });
                        } else {
                            LogUtil.e("requestContact : Failure of operating database");
                        }
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }), false);
    }

    public void refusedContact(final String userID, final String reason, final ResultCallback<Void> callback) {
        mContactApi.refusedContact(userID, reason)
                .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        final ContactOperationEntity operation = mContactOperationDao.loadByKey(userID);
                        operation.setReason(reason);
                        operation.setTime((int) (System.currentTimeMillis() / 1000));
                        operation.setType(ContactManager.CONTACT_OPERATION_REJECT_ACTIVE);
                        operation.setRemind(false);
                        if (mContactOperationDao.replace(operation)) {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    for (OnContactOperationListener listener : mContactOperationListeners) {
                                        listener.onContactOperationUpdate(operation);
                                    }
                                }
                            });
                        } else {
                            LogUtil.e("rejectContact : Failure of operating database");
                        }
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }), false);
    }

    public void acceptContact(final String userID, final ResultCallback<ContactEntity> callback) {
        mContactApi.acceptContact(userID)
                .enqueue(new ResponseHandler<>(new ResultCallback<UserEntity>() {
                    @Override
                    public void onResult(UserEntity result) {
                        final ContactOperationEntity operation = mContactOperationDao.loadByKey(userID);
                        operation.setTime((int) (System.currentTimeMillis() / 1000));
                        operation.setType(ContactManager.CONTACT_OPERATION_ACCEPT_ACTIVE);
                        operation.setRemind(false);

                         ContactEntity contact = new ContactEntity();
                        contact.setContactID(result.getUserID());
                        contact.setUserInfo(result);

                        if (mContactOperationDao.replace(operation) & addContactToDB(contact)) {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    for (OnContactOperationListener listener : mContactOperationListeners) {
                                        listener.onContactOperationUpdate(operation);
                                    }
                                }
                            });
                            CallbackUtil.callResult(contact, callback);
                        } else {
                            LogUtil.e("acceptContact : Failure of operating database");
                            onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Client));
                        }
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));

    }

    public void deleteContact(final String userID, final ResultCallback<Void> callback) {
        mContactApi.deleteContact(userID)
                .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        deleteContact(userID);
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));
    }

    public void updateContactRemark(final String contactID, final String remarkName, String description, List<String> telephones, List<String> tags, final ResultCallback<Void> callback) {
        mContactApi.updateRemark(contactID, remarkName, description, ContactDao.listToString(telephones), ContactDao.listToString(tags))
                .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        final ContactEntity contact = getContact(contactID);
                        contact.setUploadFlag(0);
                        mContactsMap.put(contactID, contact);
                        if (mContactDao.update(contact)) {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    for (OnContactChangeListener listener : mContactChangeListeners) {
                                        listener.onContactUpdate(contact);
                                    }
                                }
                            });
                        } else {
                            LogUtil.e("updateContactRemark : Failure of operating database");
                        }
                        mAppClient.getConversationManager().updateConversationTitle(Conversation.ConversationType.PRIVATE, contactID, contact.getName());
                        checkTagsChange();
                        CallbackUtil.callResult(result, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        final ContactEntity contact = getContact(contactID);
                        contact.setUploadFlag(1);
                        if (mContactDao.update(contact)) {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    for (OnContactChangeListener listener : mContactChangeListeners) {
                                        listener.onContactUpdate(contact);
                                    }
                                }
                            });
                            CallbackUtil.callResult(null, callback);
                        } else {
                            CallbackUtil.callFailure(code, error, callback);
                        }
                    }
                }), false);
    }

    public Set<String> getAllTags() {
        return mContactTags;
    }

    public ArrayList<TagEntity> getAllTagAndMemberCount() {
        return mContactDao.getAllTagAndMemberCount();
    }

    private boolean addContactToDB(final ContactEntity contact) {
        if (mContactDao.insert(contact)) {
            mContactsMap.put(contact.getUserInfo().getUserID(), contact);
            ContactNotificationMessage ntfMessage = ContactNotificationMessage.obtain(CONTACT_OPERATION_ACCEPT_ACTIVE, mAppClient.getUserManager().getUserID(), contact.getContactID(), "");
            Message hintMessage = Message.obtain(contact.getContactID(), Conversation.ConversationType.PRIVATE, ntfMessage);
            hintMessage.setSentTime(System.currentTimeMillis());
            hintMessage.setReceivedStatus(new Message.ReceivedStatus(1));
            mAppClient.getChatManager().insertIncomingMessage(hintMessage);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactChangeListener listener : mContactChangeListeners) {
                        listener.onContactAdded(contact);
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void deleteContact(final String userID) {
        mContactDao.deleteByKey(userID);
        mAppClient.getConversationManager().clearConversationMessages(Conversation.ConversationType.PRIVATE, userID, null);
        mAppClient.getConversationManager().removeConversation(Conversation.ConversationType.PRIVATE, userID, null);
        ContactOperationEntity contactOperation = mContactOperationDao.loadByKey(userID);
        if (contactOperation != null) {
            boolean needUpdate = contactOperation.isRemind();
            contactOperation.setRemind(false);
            contactOperation.setType(CONTACT_OPERATION_DELETE_ACTIVE);
            mContactOperationDao.update(contactOperation);
            if (needUpdate) {
                updateContactUnreadCount();
            }
        }

        final ContactEntity contact = mContactsMap.remove(userID);
        if (contact != null) {
            List<String> tags = contact.getTags();
            if (tags != null && tags.size() > 0) {
                checkTagsChange();
            }
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactChangeListener listener : mContactChangeListeners) {
                        listener.onContactDeleted(userID);
                    }
                }
            });
        }
    }

    private void checkTagsChange() {
        final Set<String> latestTags = mContactDao.getAllTagsType();
        int oldCount = mContactTags.size();
        int latestCount = latestTags.size();
        if (oldCount == latestCount && mContactTags.containsAll(latestTags)) {
            return;
        }
        final Set<String> oldTags = new HashSet<>(mContactTags);
        mContactTags.clear();
        mContactTags.addAll(latestTags);
        if (latestCount > oldCount) {
            latestTags.removeAll(oldTags);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactTagChangeListener listener : mContactTagChangeListeners) {
                        listener.onContactTagAdded(latestTags);
                    }
                }
            });
        } else {
            oldTags.removeAll(latestTags);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactTagChangeListener listener : mContactTagChangeListeners) {
                        listener.onContactTagDeleted(oldTags);
                    }
                }
            });
        }
    }

    public int getContactUnreadCount() {
        return mContactOperationUnreadNumber;
    }

    void updateContactUnreadCount() {
        final int count = mContactOperationDao.loadRemindCount();
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (count != mContactOperationUnreadNumber) {
                    mContactOperationUnreadNumber = count;
                    for (OnContactOperationUnreadCountChangeListener listener : mContactOperationUnreadCountChangeListeners) {
                        listener.onContactOperationUnreadCountChange(mContactOperationUnreadNumber);
                    }
                }
            }
        });
    }

    public void makeAllContactOperationAsRead() {
        mContactOperationDao.makeAllRemindAsRemind();
        updateContactUnreadCount();
    }


    public void removeContactOperation(final String contactOperationID, final ResultCallback<Void> callback) {
        if (mContactOperationDao.deleteByKey(contactOperationID)) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactOperationListener contactListener : mContactOperationListeners) {
                        contactListener.onContactOperationRemove(contactOperationID);
                    }
                }
            });
            updateContactUnreadCount();
            CallbackUtil.callResult(null, callback);
        } else {
            CallbackUtil.callFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Client), callback);
        }
    }

    public List<ContactOperationEntity> loadAllContactOperation() {
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

    public void addContactTagChangeListener(OnContactTagChangeListener listener) {
        if (!mContactTagChangeListeners.contains(listener)) {
            mContactTagChangeListeners.add(listener);
        }
    }

    public void removeContactTagChangeListener(OnContactTagChangeListener listener) {
        mContactTagChangeListeners.remove(listener);
    }


    void onReceiveContactNotificationMessage(ContactNotificationMessageEx contactMessage) {
        String operation = contactMessage.getOperation();
        String extra = contactMessage.getExtra();
        if (TextUtils.isEmpty(extra)) {
            LogUtil.e("contact operation extra is empty");
            return;
        }
        LogUtil.d("ContactOperationExtra" + contactMessage.getExtra());

        ContactMessageExtra extraContent;

        try {
            extraContent = ApiHelper.GSON.fromJson(contactMessage.getExtra(), ContactMessageExtra.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            LogUtil.e("fromJson ContactMessageExtra.class fail,json content:" + contactMessage.getExtra());
            return;
        }
        if (extraContent == null || extraContent.userProfile == null) {
            LogUtil.e("extraContent == null || extraContent.userProfile == null");
            return;
        }
        if (!mAppClient.getUserManager().replaceUserInfoOnDB(extraContent.userProfile)) {
            LogUtil.e(" ContactOperation : replace user fail");
            return;
        }

        final ContactOperationEntity contactOperation = new ContactOperationEntity();
        contactOperation.setUserInfo(extraContent.userProfile);
        contactOperation.setTime((int) (System.currentTimeMillis() / 1000));
        contactOperation.setType(operation);

        switch (operation) {
            case CONTACT_OPERATION_ACCEPT:
                contactOperation.setRemind(false);
                ContactEntity contact = new ContactEntity();
                contact.setUserInfo(extraContent.userProfile);
                addContactToDB(contact);
                break;
            case CONTACT_OPERATION_DELETE:
                contactOperation.setRemind(false);
                deleteContact(extraContent.userProfile.getUserID());
                break;
            case CONTACT_OPERATION_REQUEST:
                contactOperation.setRemind(true);
                contactOperation.setReason(contactMessage.getMessage());
                break;
            case CONTACT_OPERATION_REJECT:
                contactOperation.setRemind(true);
                break;
            default:
                return;

        }
        if (mContactOperationDao.replace(contactOperation)) {
            updateContactUnreadCount();
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactOperationListener contactListener : mContactOperationListeners) {
                        contactListener.onContactOperationReceive(contactOperation);
                    }
                }
            });
        } else {
            LogUtil.e("insertAll contact operation fail");
        }
    }


    static boolean insertAll(ArrayList<ContactEntity> contacts, AbstractDao.ReadWriteHelper readWriteHelper) {
        ContactDao contactDao = new ContactDao(readWriteHelper);
        contactDao.cleanTable();
        if (contactDao.insertAllContacts(contacts)) {
            return true;
        } else {
            LogUtil.e("updateAllContacts fail");
            return false;
        }
    }


    public interface OnContactOperationListener {
        void onContactOperationReceive(ContactOperationEntity message);

        void onContactOperationUpdate(ContactOperationEntity message);

        void onContactOperationRemove(String contactOperationID);
    }

    public interface OnContactOperationUnreadCountChangeListener {
        void onContactOperationUnreadCountChange(int count);
    }

    public interface OnContactChangeListener {
        void onContactAdded(ContactEntity contact);

        void onContactDeleted(String contactID);

        void onContactUpdate(ContactEntity contact);
    }

    public interface OnContactTagChangeListener {
        void onContactTagAdded(Set<String> newTags);

        void onContactTagDeleted(Set<String> deleteTags);
    }

    private static final class ContactMessageExtra {
        public String sourceUserNickname;
        public long version;
        public UserEntity userProfile;
    }

}
