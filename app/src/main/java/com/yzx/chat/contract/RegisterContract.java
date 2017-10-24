package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2017年10月19日.
 * 其实你找不到错误不代表错误不存在，同样你看不到技术比你牛的人并不代表世界上没有技术比你牛的人。
 */

public class RegisterContract {

    public interface View extends BaseView<Presenter> {


    }


    public interface Presenter extends BasePresenter<View> {
        void login();
    }
}
