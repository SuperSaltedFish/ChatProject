package com.yzx.chat.core.listener;

/**
 * Created by YZX on 2018年03月06日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class Result<T> {
    private T t;

    public Result(T t) {
        this.t = t;
    }

    public T getResult() {
        return t;
    }

    public void setResult(T t) {
        this.t = t;
    }
}
