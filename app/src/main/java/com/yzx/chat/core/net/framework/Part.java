package com.yzx.chat.core.net.framework;

/**
 * Created by YZX on 2018年07月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Part {
    String value();
}
