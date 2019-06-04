package com.yzx.chat.util;

/**
 * Created by YZX on 2019年01月07日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class YUVUtil {
    public static void rotateYUVDegree90(byte[] src, byte[] desc, int imageWidth, int imageHeight) {

        // Rotate the Y components
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                desc[i] = src[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                desc[i] = src[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                desc[i] = src[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
    }
}
