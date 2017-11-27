package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2017年11月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class FindNewContactContract {


    public interface View extends BaseView<Presenter> {

    }


    public interface Presenter extends BasePresenter<View> {
        void loadFriendRequest();
        void acceptFriendRequest(String friendUserID);
        void searchUser(String nicknameOrTelephone);
    }
}
