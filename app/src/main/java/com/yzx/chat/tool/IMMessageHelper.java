package com.yzx.chat.tool;


import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.extra.VideoMessage;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.NotificationMessage;
import io.rong.message.StickerMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by YZX on 2017年12月13日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class IMMessageHelper {


    public static CharSequence getMessageDigest(Conversation conversation) {
        if (conversation == null) {
            return "";
        }
        if (!TextUtils.isEmpty(conversation.getDraft())) {
            String hint = AndroidUtil.getString(R.string.EMMessageUtil_Draft);
            SpannableString spannableStr = new SpannableString(hint + " " + conversation.getDraft());
            spannableStr.setSpan(new ForegroundColorSpan(Color.RED), 0, hint.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            return spannableStr;
        } else {
            return getMessageDigest(conversation.getLatestMessage());
        }
    }

    public static CharSequence getMessageDigest(MessageContent messageContent) {
        if (messageContent instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) messageContent;
            return textMessage.getContent();
        } else if (messageContent instanceof VoiceMessage) {
            return AndroidUtil.getString(R.string.EMMessageUtil_VoiceInfo);
        } else if (messageContent instanceof ImageMessage) {
            return AndroidUtil.getString(R.string.EMMessageUtil_ImageInfo);
        } else if (messageContent instanceof LocationMessage) {
            return AndroidUtil.getString(R.string.EMMessageUtil_LocationInfo);
        } else if (messageContent instanceof StickerMessage) {
            return AndroidUtil.getString(R.string.EMMessageUtil_EmotionInfo);
        } else if (messageContent instanceof FileMessage) {
            return AndroidUtil.getString(R.string.EMMessageUtil_FileInfo);
        } else if (messageContent instanceof VideoMessage) {
            return AndroidUtil.getString(R.string.EMMessageUtil_VideoInfo);
        } else if (messageContent instanceof GroupNotificationMessage) {
            return groupNotificationMessageToString((GroupNotificationMessage) messageContent);
        }
        return "";
    }

    private static final Gson GSON = ApiHelper.getDefaultGsonInstance();

    public static CharSequence groupNotificationMessageToString(GroupNotificationMessage message) {
        GroupBean group;
        switch (message.getOperation()) {
            case GroupManager.GROUP_OPERATION_CREATE:
                GroupManager.GroupMessageExtra_Created extra_Create;
                try {
                    extra_Create = GSON.fromJson(message.getExtra(), GroupManager.GroupMessageExtra_Created.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    LogUtil.e("fromJson GroupMessageExtra_Created.class fail,json content:" + message.getExtra());
                    break;
                }
                group = extra_Create.group;
                if (group == null) {
                    LogUtil.e(" GroupOperation : group is empty");
                    break;
                }
                UserBean user;
                StringBuilder builder = new StringBuilder(group.getMembers().size() * 18);
                boolean isFirst = true;
                for (GroupMemberBean groupMember : group.getMembers()) {
                    user = groupMember.getUserProfile();
                    if (user.getUserID().equals(group.getOwner())) {
                        builder.insert(0, user.getNickname() + "邀请");
                    } else {
                        if (!isFirst) {
                            builder.append("、");
                        } else {
                            isFirst = false;
                        }
                        builder.append(user.getNickname());
                    }
                }
                builder.append("加入群组");
                return builder.toString();
            case GroupManager.GROUP_OPERATION_ADD:
                break;
            case GroupManager.GROUP_OPERATION_QUIT:
                break;
        }
        return "";
    }

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("M月d日 %'s'HH:mm", Locale.getDefault());

    public static String messageTimeToString(long timeMillis) {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        if (timeMillis >= todayCalendar.getTimeInMillis()) {
            return TIME_FORMAT.format(new Date(timeMillis));
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeMillis);
            String am_pm = "";
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 0 && hour < 6) {
                am_pm = "凌晨";
            } else if (hour >= 6 && hour < 12) {
                am_pm = "早上";
            } else if (hour == 12) {
                am_pm = "中午";
            } else if (hour > 12 && hour < 18) {
                am_pm = "下午";
            } else if (hour >= 18) {
                am_pm = "晚上";
            }
            return String.format(DATE_FORMAT.format(new Date(timeMillis)), am_pm);
        }
    }
}
