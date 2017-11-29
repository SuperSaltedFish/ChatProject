package com.yzx.chat.util;

/**
 * Created by YZX on 2017年11月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class EmojiUtil {

    private static int[] sEmojiUnicode;

    public static int[] getCommonlyUsedEmojiUnicode() {
        if (sEmojiUnicode == null) {
            synchronized (EmojiUtil.class) {
                if (sEmojiUnicode == null) {
                    sEmojiUnicode = new int[0x4F + 1];
                    for (int i = 0; i <= 0x4F; i++) {
                        sEmojiUnicode[i] = 0x1F600 + i;
                    }
                }
            }
        }
        return sEmojiUnicode;
    }
}
