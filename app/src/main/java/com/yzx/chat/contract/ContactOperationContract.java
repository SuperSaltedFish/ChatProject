package com.yzx.chat.contract;


import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactOperationBean;

import java.util.List;

/**
 * Created by YZX on 2018年01月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactOperationContract {

    public interface View extends BaseView<Presenter> {
        void addContactOperationToList(ContactOperationBean ContactOperation);

        void removeContactOperationFromList(ContactOperationBean ContactOperation);

        void updateAllContactOperationList(DiffUtil.DiffResult diffResult, List<ContactOperationBean> newDataList);

        void addMoreContactOperationToList(List<ContactOperationBean> ContactOperationList, boolean isHasMore);

        void enableLoadMoreHint(boolean isEnable);
    }


    public interface Presenter extends BasePresenter<View> {

        void init(String userID);

        boolean isLoadingMore();

        boolean hasMoreMessage();

        void removeContactOperation(ContactOperationBean ContactOperation);

        void loadAllContactOperation();

        void loadMoreContactOperation(int startID);
    }

}
