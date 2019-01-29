package com.yzx.chat.core.net.framework;


public interface HttpResponse<T> {

    int responseCode();

    T body();

}
