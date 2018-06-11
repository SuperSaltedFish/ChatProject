package com.yzx.chat.network.framework;

/**
 * Created by YZX on 2018年06月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class NetworkUnavailableException extends Exception{
    public NetworkUnavailableException() {
        super();
    }

    public NetworkUnavailableException(String message) {
        super(message);
    }

    public NetworkUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkUnavailableException(Throwable cause) {
        super(cause);
    }
}
