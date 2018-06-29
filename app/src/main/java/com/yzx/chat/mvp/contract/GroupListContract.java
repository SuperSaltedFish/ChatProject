package com.yzx.chat.mvp.contract;

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

        void showAllGroupList(List<GroupBean> groupList);

        void showNewGroup(GroupBean group, int position);

        void hideGroup(int position);

        void refreshGroup(GroupBean group, int position);
    }


    public interface Presenter extends BasePresenter<GroupListContract.View> {
        void loadAllGroup();
    }
}
