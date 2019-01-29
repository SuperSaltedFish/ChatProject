package com.yzx.chat.util;

import com.yzx.chat.core.net.framework.Call;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class AsyncUtil {

    public static void cancelTask(BackstageAsyncTask task){
        if(task!=null){
            task.cancel();
        }
    }

    public static void cancelCall(Call call){
        if(call!=null){
            call.cancel();
        }
    }

    public static void cancelResult(AsyncResult call){
        if(call!=null){
            call.cancel();
        }
    }
}
