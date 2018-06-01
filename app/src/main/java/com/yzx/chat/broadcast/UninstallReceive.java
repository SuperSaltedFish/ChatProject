package com.yzx.chat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.yzx.chat.tool.DirectoryHelper;

import java.io.File;

/**
 * Created by YZX on 2018年05月14日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class UninstallReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            Toast.makeText(context, "有应用被替换", Toast.LENGTH_LONG).show();
            deleteDirWithFile(new File(DirectoryHelper.PUBLIC_DATA_BASE_PATH));

        }
    }

    private static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }
}
