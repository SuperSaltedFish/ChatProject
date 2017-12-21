package com.yzx.chat.presenter;

import android.os.Handler;

import com.yzx.chat.contract.HomeContract;
import com.yzx.chat.tool.ChatClientManager;

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
    }

    @Override
    public void detachView() {
        mHandler.removeCallbacksAndMessages(null);
        mHomeView = null;
        mChatClientManager = null;
        mHandler = null;
    }

    private final ChatClientManager.UnreadCountChangeListener mUnreadCountChangeListener = new ChatClientManager.UnreadCountChangeListener() {
        @Override
        public void onMessageUnreadCountChange(final int unreadCount) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHomeView.updateMessageUnreadBadge(unreadCount);
                }
            });
        }

        @Override
        public void onContactUnreadCountChange(final int unreadCount) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHomeView.updateContactUnreadBadge(unreadCount);
                }
            });
        }
    };

    @Override
    public void loadUnreadCount() {

    }
}
