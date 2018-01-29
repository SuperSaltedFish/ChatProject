package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2018年01月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class StrangerProfileContract {

    public interface View extends BaseView<Presenter> {
        void goBack();
    }


    public interface Presenter extends BasePresenter<View> {

        void requestContact(String userID,String verifyContent);
    }
}
