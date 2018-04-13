package com.yzx.chat.mvp.contract;

import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.GroupBean;

import java.util.List;

/**
 * Created by YZX on 2018年03月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class GroupListContract {

    public interface View extends BaseView<GroupListContract.Presenter> {

        void showGroupList(DiffUtil.DiffResult diffResult, List<GroupBean> groupList);

        void refreshGroupItem(GroupBean group);

        void removeGroupItem(GroupBean group);
    }


    public interface Presenter extends BasePresenter<GroupListContract.View> {
        void loadAllGroup();
    }
}
