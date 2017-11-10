package com.yzx.chat.util;

import com.yzx.chat.network.framework.Call;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class HttpCallUtil {
    public static void cancel(Call call){
        if(call!=null){
            call.cancel();
        }
    }
}
