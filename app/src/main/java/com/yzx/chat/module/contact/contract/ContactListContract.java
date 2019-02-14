package com.yzx.chat.module.contact.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.ContactEntity;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ContactListContract {

    public interface View extends BaseView<Presenter> {
        void updateUnreadBadge(int unreadCount);

        void updateContactItem(ContactEntity contactEntity);

        void updateContactListView(DiffUtil.DiffResult diffResult, List<ContactEntity> newFriendList);

        void showTagCount(int count);
    }


    public interface Presenter extends BasePresenter<View> {
        void loadUnreadCount();

        void loadAllContact();

        void loadTagCount();

    }
}
