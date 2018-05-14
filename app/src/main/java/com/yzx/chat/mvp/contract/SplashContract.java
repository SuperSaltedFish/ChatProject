package com.yzx.chat.mvp.contract;

import android.content.Context;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.tool.DirectoryManager;

/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class SplashContract {

    public interface View extends BaseView<Presenter> {
        void startLoginActivity();
        void startHomeActivity();
        Context getContext();
        void error(String error);

    }


    public interface Presenter extends BasePresenter<View> {

        void checkLogin();

    }
}
