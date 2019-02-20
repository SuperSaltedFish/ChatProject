package com.yzx.chat.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.core.entity.ContactRemarkEntity;
import com.yzx.chat.core.entity.GetUserProfileEntity;
import com.yzx.chat.core.entity.TagEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.database.AbstractDao;
import com.yzx.chat.core.database.ContactDao;
import com.yzx.chat.core.database.ContactOperationDao;
import com.yzx.chat.core.database.UserDao;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.core.net.api.ContactApi;
import com.yzx.chat.core.extra.ContactNotificationMessageEx;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.util.CallbackUtil;
import com.yzx.chat.core.util.ResourcesHelper;
import com.yzx.chat.core.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static final String CONTACT_OPERATION_REQUEST = "Request";//对方请求
    public static final String CONTACT_OPERATION_ACCEPT = "Accept";//对方同意添加
    public static final String CONTACT_OPERATION_REJECT = "Reject";//对方拒绝添加
    public static final String CONTACT_OPERATION_DELETE = "Delete";//对方删除好友

    public static final String CONTACT_OPERATION_REQUEST_ACTIVE = "ActiveRequest";//主动请求
    public static final String CONTACT_OPERATION_ACCEPT_ACTIVE = "ActiveAccept";//主动同意添加
    public static final String CONTACT_OPERATION_REJECT_ACTIVE = "ActiveReject";//主动拒绝添加
    public static final String CONTACT_OPERATION_DELETE_ACTIVE = "ActiveDelete";//主动删除好友

    private static final Set<String> CONTACT_OPERATION_SET = new HashSet<>(Arrays.asList(CONTACT_OPERATION_REQUEST, CONTACT_OPERATION_ACCEPT, CONTACT_OPERATION_REJECT, CONTACT_OPERATION_DELETE));

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
    private UserDao mUserDao;

    private ContactApi mContactApi;

    private volatile int mContactOperationUnreadNumber;

    ContactManager(AppClient appClient, AbstractDao.ReadWriteHelper helper) {
        mAppClient = appClient;
        mRongIMClient = mAppClient.getRongIMClient();
        mUIHandler = new Handler(Looper.getMainLooper());
        mContactsMap = new HashMap<>(256);

        mContactChangeListeners = Collections.synchronizedList(new LinkedList<OnContactChangeListener>());
        mContactOperationListeners = Collections.synchronizedList(new LinkedList<OnContactOperationListener>());
        mContactOperationUnreadCountChangeListeners = Collections.synchronizedList(new LinkedList<OnContactOperationUnreadCountChangeListener>());
        mContactTagChangeListeners = Collections.synchronizedList(new LinkedList<OnContactTagChangeListener>());

        mContactApi = ApiHelper.getProxyInstance(ContactApi.class);

        mContactOperationDao = new ContactOperationDao(helper);
        mContactDao = new ContactDao(helper);
        mUserDao = new UserDao(helper);


        mContactTags = mContactDao.getAllTagsType();
        List<ContactEntity> contacts = mContactDao.loadAllContacts();
        if (contacts != null) {
            for (ContactEntity contact : contacts) {
                mContactsMap.put(contact.getUserProfile().getUserID(), contact);
            }
        }
    }

    public List<ContactEntity> getAllContacts() {
        if (mContactsMap == null) {
            return null;
        }
        List<ContactEntity> contacts = new ArrayList<>(mContactsMap.size() + 4);
        Parcel parcel;
        for (ContactEntity contact : mContactsMap.values()) {
            parcel = Parcel.obtain();
            contact.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            contacts.add(ContactEntity.CREATOR.createFromParcel(parcel));
            parcel.recycle();
        }
        return contacts;
    }

    public ContactEntity getContact(String userID) {
        return mContactsMap.get(userID);
    }

    public void requestContact(final String userID, final String reason, final ResultCallback<Void> callback) {
        mAppClient.getUserManager().findUserProfileByID(userID, new ResultCallback<GetUserProfileEntity>() {
            @Override
            public void onResult(final GetUserProfileEntity profile) {
                mContactApi.requestContact(userID, reason)
                        .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                            @Override
                            public void onResult(Void result) {
                                final ContactOperationEntity operation = new ContactOperationEntity();
                                operation.setReason(reason);
                                operation.setUser(profile.getUserProfile());
                                operation.setTime((int) (System.currentTimeMillis() / 1000));
                                operation.setType(ContactManager.CONTACT_OPERATION_REQUEST_ACTIVE);
                                operation.setRemind(false);
                                if (mContactOperationDao.replace(operation)) {
                                    for (OnContactOperationListener listener : mContactOperationListeners) {
                                        listener.onContactOperationUpdate(operation);
                                    }
                                    CallbackUtil.callResult(result, callback);
                                } else {
                                    LogUtil.e("requestContact : Failure of operating database");
                                    onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Client));
                                }
                            }

                            @Override
                            public void onFailure(int code, String error) {
                                CallbackUtil.callFailure(code, error, callback);
                            }
                        }));
            }

            @Override
            public void onFailure(int code, String error) {
                CallbackUtil.callFailure(code, error, callback);
            }
        });
    }

    public void refusedContact(final String userID, final String reason, final ResultCallback<Void> callback) {
        mContactApi.refusedContact(userID, reason)
                .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        ContactOperationEntity operation = mContactOperationDao.loadByKey(userID);
                        operation.setReason(reason);
                        operation.setTime((int) (System.currentTimeMillis() / 1000));
                        operation.setType(ContactManager.CONTACT_OPERATION_REJECT_ACTIVE);
                        operation.setRemind(false);
                        if (mContactOperationDao.replace(operation)) {
                            for (OnContactOperationListener listener : mContactOperationListeners) {
                                listener.onContactOperationUpdate(operation);
                            }
                            CallbackUtil.callResult(result, callback);
                        } else {
                            LogUtil.e("rejectContact : Failure of operating database");
                            onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Client));
                        }
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));
    }

    public void acceptContact(final String userID, final ResultCallback<Void> callback) {
        mAppClient.getUserManager().findUserProfileByID(userID, new ResultCallback<GetUserProfileEntity>() {
            @Override
            public void onResult(final GetUserProfileEntity user) {
                mContactApi.acceptContact(userID)
                        .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                            @Override
                            public void onResult(Void result) {
                                ContactOperationEntity operation = mContactOperationDao.loadByKey(userID);
                                operation.setTime((int) (System.currentTimeMillis() / 1000));
                                operation.setType(ContactManager.CONTACT_OPERATION_ACCEPT_ACTIVE);
                                operation.setRemind(false);

                                final ContactEntity contact = new ContactEntity();
                                contact.setUserProfile(user.getUserProfile());
                                contact.setRemark(new ContactRemarkEntity());

                                if (mContactOperationDao.replace(operation) & addContactToDB(contact)) {
                                    for (OnContactOperationListener listener : mContactOperationListeners) {
                                        listener.onContactOperationUpdate(operation);
                                    }
                                    CallbackUtil.callResult(result, callback);
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

            @Override
            public void onFailure(int code, String error) {
                CallbackUtil.callFailure(code, error, callback);
            }
        });

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

    public void updateContactRemark(final ContactEntity contact, final ResultCallback<Void> callback) {
        mContactApi.updateRemark(contact.getUserProfile().getUserID(), contact.getRemark())
                .enqueue(new ResponseHandler<>(new ResultCallback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        contact.getRemark().setUploadFlag(0);
                        UserEntity user = contact.getUserProfile();
                        if (mContactDao.update(contact) & mUserDao.update(user)) {
                            mContactsMap.put(user.getUserID(), contact);
                            mAppClient.getConversationManager().updateConversationTitle(Conversation.ConversationType.PRIVATE, user.getUserID(), contact.getName());
                            for (OnContactChangeListener listener : mContactChangeListeners) {
                                listener.onContactUpdate(contact);
                            }
                            checkTagsChange();
                            CallbackUtil.callResult(result, callback);
                        } else {
                            LogUtil.e("updateContactRemark : Failure of operating database");
                            onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Client));
                        }

                    }

                    @Override
                    public void onFailure(int code, String error) {
                        contact.getRemark().setUploadFlag(1);
                        if (mContactDao.update(contact) & mUserDao.update(contact.getUserProfile())) {
                            for (OnContactChangeListener listener : mContactChangeListeners) {
                                listener.onContactUpdate(contact);
                            }
                            CallbackUtil.callResult(null, callback);
                        } else {
                            CallbackUtil.callFailure(code, error, callback);
                        }
                    }
                }));
    }

    public Set<String> getAllTags() {
        return mContactTags;
    }

    public ArrayList<TagEntity> getAllTagAndMemberCount() {
        return mContactDao.getAllTagAndMemberCount();
    }

    private boolean addContactToDB(final ContactEntity contact) {
        if (mContactDao.insert(contact)) {
            mContactsMap.put(contact.getUserProfile().getUserID(), contact);
            ContactMessageExtra extra = new ContactMessageExtra();
            extra.userProfile = contact.getUserProfile();
            extra.sourceUserNickname = extra.userProfile.getNickname();
            ContactNotificationMessage messageContent = ContactNotificationMessage.obtain(CONTACT_OPERATION_ACCEPT_ACTIVE, extra.userProfile.getUserID(), extra.userProfile.getUserID(), "");
            messageContent.setExtra(ApiHelper.GSON.toJson(extra));
            Message hintMessage = Message.obtain(extra.userProfile.getUserID(), Conversation.ConversationType.PRIVATE, messageContent);
            hintMessage.setSenderUserId(mAppClient.getUserManager().getUserID());
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

    private void deleteContact(String userID) {
        mContactDao.deleteByKey(userID);
        mAppClient.getConversationManager().clearAllConversationMessages(Conversation.ConversationType.PRIVATE, userID);
        mAppClient.getConversationManager().removeConversation(Conversation.ConversationType.PRIVATE, userID);
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
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactChangeListener listener : mContactChangeListeners) {
                        listener.onContactDeleted(contact);
                    }
                    checkTagsChange();
                }
            });
        }
    }

    private void checkTagsChange() {
        Set<String> latestTags = mContactDao.getAllTagsType();
        int oldCount = mContactTags.size();
        int latestCount = latestTags.size();
        if (oldCount == latestCount && mContactTags.containsAll(latestTags)) {
            return;
        }
        Set<String> oldTags = new HashSet<>(mContactTags);
        mContactTags.clear();
        mContactTags.addAll(latestTags);
        if (latestCount > oldCount) {
            latestTags.removeAll(oldTags);
            for (OnContactTagChangeListener listener : mContactTagChangeListeners) {
                listener.onContactTagAdded(latestTags);
            }
        } else if (latestCount < oldCount) {
            oldTags.removeAll(latestTags);
            for (OnContactTagChangeListener listener : mContactTagChangeListeners) {
                listener.onContactTagDeleted(oldTags);
            }
        } else {
            throw new RuntimeException("Unknown tags state");
        }
    }

    public int getContactUnreadCount() {
        return mContactOperationUnreadNumber;
    }

    void updateContactUnreadCount() {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                int count = mContactOperationDao.loadRemindCount();
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
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                mContactOperationDao.makeAllRemindAsRemind();
                if (mContactOperationUnreadNumber != 0) {
                    updateContactUnreadCount();
                }
            }
        });
    }


    public void removeContactOperationAsync(final ContactOperationEntity contactMessage) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mContactOperationDao.delete(contactMessage)) {
                    for (OnContactOperationListener contactListener : mContactOperationListeners) {
                        contactListener.onContactOperationRemove(contactMessage);
                    }
                    updateContactUnreadCount();
                } else {
                    LogUtil.e("delete ContactMessageFail from DB");
                }
            }
        });
    }

    public ContactOperationEntity getContactOperation(String ContactID) {
        return mContactOperationDao.loadByKey(ContactID);
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

    void destroy() {
        mContactsMap.clear();
        mContactChangeListeners.clear();
        mContactOperationListeners.clear();
        mContactOperationUnreadCountChangeListeners.clear();
        mContactsMap.clear();

        mContactsMap = null;
        mContactChangeListeners = null;
        mContactOperationListeners = null;
        mContactOperationUnreadCountChangeListeners = null;
    }

    void onReceiveContactNotificationMessage(ContactNotificationMessageEx contactMessage) {
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
            extra = ApiHelper.GSON.fromJson(contactMessage.getExtra(), ContactMessageExtra.class);
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


        ContactOperationEntity old = mContactOperationDao.loadByKey(extra.userProfile.getUserID());
        final ContactOperationEntity contactOperation = new ContactOperationEntity();
        contactOperation.setUser(extra.userProfile);
        contactOperation.setReason(contactMessage.getMessage());
        contactOperation.setRemind(true);
        contactOperation.setTime((int) (System.currentTimeMillis() / 1000));
        contactOperation.setType(operation);

        switch (operation) {
            case CONTACT_OPERATION_ACCEPT:
                ContactEntity contact = new ContactEntity();
                contact.setUserProfile(extra.userProfile);
                contact.setRemark(new ContactRemarkEntity());
                addContactToDB(contact);
                contactOperation.setRemind(false);
                break;
            case CONTACT_OPERATION_DELETE:
                deleteContact(extra.userProfile.getUserID());
                contactOperation.setRemind(false);
                break;
        }

        if (old != null) {
            if (!TextUtils.equals(operation, CONTACT_OPERATION_DELETE)) {
                contactOperation.setRemind((!old.getType().equals(contactOperation.getType())) || old.isRemind());
            }
            if (!CONTACT_OPERATION_REQUEST.equals(operation)) {
                contactOperation.setReason(old.getReason());
            }
            if (!mContactOperationDao.update(contactOperation)) {
                LogUtil.e("insertAll contact operation fail");
                return;
            }
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactOperationListener contactListener : mContactOperationListeners) {
                        contactListener.onContactOperationUpdate(contactOperation);
                    }
                }
            });

        } else {
            if (!mContactOperationDao.insert(contactOperation)) {
                LogUtil.e("replace contact operation fail");
                return;
            }
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnContactOperationListener contactListener : mContactOperationListeners) {
                        contactListener.onContactOperationReceive(contactOperation);
                    }
                }
            });
        }
        if (contactOperation.isRemind()) {
            updateContactUnreadCount();
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

        void onContactOperationRemove(ContactOperationEntity message);
    }

    public interface OnContactOperationUnreadCountChangeListener {
        void onContactOperationUnreadCountChange(int count);
    }

    public interface OnContactChangeListener {
        void onContactAdded(ContactEntity contact);

        void onContactDeleted(ContactEntity contact);

        void onContactUpdate(ContactEntity contact);
    }

    public interface OnContactTagChangeListener {
        void onContactTagAdded(Set<String> newTags);

        void onContactTagDeleted(Set<String> deleteTags);
    }

    public static final class ContactMessageExtra {
        public String sourceUserNickname;
        public long version;
        public UserEntity userProfile;
    }

}
