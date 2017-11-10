package com.yzx.chat.util;

import com.yzx.chat.network.chat.NetworkAsyncTask;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class NetworkUtil {

    public static void cancel(NetworkAsyncTask task){
        if(task!=null){
            task.cancel();
        }
    }
}
