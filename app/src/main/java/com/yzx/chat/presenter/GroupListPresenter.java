package com.yzx.chat.presenter;

import com.yzx.chat.contract.GroupListContract;
import com.yzx.chat.network.chat.IMClient;

/**
 * Created by YZX on 2018年03月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class GroupListPresenter implements GroupListContract.Presenter {

    private GroupListContract.View mGroupListView;

    @Override
    public void attachView(GroupListContract.View view) {
        mGroupListView = view;
    }

    @Override
    public void detachView() {
        mGroupListView = null;
    }

    @Override
    public void loadAllGroup() {
        mGroupListView.updateContactListView(IMClient.getInstance().groupManager().getAllGroup());
    }
}
