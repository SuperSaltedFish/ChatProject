package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2017年11月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactContract {

    public interface View extends BaseView<Presenter> {
        void updateUnreadBadge(int unreadCount);
    }


    public interface Presenter extends BasePresenter<View> {
        void loadAllContact();
        void loadUnreadComplete(int count);

    }
}
