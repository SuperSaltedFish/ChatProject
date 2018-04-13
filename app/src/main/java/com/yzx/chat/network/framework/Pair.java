package com.yzx.chat.network.framework;

import java.util.Objects;

/**
 * Created by YZX on 2018年04月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class Pair<F, S> {

    public final F key;
    public final S value;


    public Pair(F key, S value) {
        this.key = key;
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equals(p.key, key) && Objects.equals(p.value, value);
    }


    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return "Pair{" + String.valueOf(key) + " " + String.valueOf(value) + "}";
    }


    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<A, B>(a, b);
    }

}
