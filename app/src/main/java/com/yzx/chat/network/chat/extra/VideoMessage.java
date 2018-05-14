package com.yzx.chat.network.chat.extra;


import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.UserInfo;
import io.rong.message.MediaMessageContent;

/**
 * Created by YZX on 2018年05月11日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

@MessageTag(value = "Test:VideoMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED, messageHandler = VideoMessageHandler.class)
public class VideoMessage extends MediaMessageContent {
    private Uri mThumbUri;//缩略图的url
    private String mBase64;//经过base64编码的消息体发送时携带的缩略图
    private int mDuration;//小视频时长，可选，可以根据开发者自己的产品需求去掉
    private String mName;//小视频的名称，可选，可以根据开发者自己的产品需求去掉
    private long mSize;//小视频文件大小，可选，可以根据开发者自己的产品需求去掉

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            if (!TextUtils.isEmpty(mBase64)) {
                jsonObj.put("content", mBase64);
            } else {
                Log.d("VideoMessage", "base64 is null");
            }
            if (!TextUtils.isEmpty(mName)) {
                jsonObj.put("name", mName);
            }

            jsonObj.put("size", mSize);

            if (getLocalPath() != null) {
                jsonObj.put("localPath", getLocalPath().toString());
            }
            if (getMediaUrl() != null) {
                jsonObj.put("sightUrl", getMediaUrl().toString());
            }
            jsonObj.put("duration", getDuration());
            if (!TextUtils.isEmpty(getExtra()))
                jsonObj.put("extra", getExtra());

            if (getJSONUserInfo() != null)
                jsonObj.putOpt("user", getJSONUserInfo());
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }
        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public VideoMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            if (jsonObj.has("name"))
                setName(jsonObj.optString("name"));
            if (jsonObj.has("size"))
                setSize(jsonObj.getLong("size"));
            if (jsonObj.has("content")) {
                setBase64(jsonObj.optString("content"));
            }
            if (jsonObj.has("localPath"))
                setLocalPath(Uri.parse(jsonObj.optString("localPath")));
            if (jsonObj.has("sightUrl"))
                setMediaUrl(Uri.parse(jsonObj.optString("sightUrl")));
            if (jsonObj.has("duration"))
                setDuration(jsonObj.optInt("duration"));
            if (jsonObj.has("extra")) {
                setExtra(jsonObj.optString("extra"));
            }
            if (jsonObj.has("user")) {
                setUserInfo(parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }
    }


    private VideoMessage(Uri localVideoUri) {
        setLocalPath(localVideoUri);
    }

    /**
     * 生成SightMessage对象。
     *
     * @param localVideoUri 小视频地址。
     * @return SightMessage对象实例。
     */
    public static VideoMessage obtain(Uri localVideoUri) {
        if (!"file".equals(localVideoUri.getScheme())) {
            return null;
        }
        File file = new File(localVideoUri.toString().substring(7));
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        return new VideoMessage(localVideoUri);
    }

    /**
     * 获取缩略图Uri。
     *
     * @return 缩略图Uri（收消息情况下此为内部Uri，需要通过ResourceManager.getInstance().getFile(new Resource(Uri))方式才能获取到真实地址）。
     */
    public Uri getThumbUri() {
        return mThumbUri;
    }

    /**
     * 设置缩略图Uri。
     *
     * @param thumbUri 缩略图地址
     */
    public void setThumbUri(Uri thumbUri) {
        this.mThumbUri = thumbUri;
    }

    /**
     * 设置需要传递的Base64数据
     *
     * @param base64 base64数据。
     */
    public void setBase64(String base64) {
        mBase64 = base64;
    }

    /**
     * 获取需要传递的Base64数据。
     *
     * @return base64数据。
     */
    public String getBase64() {
        return mBase64;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public String getName() {
        return mName;
    }

    public void setName(String Name) {
        this.mName = Name;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    /**
     * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
     *
     * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 构造函数。
     *
     * @param in 初始化传入的 Parcel。
     */
    public VideoMessage(Parcel in) {
        mThumbUri = ParcelUtils.readFromParcel(in, Uri.class);
        setLocalPath(ParcelUtils.readFromParcel(in, Uri.class));
        setMediaUrl(ParcelUtils.readFromParcel(in, Uri.class));
        setDuration(ParcelUtils.readIntFromParcel(in));
        setName(ParcelUtils.readFromParcel(in));
        setSize(ParcelUtils.readLongFromParcel(in));
        setExtra(ParcelUtils.readFromParcel(in));
        setUserInfo(ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     *
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志，可能是 0 或 PARCELABLE_WRITE_RETURN_VALUE。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, mThumbUri);
        ParcelUtils.writeToParcel(dest, getLocalPath());
        ParcelUtils.writeToParcel(dest, getMediaUrl());
        ParcelUtils.writeToParcel(dest, getDuration());
        ParcelUtils.writeToParcel(dest, getName());
        ParcelUtils.writeToParcel(dest, getSize());
        ParcelUtils.writeToParcel(dest, getExtra());
        ParcelUtils.writeToParcel(dest, getUserInfo());
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<VideoMessage> CREATOR = new Creator<VideoMessage>() {

        @Override
        public VideoMessage createFromParcel(Parcel source) {
            return new VideoMessage(source);
        }

        @Override
        public VideoMessage[] newArray(int size) {
            return new VideoMessage[size];
        }
    };

    @Override
    public String toString() {
        return "VideoMessage{" +
                "mThumbUri=" + mThumbUri +
                ", mBase64='" + mBase64 + '\'' +
                ", mDuration=" + mDuration +
                ", mName='" + mName + '\'' +
                ", mSize=" + mSize +
                '}';
    }
}

