package com.yzx.chat.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class BitmapUtil {


    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    // 从Resources中加载图片
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options); // 读取图片长款
        options.inSampleSize = getScaleSampleSize(options, reqWidth, reqHeight); // 计算inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
        return createScaleBitmap(src, reqWidth, reqHeight); // 进一步得到目标大小的缩略图
    }

    // 从sd卡上加载图片
    public static Bitmap decodeSampledBitmapFromFd(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = getScaleSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }

    public static Bitmap decodeStreamBySampleSize(Resources res, int resId, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeStream(res.openRawResource(resId), null, options);
    }

    public static Bitmap decodeStreamByScale(Resources res, int resId, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = (int) getScaleSampleSize(res, resId, viewWidth, viewHeight);
        return BitmapFactory.decodeStream(res.openRawResource(resId), null, options);
    }

    public static int getScaleSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    public static float getScaleSampleSize(Resources res, int resId, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(res.openRawResource(resId), null, options);
        float inSampleSize = 1f;
        if (options.outWidth > viewWidth || options.outHeight > viewHeight) {
            float heightScale = options.outWidth / (float) viewHeight;
            float widthScale = options.outHeight / (float) viewHeight;
            inSampleSize = Math.min(widthScale, heightScale);
        }
        return inSampleSize;
    }


    public static Size getBitmapBoundsSize(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        return new Size(options.outWidth, options.outHeight);
    }


    public static String saveBitmapToPNG(Bitmap bitmap, String path, String fileName) {
        String filePath = path + fileName + ".png";
        File file = new File( filePath);
        try {
            if(!file.exists()&&!file.createNewFile()){
                LogUtil.e("createNewFile fail");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            return filePath;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String saveBitmapToJPEG(Bitmap bitmap, String path, String fileName) {
        String filePath = path + fileName + ".jpeg";
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            return filePath;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
