package com.yzx.chat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by YZX on 2017年11月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ChatMessageReadReceive extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

    }

    public interface OnMessageReadChange{
       void OnMessageRead();
       void OnMessageUnRead();
    }
}
