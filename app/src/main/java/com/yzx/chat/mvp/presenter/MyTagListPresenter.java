package com.yzx.chat.mvp.presenter;

import com.yzx.chat.mvp.contract.MyTagListContract;
import com.yzx.chat.network.chat.IMClient;

/**
 * Created by YZX on 2018年06月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MyTagListPresenter implements MyTagListContract.Presenter {

    private MyTagListContract.View mMyTagListView;

    @Override
    public void attachView(MyTagListContract.View view) {
        mMyTagListView = view;
    }

    @Override
    public void detachView() {
        mMyTagListView = null;
    }

    @Override
    public void loadAllTagList() {
        mMyTagListView.showAllTags(IMClient.getInstance().getContactManager().getAllTagAndMemberCount());
    }


}
