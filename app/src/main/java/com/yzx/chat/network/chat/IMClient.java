package com.yzx.chat.network.chat;


import android.content.Context;
import android.support.annotation.NonNull;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.DBHelper;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.auth.TokenVerifyBean;
import com.yzx.chat.network.api.contact.ContactApi;
import com.yzx.chat.network.api.contact.GetUserContactsBean;
import com.yzx.chat.network.framework.HttpCallback;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.MD5Util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class IMClient {

    private static IMClient sIMClient;
    private static Context sAppContext;

    public static void init(Context context, String imAppKey) {
        sAppContext = context.getApplicationContext();
        RongIMClient.init(sAppContext, imAppKey);
        sIMClient = new IMClient();
    }

    public static IMClient getInstance() {
        if (sIMClient == null) {
            throw new RuntimeException("ChatClientManager is not initialized");
        }
        return sIMClient;
    }


    private RongIMClient mRongIMClient;
    private ChatManager mChatManager;
    private ContactManager mContactManager;
    private ConversationManager mConversationManager;
    private ThreadPoolExecutor mWorkExecutor;
    private DBHelper mDBHelper;
    private List<OnConnectionStateChangeListener> mOnConnectionStateChangeListenerList;

    private boolean isLogged;

    private IMClient() {
        mRongIMClient = RongIMClient.getInstance();
        RongIMClient.setOnReceiveMessageListener(mOnReceiveMessageListener);
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);

        mOnConnectionStateChangeListenerList = Collections.synchronizedList(new LinkedList<OnConnectionStateChangeListener>());

        mWorkExecutor = new ThreadPoolExecutor(
                0,
                2,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(32));
    }

    public void login(String token, final OnLoginListener loginListener) {
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                isLogged = false;
                loginListener.onLoginFailure("TokenIncorrect");
            }

            @Override
            public void onSuccess(String s) {
                loginSuccess(true);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                switch (errorCode) {
                    case RC_CONN_ID_REJECT:
                    case RC_CONN_USER_OR_PASSWD_ERROR:
                    case RC_CONN_NOT_AUTHRORIZED:
                    case RC_CONN_PACKAGE_NAME_INVALID:
                    case RC_CONN_APP_BLOCKED_OR_DELETED:
                    case RC_CONN_USER_BLOCKED:
                    case RC_DISCONN_KICK:
                        isLogged = false;
                        loginListener.onLoginFailure(errorCode.getMessage());
                        break;
                    default:
                        loginSuccess(false);
                }
            }

            private void loginSuccess(boolean isConnectedToServer) {
                isLogged = true;
                mDBHelper = new DBHelper(sAppContext, MD5Util.encrypt(IdentityManager.getInstance().getUserID()), Constants.DATABASE_VERSION);
                mChatManager = new ChatManager(mSubManagerCallback);
                mContactManager = new ContactManager(mSubManagerCallback);
                mConversationManager = new ConversationManager(mSubManagerCallback);
                loginListener.onLoginSuccess(isConnectedToServer);
            }
        });
    }

    private void initHTTPServer(boolean isAlreadyLogged) {
        ContactApi contactApi = (ContactApi) ApiHelper.getProxyInstance(ContactApi.class);
        AuthApi authApi = (AuthApi) ApiHelper.getProxyInstance(AuthApi.class);

        AsyncUtil.cancelCall(mTokenVerify);
        mTokenVerify = authApi.tokenVerify();
        mTokenVerify.setCallback(new HttpCallback<JsonResponse<TokenVerifyBean>>() {
            private boolean isSuccess;

            @Override
            public void onResponse(HttpResponse<JsonResponse<TokenVerifyBean>> response) {
                if (response.getResponseCode() == 200) {
                    JsonResponse<TokenVerifyBean> jsonResponse = response.getResponse();
                    if (jsonResponse != null) {
                        TokenVerifyBean tokenVerifyBean = jsonResponse.getData();
                        if (jsonResponse.getStatus() == 200 && tokenVerifyBean != null) {
                            UserBean userBean = tokenVerifyBean.getUser();
                            if (userBean != null && !userBean.isEmpty() && IdentityManager.getInstance().updateUserInfo(userBean)) {
                                isSuccess = true;
                                return;
                            }
                        }
                    }
                }
                initComplete();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                if (IdentityManager.initFromLocal()) {
                    isSuccess = true;
                } else {
                    mSplashView.startLoginActivity();
                }
            }

            @Override
            public boolean isExecuteNextTask() {
                return isSuccess;
            }
        });

        AsyncUtil.cancelCall(mGetUserFriendsTask);
        mGetUserFriendsTask = contactApi.getUserContacts();
        mGetUserFriendsTask.setCallback(new BaseHttpCallback<GetUserContactsBean>() {
            @Override
            protected void onSuccess(GetUserContactsBean response) {
                List<ContactBean> contactBeans = response.getContacts();
                if (contactBeans != null) {
                    IMClient.getInstance().contactManager().initContacts(contactBeans);
                } else {
                    LogUtil.e("response.getContacts() is null");
                    IMClient.getInstance().contactManager().initContactsFromDB();
                }
                isInitHTTPComplete = true;
                initComplete();
            }

            @Override
            protected void onFailure(String message) {
                LogUtil.e(message);
                IMClient.getInstance().contactManager().initContactsFromDB();
                initComplete();
            }
        });
        if (isAlreadyLogged) {
            sHttpExecutor.submit(mGetUserFriendsTask);
        } else {
            sHttpExecutor.submit(mTokenVerify, mGetUserFriendsTask);
        }
    }

    public void logout() {
        if (isLogged) {
            isLogged = false;
            mRongIMClient.logout();
            mChatManager.destroy();
            mContactManager.destroy();
            mConversationManager.destroy();
            mDBHelper.destroy();
            mChatManager = null;
            mContactManager = null;
            mConversationManager = null;
            mDBHelper = null;
        }
    }

    public boolean isConnected() {
        return mRongIMClient.getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED;
    }

    public ChatManager chatManager() {
        return mChatManager;
    }

    public ContactManager contactManager() {
        return mContactManager;
    }

    public ConversationManager conversationManager() {
        return mConversationManager;
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















    private final SubManagerCallback mSubManagerCallback = new SubManagerCallback() {
        @Override
        public void chatManagerCallback(int callbackCode, Object arg) {

        }

        @Override
        public void conversationManagerCallback(int callbackCode, Object arg) {
            switch (callbackCode) {
                case ConversationManager.CALLBACK_CODE_UPDATE_UNREAD:
                    mChatManager.updateChatUnreadCount();
                    break;
            }
        }

        @Override
        public void contactManagerCallback(int callbackCode, Object arg) {

        }

        @Override
        public AbstractDao.ReadWriteHelper getDatabaseReadWriteHelper() {
            return mDBHelper.getReadWriteHelper();
        }

        @Override
        public void execute(Runnable runnable) {
            mWorkExecutor.execute(runnable);
        }
    };

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
                    mChatManager.onReceiveContactNotificationMessage(message, i);
                    break;
                case "RC:ContactNtf":
                    mContactManager.onReceiveContactNotificationMessage(message);
                    break;
            }
            if (i == 0) {
                mChatManager.updateChatUnreadCount();
                mContactManager.updateContactUnreadCount();
            }
            return true;
        }
    };

    public interface OnLoginListener {

        void onLoginSuccess(boolean isConnectedToServer);

        void onLoginFailure(String error);

    }

    public interface OnConnectionStateChangeListener {

        void onConnected();

        void onDisconnected(String reason);

    }

    interface SubManagerCallback {

        void chatManagerCallback(int callbackCode, Object arg);

        void conversationManagerCallback(int callbackCode, Object arg);

        void contactManagerCallback(int callbackCode, Object arg);

        AbstractDao.ReadWriteHelper getDatabaseReadWriteHelper();

        void execute(Runnable runnable);
    }
}
