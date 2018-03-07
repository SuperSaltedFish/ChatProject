package com.yzx.chat.base;


import com.yzx.chat.network.framework.NetworkExecutor;

/**
 * Created by YZX on 2017年10月18日.
 * 其实你找不到错误不代表错误不存在，同样你看不到技术比你牛的人并不代表世界上没有技术比你牛的人。
 */


public interface BasePresenter<V extends BaseView> {

    NetworkExecutor sHttpExecutor = NetworkExecutor.getInstance();

    void attachView(V v);

    void detachView();
}
