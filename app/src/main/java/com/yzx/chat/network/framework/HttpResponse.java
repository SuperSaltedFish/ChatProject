package com.yzx.chat.network.framework;


public interface HttpResponse<T> {

    int responseCode();

    T body();

}
