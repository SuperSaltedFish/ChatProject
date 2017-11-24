package com.yzx.chat.contract;

import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.FriendBean;

import java.util.List;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactContract {

    public interface View extends BaseView<Presenter> {
        void updateUnreadBadge(int unreadCount);
        void updateContactListView(DiffUtil.DiffResult diffResult, List<FriendBean> newFriendList);
    }


    public interface Presenter extends BasePresenter<View> {
        void refreshAllContact(List<FriendBean> oldData);
        void loadUnreadComplete(int count);

    }
}
