package com.yzx.chat.tool;

import android.os.Environment;

import com.yzx.chat.configure.AppApplication;

import java.io.File;

/**
 * Created by YZX on 2017年12月11日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class DirectoryManager {

    private static final String PRIVATE_DATA_BASE_PATH = AppApplication.getAppContext().getFilesDir().getPath();

    private static final String PROTECTED_DATA_BASE_PATH = AppApplication.getAppContext().getExternalFilesDir(null).getPath();

    private static final String PUBLIC_DATA_BASE_PATH = Environment.getExternalStorageDirectory().getPath()+"/Chat";

    private static final String PATH_VOICE_RECORDER = "/VoiceRecorder/";

    private static final String PATH_TEMP = "/temp/";

    private static final String PATH_IMAGE = "/image/";

    private static final String PATH_VIDEO = "/Video/";

    public static void init() {
        File file;
        file = new File(PRIVATE_DATA_BASE_PATH + PATH_VOICE_RECORDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(PROTECTED_DATA_BASE_PATH + PATH_TEMP);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(PUBLIC_DATA_BASE_PATH + PATH_IMAGE);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(PUBLIC_DATA_BASE_PATH + PATH_VIDEO);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getPrivateVoiceRecorderPath() {
        return PRIVATE_DATA_BASE_PATH + PATH_VOICE_RECORDER;
    }

    public static String getProtectedTempPath() {
        return PRIVATE_DATA_BASE_PATH + PATH_TEMP;
    }

    public static String getPublicImagePath() {
        return PUBLIC_DATA_BASE_PATH + PATH_IMAGE;
    }

    public static String getPublicVideoPath() {
        return PUBLIC_DATA_BASE_PATH + PATH_VIDEO;
    }
}
