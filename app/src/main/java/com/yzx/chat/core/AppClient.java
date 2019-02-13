package com.yzx.chat.core;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.database.DBHelper;
import com.yzx.chat.core.entity.LoginResponseEntity;
import com.yzx.chat.core.extra.ContactNotificationMessageEx;
import com.yzx.chat.core.extra.VideoMessage;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.core.net.api.AuthApi;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.util.CallbackUtil;
import com.yzx.chat.core.util.ResourcesHelper;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.core.util.MD5Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class AppClient {

    @SuppressLint("StaticFieldLeak")
    private static AppClient sAppClient;

    public static void init(Context context) {
        sAppClient = new AppClient(context.getApplicationContext());
    }

    public static AppClient getInstance() {
        if (sAppClient == null) {
            throw new RuntimeException("ChatClientManager is not initialized");
        }
        return sAppClient;
    }

    private Context mAppContext;
    private RongIMClient mRongIMClient;
    private AuthApi mAuthApi;
    private List<OnConnectionStateChangeListener> mOnConnectionStateChangeListenerList;

    private UserManager mUserManager;
    private ChatManager mChatManager;
    private ContactManager mContactManager;
    private GroupManager mGroupManager;
    private ConversationManager mConversationManager;
    private DBHelper mDBHelper;
    private StorageHelper mStorageHelper;

    private String mToken;
    private String mDeviceID;
    private Semaphore mLoginLock;
    private boolean isLogged;

    private AppClient(Context appContext) {
        mAppContext = appContext.getApplicationContext();
        mRongIMClient = RongIMClient.getInstance();
        mOnConnectionStateChangeListenerList = Collections.synchronizedList(new LinkedList<OnConnectionStateChangeListener>());
        mAuthApi = ApiHelper.getProxyInstance(AuthApi.class);
        mLoginLock = new Semaphore(1);
        mStorageHelper = new StorageHelper(mAppContext, mAppContext.getPackageName());

        RongIMClient.init(mAppContext);
        try {
            RongIMClient.registerMessageType(VideoMessage.class);
            RongIMClient.registerMessageType(ContactNotificationMessageEx.class);
        } catch (AnnotationNotFoundException ignored) {
        }
        RongIMClient.setOnReceiveMessageListener(mOnReceiveMessageListener);
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);
    }

    public void login(String account, String password, String verifyCode, final ResultCallback<LoginResponseEntity> callback) {
        try {
            mLoginLock.acquire();
        } catch (InterruptedException e) {
            return;
        }
        if (isLogged) {
            mLoginLock.release();
            throw new RuntimeException("The user has already logged in, please do not log in again！");
        }
        mAuthApi.login(account, password, verifyCode)
                .enqueue(new ResponseHandler<>(new ResultCallback<LoginResponseEntity>() {
                    @Override
                    public void onResult(final LoginResponseEntity result) {
                        String token = result.getToken();
                        UserEntity userEntity = result.getUserProfile();
                        ArrayList<ContactEntity> contacts = result.getContacts();
                        ArrayList<GroupEntity> groups = result.getGroups();
                        if (userEntity == null || userEntity.isEmpty() || TextUtils.isEmpty(token)) {
                            onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Server3));
                            return;
                        }
                        mDBHelper = new DBHelper(mAppContext, MD5Util.encrypt32(userEntity.getUserID()), Constants.DATABASE_VERSION);
                        boolean isUpdateSuccess = true;
                        if (!ContactManager.insertAll(contacts, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("insertAll contacts fail");
                        } else if (!GroupManager.insertAll(groups, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("insertAll groups fail");
                        }
                        if (!isUpdateSuccess) {
                            onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Client));
                            return;
                        }
                        RongIMClient.connect(result.getToken(), new RongIMClient.ConnectCallback() {
                            @Override
                            public void onTokenIncorrect() {
                                onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Client));
                            }

                            @Override
                            public void onSuccess(String s) {
                                init(result.getToken(), result.getUserProfile());
                                mLoginLock.release();
                                CallbackUtil.callResult(result, callback);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, errorCode.getMessage());
                            }
                        });

                    }

                    @Override
                    public void onFailure(int code, String error) {
                        destroy();
                        mLoginLock.release();
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));
    }


    public void loginByLocalToken(final ResultCallback<UserEntity> callback) {
        try {
            mLoginLock.acquire();
        } catch (InterruptedException e) {
            return;
        }
        if (isLogged) {
            mLoginLock.release();
            throw new RuntimeException("The user has already logged in, please do not log in again！");
        }
        final String token = mStorageHelper.getToken();
        String strUserInfo = mStorageHelper.getUserInfo();
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(strUserInfo)) {
            mLoginLock.release();
            CallbackUtil.callFailure(ResponseHandler.ERROR_CODE_NOT_LOGGED_IN, "", callback);
            return;
        }
        try {
            final UserEntity userInfo = ApiHelper.GSON.fromJson(strUserInfo, UserEntity.class);
            if (userInfo == null || userInfo.isEmpty()) {
                mLoginLock.release();
                CallbackUtil.callFailure(ResponseHandler.ERROR_CODE_NOT_LOGGED_IN, "", callback);
                return;
            }
            RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                @Override
                public void onTokenIncorrect() {
                    mLoginLock.release();
                    CallbackUtil.callFailure(ResponseHandler.ERROR_CODE_NOT_LOGGED_IN, "", callback);
                }

                @Override
                public void onSuccess(String s) {
                    init(token, userInfo);
                    mLoginLock.release();
                    CallbackUtil.callResult(userInfo, callback);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    mLoginLock.release();
                    CallbackUtil.callFailure(ResponseHandler.ERROR_CODE_UNKNOWN, errorCode.getMessage(), callback);
                }
            });
        } catch (JsonSyntaxException e) {
            mLoginLock.release();
            CallbackUtil.callFailure(ResponseHandler.ERROR_CODE_NOT_LOGGED_IN, "", callback);
        }

    }



    private void init(String token, UserEntity userInfo) {
        isLogged = true;
        mToken = token;
        mUserManager = new UserManager(this, userInfo);
        mChatManager = new ChatManager(this);
        mConversationManager = new ConversationManager(this);
        mContactManager = new ContactManager(this, mDBHelper.getReadWriteHelper());
        mGroupManager = new GroupManager(this, mDBHelper.getReadWriteHelper());
        mContactManager.updateContactUnreadCount();
        mConversationManager.updateChatUnreadCount();
    }

    private void destroy() {
        if (mChatManager != null) {
            mChatManager.destroy();
            mChatManager = null;
        }
        if (mConversationManager != null) {
            mConversationManager.destroy();
            mConversationManager = null;
        }
        if (mContactManager != null) {
            mContactManager.destroy();
            mContactManager = null;
        }
        if (mGroupManager != null) {
            mGroupManager.destroy();
            mGroupManager = null;
        }
        if (mUserManager != null) {
            mUserManager.destroy();
            mUserManager = null;
        }
        if (mDBHelper != null) {
            mDBHelper.destroy();
            mDBHelper = null;
        }
        isLogged = false;
    }

    public synchronized void logout() {
        try {
            mLoginLock.acquire();
        } catch (InterruptedException e) {
            return;
        }
        mRongIMClient.logout();
        destroy();
        SharePreferenceManager.getIdentityPreferences().clear(false);
        mLoginLock.release();
    }

    public boolean isLogged() {
        return isLogged;
    }

    public boolean isConnected() {
        return mRongIMClient.getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED;
    }

    public String getToken() {
        return mToken;
    }

    public String getDeviceID() {
        if (TextUtils.isEmpty(mDeviceID)) {
            synchronized (this) {
                if (TextUtils.isEmpty(mDeviceID)) {
                    mDeviceID = mStorageHelper.getDeviceID();
                }
                if (TextUtils.isEmpty(mDeviceID)) {
                    mDeviceID = String.format(Locale.getDefault(), "%s(%s).%s", Build.BRAND, Build.MODEL, UUID.randomUUID().toString());
                    mDeviceID = mDeviceID.replaceAll(" ", "_");
                    if (!mStorageHelper.saveDeviceID(mDeviceID)) {
                        LogUtil.w("saveDeviceIDToLocal fail");
                    }
                }
            }
        }
        return mDeviceID;
    }

    public Context getAppContext() {
        return mAppContext;
    }

    public RongIMClient getRongIMClient() {
        return mRongIMClient;
    }

    public ChatManager getChatManager() {
        return mChatManager;
    }

    public ContactManager getContactManager() {
        return mContactManager;
    }

    public GroupManager getGroupManager() {
        return mGroupManager;
    }

    public ConversationManager getConversationManager() {
        return mConversationManager;
    }

    public UserManager getUserManager() {
        return mUserManager;
    }


    public void addConnectionListener(OnConnectionStateChangeListener listener) {
        if (!mOnConnectionStateChangeListenerList.contains(listener)) {
            mOnConnectionStateChangeListenerList.add(listener);
            if (isConnected()) {
                listener.onConnected();
            } else {
                listener.onDisconnected(mRongIMClient.getCurrentConnectionStatus().getMessage());
            }
        }
    }

    public void removeConnectionListener(OnConnectionStateChangeListener listener) {
        mOnConnectionStateChangeListenerList.remove(listener);
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
            for (OnConnectionStateChangeListener listener : mOnConnectionStateChangeListenerList) {
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
                case "RC:ImgMsg":
                case "RC:LBSMsg":
                case "RC:FileMsg":
                case "Custom:VideoMsg":
                    mChatManager.onReceiveContactNotificationMessage(message, i);
                    break;
                case "Custom:ContactNtf"://该类型不会保存期起来
                    mContactManager.onReceiveContactNotificationMessage((ContactNotificationMessageEx) message.getContent());
                    break;
                case "RC:GrpNtf":
                    mGroupManager.onReceiveGroupNotificationMessage((GroupNotificationMessage) message.getContent());
                    break;
                default:
                    LogUtil.e("Unknown Message ObjectName:" + message.getObjectName());
            }
            if (i == 0 && mConversationManager != null && mContactManager != null) {
                mConversationManager.updateChatUnreadCount();
                mContactManager.updateContactUnreadCount();
            }
            return true;
        }
    };

    public interface OnConnectionStateChangeListener {

        void onConnected();

        void onDisconnected(String reason);

    }
}

