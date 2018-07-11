package com.yzx.chat.mvp.contract;

import android.content.Context;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class SplashContract {

    public interface View extends BaseView<Presenter> {
        void startLoginActivity();

        void startHomeActivity();

        void startGuide();

        Context getContext();

        void showError(String error);

    }


    public interface Presenter extends BasePresenter<View> {

        void checkLogin();

    }
}
