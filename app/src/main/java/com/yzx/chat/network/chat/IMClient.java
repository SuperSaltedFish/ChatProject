package com.yzx.chat.network.chat;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.DBHelper;
import com.yzx.chat.network.api.JsonRequest;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.UserInfoBean;
import com.yzx.chat.network.chat.extra.VideoMessage;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.ResponseCallback;
import com.yzx.chat.network.framework.HttpDataFormatAdapter;
import com.yzx.chat.network.framework.HttpParamsType;
import com.yzx.chat.network.framework.HttpRequest;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.tool.SharePreferenceManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.MD5Util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class IMClient {

    static final Conversation.ConversationType[] SUPPORT_CONVERSATION_TYPE = {Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP};


    private static IMClient sIMClient;
    private static Context sAppContext;

    public static void init(Context context) {
        sAppContext = context.getApplicationContext();
        RongIMClient.init(sAppContext);
        sIMClient = new IMClient();
    }

    public static IMClient getInstance() {
        if (sIMClient == null) {
            throw new RuntimeException("ChatClientManager is not initialized");
        }
        return sIMClient;
    }


    private RongIMClient mRongIMClient;
    private UserManager mUserManager;
    private CryptoManager mCryptoManager;
    private ChatManager mChatManager;
    private ContactManager mContactManager;
    private GroupManager mGroupManager;
    private ConversationManager mConversationManager;
    private ThreadPoolExecutor mWorkExecutor;
    private DBHelper mDBHelper;
    private Handler mUiHandler;
    private List<OnConnectionStateChangeListener> mOnConnectionStateChangeListenerList;

    private boolean isLogged;

    private IMClient() {
        mRongIMClient = RongIMClient.getInstance();
        RongIMClient.setOnReceiveMessageListener(mOnReceiveMessageListener);
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);
        try {
            RongIMClient.registerMessageType(VideoMessage.class);
        } catch (AnnotationNotFoundException ignored) {
        }
        mUiHandler = new Handler(Looper.getMainLooper());
        mOnConnectionStateChangeListenerList = Collections.synchronizedList(new LinkedList<OnConnectionStateChangeListener>());
        mWorkExecutor = new ThreadPoolExecutor(
                0,
                2,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(32));
    }


    public void loginByToken(Call<JsonResponse<UserInfoBean>> loginOrRegisterOrTokenVerifyCall, final ResultCallback<Void> resultCallback) {
        loginOrRegisterOrTokenVerifyCall.setHttpDataFormatAdapter(new HttpDataFormatAdapter() {
            private Gson mGson = ApiHelper.getDefaultGsonInstance();

            @Nullable
            @Override
            public String paramsToString(HttpRequest httpRequest) {
                Map<String, Object> params = httpRequest.params().get(HttpParamsType.PARAMETER_HTTP);
                JsonRequest request = new JsonRequest();
                request.setParams(params);
                request.setStatus(200);
                request.setToken(UserManager.getLocalToken());
                String json = mGson.toJson(request);
                LogUtil.e("request: " + json);
                if (json != null) {
                    return json;
                }
                return null;
            }

            @Nullable
            @Override
            public Map<HttpParamsType, Map<String, Object>> multiParamsFormat(HttpRequest httpRequest) {
                return null;
            }

            @Nullable
            @Override
            public Object responseToObject(String url, String httpResponse, Type genericType) {
                try {
                    LogUtil.e("response: " + httpResponse);
                    return mGson.fromJson(httpResponse, genericType);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        login(loginOrRegisterOrTokenVerifyCall, resultCallback);
    }


    public void login(final Call<JsonResponse<UserInfoBean>> loginOrRegisterOrTokenVerifyCall, final ResultCallback<Void> resultCallback) {
        if (isLogged) {
            throw new RuntimeException("The user has already logged in, please do not log in again！");
        }
        mWorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final CountDownLatch latch = new CountDownLatch(1);
                final Result<Boolean> result = new Result<>(true);
                final Result<String> errorMessage = new Result<>(AndroidUtil.getString(R.string.Server_Error));
                loginOrRegisterOrTokenVerifyCall.setResponseCallback(new ResponseCallback<JsonResponse<UserInfoBean>>() {
                    @Override
                    public void onResponse(HttpResponse<JsonResponse<UserInfoBean>> response) {
                        if (response.getResponseCode() != 200) {
                            failure("HTTP response:" + response.getResponseCode());
                            return;
                        }
                        JsonResponse<UserInfoBean> jsonResponse = response.getResponse();
                        if (jsonResponse == null) {
                            failure("JsonResponse is null");
                            return;
                        }
                        UserInfoBean userInfo = jsonResponse.getData();
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
                        UserBean userBean = userInfo.getUserProfile();
                        ArrayList<ContactBean> contacts = userInfo.getContacts();
                        ArrayList<GroupBean> groups = userInfo.getGroups();
                        if (userBean == null || userBean.isEmpty() || TextUtils.isEmpty(secretKey) || TextUtils.isEmpty(token)) {
                            failure("server error : user result is empty");
                            return;
                        }
                        mDBHelper = new DBHelper(sAppContext, MD5Util.encrypt32(userBean.getUserID()), Constants.DATABASE_VERSION);
                        boolean isUpdateSuccess = true;
                        if (!UserManager.update(token, userBean, mDBHelper.getReadWriteHelper())) {
                            isUpdateSuccess = false;
                            LogUtil.e("update token and user fail");
                        } else if (!CryptoManager.update(secretKey)) {
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
                        mChatManager = new ChatManager(mCallbackHelper);
                        mConversationManager = new ConversationManager(mCallbackHelper);
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

                NetworkExecutor.getInstance().submit(loginOrRegisterOrTokenVerifyCall);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (result.getResult()) {
                    isLogged = true;
                    resultCallback.onSuccess(null);
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
        mCryptoManager = CryptoManager.getInstanceFromLocal();
        if (mCryptoManager == null) {
            return false;
        }
        mContactManager = new ContactManager(mCallbackHelper, mDBHelper.getReadWriteHelper());
        mGroupManager = new GroupManager(mCallbackHelper, mDBHelper.getReadWriteHelper());
        mChatManager = new ChatManager(mCallbackHelper);
        mConversationManager = new ConversationManager(mCallbackHelper);
        return true;
    }

    public synchronized void logout() {
        isLogged = false;
        mRongIMClient.logout();
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
        mCryptoManager = null;
        SharePreferenceManager.getIdentityPreferences().clear(false);
    }

    public boolean isLogged() {
        return isLogged;
    }

    public boolean isConnected() {
        return mRongIMClient.getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED;
    }

    public UserManager userManager() {
        return mUserManager;
    }

    public ChatManager chatManager() {
        return mChatManager;
    }

    public ContactManager contactManager() {
        return mContactManager;
    }

    public GroupManager groupManager() {
        return mGroupManager;
    }

    public ConversationManager conversationManager() {
        return mConversationManager;
    }

    public CryptoManager cryptoManager() {
        return mCryptoManager;
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


    private final CallbackHelper mCallbackHelper = new CallbackHelper() {
        @Override
        public void callChatManager(int callbackCode, Object arg) {

        }

        @Override
        public void callConversationManager(int code, Object arg) {
            switch (code) {
                case ConversationManager.CALLBACK_CODE_UPDATE_UNREAD:
                    mChatManager.updateChatUnreadCount();
                    break;
                case ConversationManager.CALLBACK_CODE_ClEAR_AND_REMOVE_PRIVATE:
                    String contactID = (String) arg;
                    if (!TextUtils.isEmpty(contactID)) {
                        Conversation conversation = mConversationManager.getConversation(Conversation.ConversationType.PRIVATE, contactID);
                        if (conversation != null) {
                            mConversationManager.removeConversation(conversation, false);
                            mConversationManager.clearAllConversationMessages(conversation);
                        }
                    }
                    break;
                case ConversationManager.CALLBACK_CODE_ClEAR_AND_REMOVE_GROUP:
                    String groupID = (String) arg;
                    if (!TextUtils.isEmpty(groupID)) {
                        Conversation conversation = mConversationManager.getConversation(Conversation.ConversationType.GROUP, groupID);
                        if (conversation != null) {
                            mConversationManager.removeConversation(conversation, false);
                            mConversationManager.clearAllConversationMessages(conversation);
                        }
                    }
                    break;
                case ConversationManager.CALLBACK_CODE_UPDATE_PRIVATE:
                    ContactBean contact = (ContactBean) arg;
                    Conversation privateConversation = mConversationManager.getConversation(Conversation.ConversationType.PRIVATE, contact.getUserProfile().getUserID());
                    if (privateConversation != null) {
                        privateConversation.setConversationTitle(contact.getName());
                        mConversationManager.updateConversation(privateConversation, null);
                    }
                    break;
                case ConversationManager.CALLBACK_CODE_UPDATE_GROUP:
                    GroupBean group = (GroupBean) arg;
                    Conversation groupConversation = mConversationManager.getConversation(Conversation.ConversationType.GROUP, group.getGroupID());
                    if (groupConversation != null) {
                        groupConversation.setConversationTitle(group.getName());
                        mConversationManager.updateConversation(groupConversation, null);
                    }
                    break;
            }
        }

        @Override
        public void callContactManager(int code, Object arg) {

        }

        @Override
        public void callGroupManager(int code, Object arg) {

        }

        @Override
        public AbstractDao.ReadWriteHelper getDatabaseReadWriteHelper() {
            return mDBHelper.getReadWriteHelper();
        }

        @Override
        public void runOnBackgroundThread(Runnable runnable) {
            mWorkExecutor.execute(runnable);
        }

        @Override
        public void runOnUiThread(Runnable runnable) {
            mUiHandler.post(runnable);
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
                case "Custom:VideoMsg":
                    mChatManager.onReceiveContactNotificationMessage(message, i);
                    break;
                case "RC:ContactNtf":
                    mContactManager.onReceiveContactNotificationMessage(message);
                    break;
                case "RC:GrpNtf":
                    mGroupManager.onReceiveGroupNotificationMessage(message);
                    break;
            }
            if (i == 0) {
                mChatManager.updateChatUnreadCount();
                mContactManager.updateContactUnreadCount();
            }
            return true;
        }
    };

    public interface OnConnectionStateChangeListener {

        void onConnected();

        void onDisconnected(String reason);

    }

    interface CallbackHelper {

        void callChatManager(int callbackCode, Object arg);

        void callConversationManager(int callbackCode, Object arg);

        void callContactManager(int callbackCode, Object arg);

        void callGroupManager(int callbackCode, Object arg);

        AbstractDao.ReadWriteHelper getDatabaseReadWriteHelper();

        void runOnBackgroundThread(Runnable runnable);

        void runOnUiThread(Runnable runnable);
    }
}

