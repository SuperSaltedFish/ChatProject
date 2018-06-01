package com.yzx.chat.tool;

import android.os.Environment;
import android.text.TextUtils;

import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.util.MD5Util;

import java.io.File;

/**
 * Created by YZX on 2017年12月11日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class DirectoryHelper {

    public static final String PUBLIC_DATA_BASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Chat";

    private static final String PRIVATE_DATA_BASE_PATH = AppApplication.getAppContext().getFilesDir().getPath();

    private static final String PROTECTED_DATA_BASE_PATH = AppApplication.getAppContext().getExternalFilesDir(null).getPath();

    private static final String PATH_VOICE_RECORDER = "/VoiceRecorder/";

    private static final String PATH_TEMP = "/temp/";

    private static final String PATH_IMAGE = "/image/";

    private static final String PATH_VIDEO = "/Video/";

    private static final String PATH_THUMBNAIL = "/Thumbnail/";

    private static String sUserDirectory;

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
        file = new File(getPublicThumbnailPath());
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void initUserDirectory(String userID) {
        String md5 = MD5Util.encrypt32(userID);
        if (TextUtils.isEmpty(md5)) {
            throw new IllegalArgumentException("userID is null");
        }
        sUserDirectory = "/" + md5;
        File file;
        file = new File(getPrivateUserVoiceRecorderPath());
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(getUserImagePath());
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(getUserVideoPath());
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getPrivateUserVoiceRecorderPath() {
        if (sUserDirectory == null) {
            throw new RuntimeException("UserDirectory is not initialized");
        }
        return PRIVATE_DATA_BASE_PATH + sUserDirectory + PATH_VOICE_RECORDER;
    }

    public static String getUserImagePath() {
        if (sUserDirectory == null) {
            throw new RuntimeException("UserDirectory is not initialized");
        }
        return PUBLIC_DATA_BASE_PATH + sUserDirectory + PATH_IMAGE;
    }

    public static String getUserVideoPath() {
        if (sUserDirectory == null) {
            throw new RuntimeException("UserDirectory is not initialized");
        }
        return PUBLIC_DATA_BASE_PATH + sUserDirectory + PATH_VIDEO;
    }

    public static String getProtectedTempPath() {
        return PROTECTED_DATA_BASE_PATH + PATH_TEMP;
    }

    public static String getPublicTempPath() {
        return PUBLIC_DATA_BASE_PATH + PATH_TEMP;
    }

    public static String getPublicThumbnailPath() {
        return PUBLIC_DATA_BASE_PATH + PATH_THUMBNAIL;
    }

}
