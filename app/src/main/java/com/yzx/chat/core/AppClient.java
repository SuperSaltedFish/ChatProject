package com.yzx.chat.core;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yzx.chat.R;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.listener.Result;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.database.AbstractDao;
import com.yzx.chat.core.database.DBHelper;
import com.yzx.chat.core.entity.JsonRequest;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.entity.UserInfoEntity;
import com.yzx.chat.core.extra.ContactNotificationMessageEx;
import com.yzx.chat.core.extra.VideoMessage;
import com.yzx.chat.core.net.api.AuthApi;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.Executor.HttpExecutor;
import com.yzx.chat.core.net.framework.Executor.HttpRequest;
import com.yzx.chat.core.net.framework.ResponseCallback;
import com.yzx.chat.core.net.framework.HttpConverter;
import com.yzx.chat.core.net.framework.HttpResponse;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.MD5Util;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class AppClient implements IManagerHelper {

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
    private Handler mUIHandler;
    private AuthApi mAuthApi;
    private List<OnConnectionStateChangeListener> mOnConnectionStateChangeListenerList;

    private UserManager mUserManager;
    private ConfigurationManager mConfigurationManager;
    private ChatManager mChatManager;
    private ContactManager mContactManager;
    private GroupManager mGroupManager;
    private ConversationManager mConversationManager;
    private DBHelper mDBHelper;


    private boolean isLogged;

    private AppClient(Context appContext) {
        mAppContext = appContext.getApplicationContext();
        mRongIMClient = RongIMClient.getInstance();
        mUIHandler = new Handler(Looper.getMainLooper());
        mOnConnectionStateChangeListenerList = Collections.synchronizedList(new LinkedList<OnConnectionStateChangeListener>());
        mAuthApi = ApiHelper.getProxyInstance(AuthApi.class);

        mUserManager = new UserManager();
        mChatManager = new ChatManager();
        mConversationManager = new ConversationManager();
        mContactManager = new ContactManager();
        mGroupManager = new GroupManager();


        RongIMClient.init(mAppContext);
        try {
            RongIMClient.registerMessageType(VideoMessage.class);
            RongIMClient.registerMessageType(ContactNotificationMessageEx.class);
        } catch (AnnotationNotFoundException ignored) {
        }
        RongIMClient.setOnReceiveMessageListener(mOnReceiveMessageListener);
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);
    }

    public void login(String account, String password, String verifyCode) {
        mAuthApi.login(account,password,verifyCode)
                .enqueue();
    }


    public void loginByToken(Call<JsonResponse<UserInfoEntity>> loginOrRegisterOrTokenVerifyCall, final ResultCallback<Void> resultCallback) {
        loginOrRegisterOrTokenVerifyCall.setHttpConverter(new HttpConverter() {
            private Gson mGson = ApiHelper.getDefaultGsonInstance();

            @Nullable
            @Override
            public byte[] convertRequest(Map<String, Object> requestParams) {
                JsonRequest request = new JsonRequest();
                request.setParams(requestParams);
                request.setStatus(200);
                request.setToken(UserManager.getLocalToken());
                String json = mGson.toJson(request);
                LogUtil.e("convertRequest: " + json);
                return json == null ? null : json.getBytes(StandardCharsets.UTF_8);
            }

            @Nullable
            @Override
            public byte[] convertMultipartRequest(String partName, Object body) {
                return null;
            }

            @Nullable
            @Override
            public Object convertResponseBody(byte[] body, Type genericType) {
                if (body == null || body.length == 0) {
                    return null;
                }
                String strBody = new String(body);
                LogUtil.e("convertResponseBody:" + strBody);
                return ApiHelper.getDefaultGsonInstance().fromJson(new String(body), genericType);
            }

        });
        login(loginOrRegisterOrTokenVerifyCall, resultCallback);
    }


    public void login(final Call<JsonResponse<UserInfoEntity>> loginOrRegisterOrTokenVerifyCall, final ResultCallback<Void> resultCallback) {
        if (isLogged) {
            throw new RuntimeException("The user has already logged in, please do not log in again！");
        }
        mWorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final CountDownLatch latch = new CountDownLatch(1);
                final Result<Boolean> result = new Result<>(true);
                final Result<String> errorMessage = new Result<>(AndroidUtil.getString(R.string.Error_Server1));
                loginOrRegisterOrTokenVerifyCall.setResponseCallback(new ResponseCallback<JsonResponse<UserInfoEntity>>() {

                    @Override
                    public void onResponse(HttpRequest request, HttpResponse<JsonResponse<UserInfoEntity>> response) {
                        if (response.responseCode() != 200) {
                            failure("HTTP response:" + response.responseCode());
                            return;
                        }
                        JsonResponse<UserInfoEntity> jsonResponse = response.body();
                        if (jsonResponse == null) {
                            failure("JsonResponse is null");
                            return;
                        }
                        UserInfoEntity userInfo = jsonResponse.getData();
                        if (jsonResponse.getStatus() != 200) {
                            errorMessage.setResult(jsonResponse.getMessage());
                            failure("Status ==" + jsonResponse.getStatus());
                            return;
                        }
                        if (userInfo == null) {
                            failure("userInfo is null");
                            return;
                        }

                        String token = userInfo.getToken();
                        String secretKey = userInfo.getSecretKey();
                        UserEntity userEntity = userInfo.getUserProfile();
                        ArrayList<ContactEntity> contacts = userInfo.getContacts();
                        ArrayList<GroupEntity> groups = userInfo.getGroups();
                        if (userEntity == null || userEntity.isEmpty() || TextUtils.isEmpty(secretKey) || TextUtils.isEmpty(token)) {
                            failure("server error : user result is empty");
                            return;
                        }
                        mDBHelper = new DBHelper(sAppContext, MD5Util.encrypt32(userEntity.getUserID()), Constants.DATABASE_VERSION);
                        boolean isUpdateSuccess = true;
                        if (!UserManager.updateLocal(token, userEntity, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("update token and user fail");
                        } else if (!ConfigurationManager.update(secretKey)) {
                            isUpdateSuccess = false;
                            LogUtil.e("update secretKey fail");
                        } else if (!ContactManager.update(contacts, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("update contacts fail");
                        } else if (!GroupManager.update(groups, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("update groups fail");
                        }
                        if (!isUpdateSuccess) {
                            failure("client error : update user info fail");
                        } else {
                            if (initFromLocal()) {
                                loginIMServer(token);
                            } else {
                                failure("init im fail from local");
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        String userID = UserManager.getLocalUserID();
                        String token = UserManager.getLocalToken();
                        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(token)) {
                            failure("get userID or token is null from local");
                            return;
                        }
                        mDBHelper = new DBHelper(sAppContext, MD5Util.encrypt32(userID), Constants.DATABASE_VERSION);
                        if (initFromLocal()) {
                            loginIMServer(token);
                        } else {
                            failure("init im fail from local");
                        }
                    }

                    private void loginIMServer(String token) {
                        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                            @Override
                            public void onTokenIncorrect() {
                                failure("token is incorrect");
                            }

                            @Override
                            public void onSuccess(String s) {
                                success(true);
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
                                        failure("login IM server fail:" + errorCode.getMessage());
                                        break;
                                    default:
                                        success(false);
                                }
                            }
                        });
                    }

                    private void success(boolean isConnectedToServer) {
                        mChatManager = new ChatManager(AppClient.this);
                        mContactManager = new ContactManager(AppClient.this);
                        mGroupManager = new GroupManager(AppClient.this);
                        mChatManager = new ChatManager(AppClient.this);
                        mConversationManager = new ConversationManager();
                        mContactManager.updateContactUnreadCount();
                        mConversationManager.updateChatUnreadCount();
                        latch.countDown();
                    }

                    private void failure(String error) {
                        LogUtil.e(error);
                        result.setResult(false);
                        latch.countDown();
                    }

                    @Override
                    public boolean isExecuteNextTask() {
                        return false;
                    }
                }, false);

                HttpExecutor.getInstance().submit(loginOrRegisterOrTokenVerifyCall);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (result.getResult()) {
                    isLogged = true;
                    resultCallback.onResult(null);
                } else {
                    isLogged = false;
                    logout();
                    resultCallback.onFailure(errorMessage.getResult());
                }
            }
        });
    }

    private boolean initFromLocal() {
        mUserManager = UserManager.getInstanceFromLocal(mDBHelper.getReadWriteHelper());
        if (mUserManager == null) {
            return false;
        }
        mConfigurationManager = ConfigurationManager.getInstanceFromLocal();
        if (mConfigurationManager == null) {
            return false;
        }
        return true;
    }

    public synchronized void logout() {
        isLogged = false;
        mRongIMClient.logout();
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
        if (mWorkExecutor != null) {
            mWorkExecutor.getQueue().clear();
        }
        if (mChatManager != null) {
            mChatManager.destroy();
            mChatManager = null;
        }
        if (mContactManager != null) {
            mContactManager.destroy();
            mContactManager = null;
        }
        if (mGroupManager != null) {
            mGroupManager.destroy();
            mGroupManager = null;
        }
        if (mConversationManager != null) {
            mConversationManager.destroy();
            mConversationManager = null;
        }
        if (mUserManager != null) {
            mUserManager.destroy();
            mUserManager = null;
        }
        if (mDBHelper != null) {
            mDBHelper.destroy();
            mDBHelper = null;
        }
        mUserManager = null;
        mConfigurationManager = null;
        SharePreferenceManager.getIdentityPreferences().clear(false);
    }

    public boolean isLogged() {
        return isLogged;
    }

    public boolean isConnected() {
        return mRongIMClient.getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED;
    }

    public Context getAppContext() {
        return mAppContext;
    }

    public RongIMClient getRongIMClient() {
        return mRongIMClient;
    }

    @Override
    public ChatManager getChatManager() {
        return mChatManager;
    }

    @Override
    public ContactManager getContactManager() {
        return mContactManager;
    }

    @Override
    public GroupManager getGroupManager() {
        return mGroupManager;
    }

    @Override
    public ConversationManager getConversationManager() {
        return mConversationManager;
    }

    @Override
    public UserManager getUserManager() {
        return mUserManager;
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return mConfigurationManager;
    }

    @Override
    public AbstractDao.ReadWriteHelper getReadWriteHelper() {
        return mDBHelper.getReadWriteHelper();
    }

    @Override
    public void runOnUiThread(Runnable runnable) {
        mUIHandler.post(runnable);
    }

    @Override
    public void runOnWorkThread(Runnable runnable) {
        mWorkExecutor.execute(runnable);
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

