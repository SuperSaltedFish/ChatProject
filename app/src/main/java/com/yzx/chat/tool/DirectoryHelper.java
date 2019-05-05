package com.yzx.chat.tool;

import android.os.Environment;
import android.text.TextUtils;

import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.util.MD5Util;

import java.io.File;

/**
 * Created by YZX on 2017年12月11日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class DirectoryHelper {

    public static final String PUBLIC_DATA_BASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/MyChat";

    private static final String PRIVATE_DATA_BASE_PATH = AppApplication.getAppContext().getFilesDir().getPath();

    private static final String PROTECTED_DATA_BASE_PATH = AppApplication.getAppContext().getExternalFilesDir(null).getPath();

    private static final String PATH_VOICE_RECORDER = "/VoiceRecorder/";

    private static final String PATH_TEMP = "/temp/";

    private static final String PATH_IMAGE = "/image/";

    private static final String PATH_VIDEO = "/Video/";

    private static final String PATH_THUMBNAIL = "/Thumbnail/";

    static {
        File file;
        file = new File(getProtectedTempPath());
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(getPublicTempPath());
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(getProtectedThumbnailPath());
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private static String createDirectoryIfNeed(String rootPath, String subPath, String userID) {
        File file;
        if (TextUtils.isEmpty(userID)) {
            file = new File(rootPath + subPath);
        } else {
            userID = MD5Util.encrypt16(userID);
            file = new File(rootPath + File.separator + userID + subPath);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getPath();
    }

    public static String getVoiceRecorderPath() {
        return createDirectoryIfNeed(PRIVATE_DATA_BASE_PATH,PATH_VOICE_RECORDER, AppClient.getInstance().getUserID());
    }

    public static String getImagePath() {
        return createDirectoryIfNeed(PROTECTED_DATA_BASE_PATH,PATH_IMAGE, AppClient.getInstance().getUserID());
    }

    public static String getVideoPath() {
        return createDirectoryIfNeed(PUBLIC_DATA_BASE_PATH,PATH_VIDEO, AppClient.getInstance().getUserID());
    }

    public static String getProtectedTempPath() {
        return PROTECTED_DATA_BASE_PATH + PATH_TEMP;
    }

    public static String getPublicTempPath() {
        return PUBLIC_DATA_BASE_PATH + PATH_TEMP;
    }

    public static String getProtectedThumbnailPath() {
        return PROTECTED_DATA_BASE_PATH + PATH_THUMBNAIL;
    }

}
