package com.yzx.chat.network.chat.extra;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.MD5Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.imlib.model.Message;
import io.rong.message.MessageHandler;

/**
 * Created by YZX on 2018年05月11日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class VideoMessageHandler extends MessageHandler<VideoMessage> {
    private final static String TAG = "VideoMessageHandler";

    private static final int THUMB_COMPRESSED_WIDTH_SIZE = 158;
    private static final int THUMB_COMPRESSED_HEIGHT_SIZE = 280;
    private static final int THUMB_COMPRESSED_QUALITY = 50;
    private static final String VIDEO_THUMBNAIL_PATH = DirectoryHelper.getPublicThumbnailPath();

    public VideoMessageHandler(Context context) {
        super(context);
    }

    @Override
    public void decodeMessage(Message message, VideoMessage model) {
        if (!TextUtils.isEmpty(model.getBase64())) {
            byte[] data = null;
            try {
                data = Base64.decode(model.getBase64(), Base64.NO_WRAP);
            } catch (IllegalArgumentException e) {
                RLog.e(TAG, "afterDecodeMessage Not Base64 Content!");
                e.printStackTrace();
            }

            if (!isImageFile(data)) {
                RLog.e(TAG, "afterDecodeMessage Not Image File!");
                return;
            }
            File file = new File(VIDEO_THUMBNAIL_PATH + MD5Util.encrypt16(message.getSenderUserId() + message.getTargetId() + message.getMessageId()) + ".jpeg");
            if (!file.exists()) {
                FileUtils.byte2File(data, file.getParent(), file.getName());
            }
            model.setThumbUri(Uri.fromFile(file));
            model.setBase64(null);
        }
    }

    @Override
    public void encodeMessage(Message message) {
        VideoMessage model = (VideoMessage) message.getContent();
        Uri localVideo = model.getLocalPath();
        VideoInfo videoInfo = getVideoInfoFromLocalFile(localVideo.getPath());
        if (videoInfo == null) {
            return;
        }
        LogUtil.e(model.toString());
        if (videoInfo.thumbnail != null) {
            File file = new File(VIDEO_THUMBNAIL_PATH + MD5Util.encrypt32(message.getSenderUserId() + message.getTargetId() + message.getMessageId()) + ".jpeg");
            if (!file.exists()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    videoInfo.thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMB_COMPRESSED_QUALITY, outputStream);
                    byte[] data = outputStream.toByteArray();
                    model.setBase64(Base64.encodeToString(data, Base64.NO_WRAP));
                    FileUtils.byte2File(data, file.getParent(), file.getName());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                byte[] data = FileUtils.file2byte(file);
                model.setBase64(Base64.encodeToString(data, Base64.NO_WRAP));
            }
            model.setThumbUri(Uri.fromFile(file));
            videoInfo.thumbnail.recycle();
        }
        model.setDuration(videoInfo.duration);
        model.setName(videoInfo.name);
        model.setSize(videoInfo.size);
    }

    private static boolean isImageFile(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        return options.outWidth != -1;
    }

    private static VideoInfo getVideoInfoFromLocalFile(String videoPath) {
        if (TextUtils.isEmpty(videoPath)) {
            return null;
        }
        File file = new File(videoPath);
        if (!file.exists()) {
            return null;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bitmap;
        String duration;
        try {
            retriever.setDataSource(videoPath);
            bitmap = retriever.getFrameAtTime(-1); //取得指定时间的Bitmap，即可以实现抓图（缩略图）功能
            duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (RuntimeException ex) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        VideoInfo videoInfo = new VideoInfo();
        if (bitmap != null && (bitmap.getWidth() > THUMB_COMPRESSED_WIDTH_SIZE || bitmap.getHeight() > THUMB_COMPRESSED_HEIGHT_SIZE)) {
            Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, THUMB_COMPRESSED_WIDTH_SIZE, THUMB_COMPRESSED_HEIGHT_SIZE, true);
            if (bitmap != scaleBitmap && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            videoInfo.thumbnail = scaleBitmap;
        }
        if (!TextUtils.isEmpty(duration)) {
            videoInfo.duration = Integer.parseInt(duration);
        }
        videoInfo.size = file.length();
        videoInfo.name = file.getName();
        return videoInfo;
    }

    private static final class VideoInfo {
        Bitmap thumbnail;
        int duration;
        long size;
        String name;
    }

}
