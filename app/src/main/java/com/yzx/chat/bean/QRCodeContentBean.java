package com.yzx.chat.bean;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by YZX on 2018年06月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class QRCodeContentBean {
    public static final int TYPE_USER = 1;
    public static final int TYPE_GROUP = 2;

    @IntDef({TYPE_USER, TYPE_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QRCodeType {
    }

    private int type;
    private String id;

    public int getType() {
        return type;
    }

    public void setType(@QRCodeType int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
