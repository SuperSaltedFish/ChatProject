package com.yzx.chat.util;

import com.yzx.chat.core.util.Base64Util;
import com.yzx.chat.core.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by YZX on 2018年08月27日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class FileUtil {

    private static final double BYTE_SIZE_KB = 1024;
    private static final double BYTE_SIZE_MB = BYTE_SIZE_KB * 1024;
    private static final double BYTE_SIZE_GB = BYTE_SIZE_MB * 1024;

    public static String fileSizeFormat(long size) {
        if (size < BYTE_SIZE_KB) {
            return String.format(Locale.getDefault(), "%d Byte", size);
        } else if (size < BYTE_SIZE_MB) {
            return String.format(Locale.getDefault(), "%d KB", size / (int) BYTE_SIZE_KB);
        } else if (size < BYTE_SIZE_GB) {
            return String.format(Locale.getDefault(), "%.02f MB", size / BYTE_SIZE_MB);
        } else {
            return String.format(Locale.getDefault(), "%.02f GB", size / BYTE_SIZE_GB);
        }
    }

    public static String encodeBase64File(String path) {
        File file = new File(path);
        FileInputStream inputFile = null;
        try {
            inputFile = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            int length = inputFile.read(buffer);
            inputFile.close();
            if (length > 0) {
                return Base64Util.encodeToString(buffer);
            } else {
                return null;
            }
        } catch (IOException e) {
            LogUtil.d(e.toString(),e);
            return null;
        } finally {
            if (inputFile != null) {
                try {
                    inputFile.close();
                } catch (IOException e) {
                    LogUtil.d(e.toString(),e);
                }
            }
        }
    }


    public static String decoderBase64ToFile(String base64Code, String savePath, String fileName) {
        File file = new File(savePath);
        if (!file.exists() && !file.mkdirs()) {
            return null;
        }
        byte[] buffer = Base64Util.decode(base64Code);
        if (buffer == null || buffer.length == 0) {
            return null;
        }

        FileOutputStream out = null;
        try {
            file = new File(savePath + fileName);
            if (!file.exists() && !file.createNewFile()) {
                return null;
            }
            out = new FileOutputStream(file);
            out.write(buffer);
            return file.getPath();
        } catch (IOException e) {
            LogUtil.d(e.toString(),e);
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LogUtil.d(e.toString(),e);
                }
            }
        }
    }

    public static void saveStringToFile(String content, String savePath, String fileName) {
        File file = new File(savePath);
        if (!file.exists() && !file.mkdirs()) {
            return;
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(savePath + fileName), false);
            writer.write(content);
        } catch (IOException e) {
            LogUtil.d(e.toString(),e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LogUtil.d(e.toString(),e);
                }
            }
        }
    }
}
