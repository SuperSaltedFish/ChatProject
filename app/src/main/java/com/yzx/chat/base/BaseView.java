package com.yzx.chat.base;


import cn.swiftpass.standardwallet.core.listener.Cancelable;

/**
 * Created by YZX on 2017年10月18日.
 * 其实你找不到错误不代表错误不存在，同样你看不到技术比你牛的人并不代表世界上没有技术比你牛的人。
 */


public interface BaseView<P> {

    P getPresenter();

    void setEnableLoading(boolean isEnable);

    void setEnableLoading(boolean isEnable, Cancelable cancelableTask);

    void showErrorDialog(String error);

    void showToast(String content);

    boolean isAttachedToPresenter();
}
