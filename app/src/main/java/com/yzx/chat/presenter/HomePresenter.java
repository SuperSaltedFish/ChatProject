package com.yzx.chat.presenter;

import android.os.Handler;

import com.yzx.chat.contract.HomeContract;
import com.yzx.chat.network.chat.ChatManager;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View mHomeView;
    private Handler mHandler;
    private IMClient mIMClient;

    @Override
    public void attachView(HomeContract.View view) {
        mHomeView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.chatManager().addChatMessageUnreadCountChangeListener(mOnChatMessageUnreadCountChangeListener);
        mIMClient.contactManager().addContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
    }

    @Override
    public void detachView() {
        mIMClient.chatManager().removeChatMessageUnreadCountChangeListener(mOnChatMessageUnreadCountChangeListener);
        mIMClient.contactManager().removeContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mHomeView = null;
        mIMClient = null;
        mHandler = null;
    }


    @Override
    public void loadUnreadCount() {
        mIMClient.chatManager().updateChatUnreadCount();
        mIMClient.contactManager().updateContactUnreadCount();
    }

    private final ChatManager.OnChatMessageUnreadCountChangeListener mOnChatMessageUnreadCountChangeListener = new ChatManager.OnChatMessageUnreadCountChangeListener() {
        @Override
        public void onChatMessageUnreadCountChange(final int count) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHomeView.updateMessageUnreadBadge(count);
                }
            });
        }
    };

    private final ContactManager.OnContactOperationUnreadCountChangeListener mOnContactOperationUnreadCountChangeListener = new ContactManager.OnContactOperationUnreadCountChangeListener() {
        @Override
        public void onContactOperationUnreadCountChange(final int count) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHomeView.updateContactUnreadBadge(count);
                }
            });
        }
    };

}
