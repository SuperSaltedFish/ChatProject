package com.yzx.chat.tool;

import android.os.Environment;
import android.text.TextUtils;

import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.core.util.MD5Util;

import java.io.File;

/**
 * Created by YZX on 2017年12月11日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class DirectoryHelper {

    public static final String PUBLIC_DATA_BASE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "MyChat";

    private static final String PRIVATE_DATA_BASE_PATH = AppApplication.getAppContext().getFilesDir().getPath();

    private static final String PROTECTED_DATA_BASE_PATH = AppApplication.getAppContext().getExternalFilesDir(null).getPath();

    private static final String PATH_VOICE_RECORDER = "/VoiceRecorder";

    private static final String PATH_TEMP = File.separator + "Temp";

    private static final String PATH_IMAGE = File.separator + "Image";

    private static final String PATH_VIDEO = File.separator + "Video";

    private static final String PATH_THUMBNAIL = File.separator + "Thumbnail";


    private static String createDirectoryIfNeed(String rootPath, String subPath, String userID) {
        File file;
        if (TextUtils.isEmpty(userID)) {
            userID = "unknown";
        } else {
            userID = MD5Util.encrypt16(userID);
        }
        String path = rootPath + subPath + File.separator + userID;
        file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                LogUtil.e("Create path fail: " + path);
            }
        }
        return path;
    }

    public static String getVoiceRecorderPath() {
        return createDirectoryIfNeed(PRIVATE_DATA_BASE_PATH, PATH_VOICE_RECORDER, AppClient.getInstance().getUserID());
    }

    public static String getImagePath() {
        return createDirectoryIfNeed(PROTECTED_DATA_BASE_PATH, PATH_IMAGE, AppClient.getInstance().getUserID());
    }

    public static String getVideoPath() {
        return createDirectoryIfNeed(PUBLIC_DATA_BASE_PATH, PATH_VIDEO, AppClient.getInstance().getUserID());
    }

    public static String getProtectedTempPath() {
        return createDirectoryIfNeed(PROTECTED_DATA_BASE_PATH, PATH_TEMP, AppClient.getInstance().getUserID());
    }

    public static String getPublicTempPath() {
        return createDirectoryIfNeed(PUBLIC_DATA_BASE_PATH, PATH_TEMP, AppClient.getInstance().getUserID());
    }

    public static String getProtectedThumbnailPath() {
        return createDirectoryIfNeed(PROTECTED_DATA_BASE_PATH, PATH_THUMBNAIL, AppClient.getInstance().getUserID());
    }

}
