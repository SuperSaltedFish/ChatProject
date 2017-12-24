package com.yzx.chat.presenter;

import android.os.Handler;
import android.os.Looper;

import com.yzx.chat.contract.HomeContract;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.util.LogUtil;

import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View mHomeView;
    private Handler mHandler;
    private ChatClientManager mChatClientManager;

    @Override
    public void attachView(HomeContract.View view) {
        mHomeView = view;
        mHandler = new Handler();
        mChatClientManager = ChatClientManager.getInstance();
        mChatClientManager.addOnMessageReceiveListener(mChatMessageReceiveListener, null);
        mChatClientManager.addContactListener(mOnContactMessageReceiveListener);
    }

    @Override
    public void detachView() {
        mChatClientManager.removeOnMessageReceiveListener(mChatMessageReceiveListener);
        mChatClientManager.removeContactListener(mOnContactMessageReceiveListener);
        mHandler.removeCallbacksAndMessages(null);
        mHomeView = null;
        mChatClientManager = null;
        mHandler = null;
    }


    @Override
    public void loadUnreadCount() {

    }

    private final ChatClientManager.OnChatMessageReceiveListener mChatMessageReceiveListener = new ChatClientManager.OnChatMessageReceiveListener() {
        @Override
        public void onChatMessageReceived(Message message, int untreatedCount) {

        }
    };

    private final ChatClientManager.OnContactMessageReceiveListener mOnContactMessageReceiveListener = new ChatClientManager.OnContactMessageReceiveListener() {
        @Override
        public void onContactMessageReceive(ContactNotificationMessage message) {

        }
    };
}
