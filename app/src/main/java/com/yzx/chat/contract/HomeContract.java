package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class HomeContract {

    public interface View extends BaseView<Presenter> {
        void updateMessageUnreadBadge(int count);

        void updateContactUnreadBadge(int count);
    }


    public interface Presenter extends BasePresenter<View> {

    }
}
