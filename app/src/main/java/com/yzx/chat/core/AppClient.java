package com.yzx.chat.core;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.database.DBHelper;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.LoginResponseEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.extra.ContactNotificationMessageEx;
import com.yzx.chat.core.extra.VideoMessage;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.core.net.api.AuthApi;
import com.yzx.chat.core.util.CallbackUtil;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.core.util.MD5Util;
import com.yzx.chat.core.util.ResourcesHelper;
import com.yzx.chat.core.util.Sha256Util;

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
        ResourcesHelper.init(context.getApplicationContext());
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
    private LoginExpiredListener mLoginExpiredListener;

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
        mLoginExpiredListener = new LoginExpiredListenerwapper(null);
        mLoginLock = new Semaphore(1);
        mStorageHelper = new StorageHelper(mAppContext, mAppContext.getPackageName());
        mUserManager = new UserManager(this);
        mChatManager = new ChatManager(this);
        mConversationManager = new ConversationManager(this);
        mContactManager = new ContactManager(this);
        mGroupManager = new GroupManager(this);

        initIM();
    }

    private void initIM() {
        RongIMClient.init(mAppContext);
        try {
            RongIMClient.registerMessageType(VideoMessage.class);
            RongIMClient.registerMessageType(ContactNotificationMessageEx.class);
        } catch (AnnotationNotFoundException ignored) {
        }
        RongIMClient.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int remainder) {
                switch (message.getObjectName()) {
                    case "RC:TxtMsg":
                    case "RC:VcMsg":
                    case "RC:ImgMsg":
                    case "RC:LBSMsg":
                    case "RC:FileMsg":
                    case "Custom:VideoMsg":
                        mChatManager.onReceiveContactNotificationMessage(message, remainder);
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
                if (remainder == 0) {
                    mContactManager.updateContactUnreadCount();
                    mConversationManager.updateChatUnreadCount();
                    mConversationManager.onConversationListChange();
                }
                return true;
            }
        });
        RongIMClient.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus connectionStatus) {
                switch (connectionStatus) {
                    case CONNECTED:
                    case CONNECTING:
                        for (OnConnectionStateChangeListener listener : mOnConnectionStateChangeListenerList) {
                            listener.onConnected();
                        }
                        break;
                    case NETWORK_UNAVAILABLE:
                    case DISCONNECTED:
                        for (OnConnectionStateChangeListener listener : mOnConnectionStateChangeListenerList) {
                            listener.onDisconnected();
                        }
                        break;
                    case KICKED_OFFLINE_BY_OTHER_CLIENT:
                    case TOKEN_INCORRECT:
                    case SERVER_INVALID:
                    case CONN_USER_BLOCKED:
                        for (OnConnectionStateChangeListener listener : mOnConnectionStateChangeListenerList) {
                            listener.onUserInvalid();
                        }
                        break;
                }
            }
        });
    }

    public void obtainSMSOfLoginType(String telephone, ResultCallback<Void> callback) {
        mAuthApi.obtainSMSCode(telephone, AuthApi.SMS_CODE_TYPE_LOGIN)
                .enqueue(new ResponseHandler<>(callback));
    }

    public void obtainSMSOfRegisterType(String telephone, ResultCallback<Void> callback) {
        mAuthApi.obtainSMSCode(telephone, AuthApi.SMS_CODE_TYPE_REGISTER)
                .enqueue(new ResponseHandler<>(callback));
    }

    public void register(String account, String password, String nickname, String verifyCode, ResultCallback<Void> callback) {
        mAuthApi.register(account, Sha256Util.sha256WithSalt(password, account), nickname, verifyCode)
                .enqueue(new ResponseHandler<>(callback));
    }

    public void login(String account, String password, String verifyCode, final ResultCallback<Void> callback) {
        try {
            mLoginLock.acquire();
        } catch (InterruptedException e) {
            return;
        }
        if (isLogged) {
            mLoginLock.release();
            throw new RuntimeException("The user has already logged in, please do not log in again！");
        }
        mAuthApi.login(account, Sha256Util.sha256WithSalt(password, account), verifyCode)
                .enqueue(new ResponseHandler<>(new ResultCallback<LoginResponseEntity>() {
                    @Override
                    public void onResult(final LoginResponseEntity result) {
                        String token = result.getToken();
                        UserEntity userInfo = result.getUserProfile();
                        ArrayList<ContactEntity> contacts = result.getContacts();
                        ArrayList<GroupEntity> groups = result.getGroups();
                        if (userInfo == null || userInfo.isEmpty() || TextUtils.isEmpty(token)) {
                            onFailure(ResponseHandler.ERROR_CODE_UNKNOWN, ResourcesHelper.getString(R.string.Error_Server3));
                            return;
                        }
                        mDBHelper = new DBHelper(mAppContext, MD5Util.encrypt32(userInfo.getUserID()), Constants.DATABASE_VERSION);
                        boolean isUpdateSuccess = true;
                        if (!ContactManager.insertAll(contacts, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("insertAll contacts fail");
                        } else if (!GroupManager.insertAll(groups, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("insertAll groups fail");
                        } else if (!UserManager.replaceUserInfoOnDB(userInfo, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("insertAll userInfo fail");
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
                                CallbackUtil.callResult(null, callback);
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


    public void loginByLocalToken(final ResultCallback<Void> callback) {
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
        final String userID = mStorageHelper.getUserID();
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(userID)) {
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
                mDBHelper = new DBHelper(mAppContext, MD5Util.encrypt32(userID), Constants.DATABASE_VERSION);
                init(token, UserManager.getUserInfoFromDB(userID, mDBHelper.getReadWriteHelper()));
                mLoginLock.release();
                CallbackUtil.callResult(null, callback);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                mLoginLock.release();
                CallbackUtil.callFailure(ResponseHandler.ERROR_CODE_UNKNOWN, errorCode.getMessage(), callback);
            }
        });

    }


    private void init(String token, UserEntity userInfo) {
        isLogged = true;
        mToken = token;
        mStorageHelper.saveToken(token);
        mStorageHelper.saveUserID(userInfo.getUserID());
        mUserManager.init(mDBHelper.getReadWriteHelper(), userInfo);
        mChatManager.init();
        mConversationManager.init();
        mContactManager.init(mDBHelper.getReadWriteHelper());
        mGroupManager.init(mDBHelper.getReadWriteHelper());
        mContactManager.updateContactUnreadCount();
        mConversationManager.updateChatUnreadCount();
    }

    private void destroy() {
        mChatManager.destroy();
        mConversationManager.destroy();
        mContactManager.destroy();
        mGroupManager.destroy();
        mUserManager.destroy();
        if(mDBHelper!=null){
            mDBHelper.destroy();
            mDBHelper=null;
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

    public LoginExpiredListener getLoginExpiredListener() {
        return mLoginExpiredListener;
    }

    public void setLoginExpiredListener(LoginExpiredListener listener) {
        mLoginExpiredListener = new LoginExpiredListenerwapper(listener);
    }

    public void addConnectionListener(OnConnectionStateChangeListener listener) {
        if (!mOnConnectionStateChangeListenerList.contains(listener)) {
            mOnConnectionStateChangeListenerList.add(listener);
            if (isConnected()) {
                listener.onConnected();
            } else {
                listener.onDisconnected();
            }
        }
    }

    public void removeConnectionListener(OnConnectionStateChangeListener listener) {
        mOnConnectionStateChangeListenerList.remove(listener);
    }

    private class LoginExpiredListenerwapper implements LoginExpiredListener {
        private LoginExpiredListener mLoginExpiredListener;

        public LoginExpiredListenerwapper(LoginExpiredListener loginExpiredListener) {
            mLoginExpiredListener = loginExpiredListener;
        }

        @Override
        public void onLoginExpired() {
            logout();
            if (mLoginExpiredListener != null) {
                mLoginExpiredListener.onLoginExpired();
            }
        }
    }

    public interface LoginExpiredListener {
        void onLoginExpired();
    }

    public interface OnConnectionStateChangeListener {

        void onConnected();

        void onDisconnected();

        void onUserInvalid();
    }

}

