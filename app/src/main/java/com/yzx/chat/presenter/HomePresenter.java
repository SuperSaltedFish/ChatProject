package com.yzx.chat.presenter;

import com.yzx.chat.contract.HomeContract;
import com.yzx.chat.tool.ChatClientManager;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View mHomeView;

    @Override
    public void attachView(HomeContract.View view) {
        mHomeView = view;
        ChatClientManager.getInstance().addUnreadCountChangeListener(mUnreadCountChangeListener);
    }

    @Override
    public void detachView() {
        mHomeView = null;
        ChatClientManager.getInstance().removeUnreadCountChangeListener(mUnreadCountChangeListener);
    }

    private final ChatClientManager.UnreadCountChangeListener mUnreadCountChangeListener = new ChatClientManager.UnreadCountChangeListener() {
        @Override
        public void onUnreadCountChange(int unreadCount) {
            mHomeView.updateUnreadBadge(unreadCount);
        }
    };
}
