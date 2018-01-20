package com.yzx.chat.contract;


import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactMessageBean;

import java.util.List;

/**
 * Created by YZX on 2018年01月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactMessageContract {

    public interface View extends BaseView<Presenter> {
        void addContactMessageToList(ContactMessageBean contactMessage);

        void removeContactMessageFromList(ContactMessageBean contactMessage);

        void updateAllContactMessageList(DiffUtil.DiffResult diffResult, List<ContactMessageBean> newDataList);

        void addMoreContactMessageToList(List<ContactMessageBean> contactMessageList, boolean isHasMore);

        void enableLoadMoreHint(boolean isEnable);
    }


    public interface Presenter extends BasePresenter<View> {

        void init(String userID);

        boolean isLoadingMore();

        boolean hasMoreMessage();

        void removeContactMessage(ContactMessageBean contactMessage);

        void loadAllContactMessage();

        void loadMoreContactMessage(int startID);
    }

}
