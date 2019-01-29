package com.yzx.chat.core.extra;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;


/**
 * Created by YZX on 2018年07月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
@MessageTag(value = "Custom:ContactNtf", flag = MessageTag.NONE)
public class ContactNotificationMessageEx extends MessageContent{

    private String operation;
    private String sourceUserId;
    private String targetUserId;
    private String message;
    private String extra;
    public static final Parcelable.Creator<ContactNotificationMessageEx> CREATOR = new Parcelable.Creator<ContactNotificationMessageEx>() {
        public ContactNotificationMessageEx createFromParcel(Parcel source) {
            return new ContactNotificationMessageEx(source);
        }

        public ContactNotificationMessageEx[] newArray(int size) {
            return new ContactNotificationMessageEx[size];
        }
    };

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSourceUserId() {
        return this.sourceUserId;
    }

    public void setSourceUserId(String sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public String getTargetUserId() {
        return this.targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public ContactNotificationMessageEx(Parcel in) {
        this.operation = ParcelUtils.readFromParcel(in);
        this.sourceUserId = ParcelUtils.readFromParcel(in);
        this.targetUserId = ParcelUtils.readFromParcel(in);
        this.message = ParcelUtils.readFromParcel(in);
        this.extra = ParcelUtils.readFromParcel(in);
        this.setUserInfo(ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    public static ContactNotificationMessageEx obtain(String operation, String sourceUserId, String targetUserId, String message) {
        ContactNotificationMessageEx obj = new ContactNotificationMessageEx();
        obj.operation = operation;
        obj.sourceUserId = sourceUserId;
        obj.targetUserId = targetUserId;
        obj.message = message;
        return obj;
    }

    private ContactNotificationMessageEx() {
    }

    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.putOpt("operation", this.operation);
            jsonObj.putOpt("sourceUserId", this.sourceUserId);
            jsonObj.putOpt("targetUserId", this.targetUserId);
            if (!TextUtils.isEmpty(this.message)) {
                jsonObj.putOpt("message", this.message);
            }

            if (!TextUtils.isEmpty(this.getExtra())) {
                jsonObj.putOpt("extra", this.getExtra());
            }

            if (this.getJSONUserInfo() != null) {
                jsonObj.putOpt("user", this.getJSONUserInfo());
            }
        } catch (JSONException var4) {
            RLog.e("ContactNotificationMessage", "JSONException " + var4.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public ContactNotificationMessageEx(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
            var5.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            this.setOperation(jsonObj.optString("operation"));
            this.setSourceUserId(jsonObj.optString("sourceUserId"));
            this.setTargetUserId(jsonObj.optString("targetUserId"));
            this.setMessage(jsonObj.optString("message"));
            this.setExtra(jsonObj.optString("extra"));
            if (jsonObj.has("user")) {
                this.setUserInfo(this.parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }
        } catch (JSONException var4) {
            RLog.e("ContactNotificationMessage", "JSONException " + var4.getMessage());
        }

    }

    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.operation);
        ParcelUtils.writeToParcel(dest, this.sourceUserId);
        ParcelUtils.writeToParcel(dest, this.targetUserId);
        ParcelUtils.writeToParcel(dest, this.message);
        ParcelUtils.writeToParcel(dest, this.extra);
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
    }

    public int describeContents() {
        return 0;
    }
}
