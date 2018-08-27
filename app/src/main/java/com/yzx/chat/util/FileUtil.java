package com.yzx.chat.util;

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
}
