package com.yzx.chat.core.net.framework.Executor;


import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by YZX on 2019年01月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class PostRequest extends HttpRequest {

    private byte[] mBody;

    public PostRequest(String url, byte[] body) {
        super(url, METHOD_POST);
        mBody = body;
    }

    @Override
    public boolean hasBody() {
        return mBody != null && mBody.length > 0;
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        if (hasBody()) {
            outputStream.write(mBody);
        }
    }
}
