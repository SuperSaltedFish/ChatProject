package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2018年02月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class MyQRCodeActivityContract {
    public interface View extends BaseView<Presenter> {

        void showQRCode(String content);

        void showError(String error);
    }


    public interface Presenter extends BasePresenter<View> {

        void updateQRCode();

    }
}
