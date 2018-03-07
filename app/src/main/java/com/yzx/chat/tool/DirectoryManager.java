package com.yzx.chat.tool;

import com.yzx.chat.configure.AppApplication;

import java.io.File;

/**
 * Created by YZX on 2017年12月11日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class DirectoryManager {

    private static final String PRIVATE_DATA_BASE_PATH = AppApplication.getAppContext().getFilesDir().getPath();

    private static final String PATH_VOICE_RECORDER = "/VoiceRecorder/";

    public static void init() {
        File file;
        file = new File(PRIVATE_DATA_BASE_PATH + PATH_VOICE_RECORDER);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getVoiceRecorderPath() {
        return PRIVATE_DATA_BASE_PATH + PATH_VOICE_RECORDER;
    }
}
