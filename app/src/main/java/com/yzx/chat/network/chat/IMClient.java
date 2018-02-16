package com.yzx.chat.network.chat;


import android.content.Context;

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

    public static void init(Context appContext, String imAppKey) {
        RongIMClient.init(appContext, imAppKey);
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
    private List<onConnectionStateChangeListener> mOnConnectionStateChangeListenerList;
    private ThreadPoolExecutor mWorkExecutor;

    private IMClient() {
        mRongIMClient = RongIMClient.getInstance();
        RongIMClient.setOnReceiveMessageListener(mOnReceiveMessageListener);
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);

        mOnConnectionStateChangeListenerList = Collections.synchronizedList(new LinkedList<onConnectionStateChangeListener>());
        mChatManager = new ChatManager(mSubManagerCallback);
        mContactManager = new ContactManager(mSubManagerCallback);
        mConversationManager = new ConversationManager(mSubManagerCallback);

        mWorkExecutor = new ThreadPoolExecutor(
                0,
                2,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(32));
    }

    public void login(String token, RongIMClient.ConnectCallback callback) {
        RongIMClient.connect(token, callback);
    }

    public void logout() {
        mRongIMClient.logout();
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

    public void addConnectionListener(onConnectionStateChangeListener listener) {
        if (!mOnConnectionStateChangeListenerList.contains(listener)) {
            mOnConnectionStateChangeListenerList.add(listener);
            if (isConnected()) {
                listener.onConnected();
            } else {
                listener.onDisconnected(mRongIMClient.getCurrentConnectionStatus().getMessage());
            }
        }
    }

    public void removeConnectionListener(onConnectionStateChangeListener listener) {
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
            for (onConnectionStateChangeListener listener : mOnConnectionStateChangeListenerList) {
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

    public interface onConnectionStateChangeListener {

        void onConnected();

        void onDisconnected(String reason);

    }

    interface SubManagerCallback {

        void chatManagerCallback(int callbackCode, Object arg);

        void conversationManagerCallback(int callbackCode, Object arg);

        void contactManagerCallback(int callbackCode, Object arg);

        void execute(Runnable runnable);
    }
}
