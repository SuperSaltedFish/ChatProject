package com.yzx.chat.core.net.framework;

/**
 * Created by YZX on 2019年01月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ResponseException extends Throwable{
    public ResponseException(String error) {
        super(error);
    }
}
