package com.yzx.chat.core.net.framework;

/**
 * Created by YZX on 2019年01月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class PartContent {
    private String mContentType;
    private byte[] mContent;

    public String getContentType() {
        return mContentType;
    }

    public void setContentType(String contentType) {
        mContentType = contentType;
    }

    public byte[] getContent() {
        return mContent;
    }

    public void setContent(byte[] content) {
        mContent = content;
    }
}
