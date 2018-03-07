package com.yzx.chat.contract;

import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactBean;

import java.util.List;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactContract {

    public interface View extends BaseView<Presenter> {
        void updateUnreadBadge(int unreadCount);

        void updateContactItem(ContactBean contactBean);

        void updateContactListView(DiffUtil.DiffResult diffResult, List<ContactBean> newFriendList);
    }


    public interface Presenter extends BasePresenter<View> {
        void loadUnreadCount();

        void loadAllContact();

    }
}
