package com.yzx.chat.tool;


import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.ContactManager;
import com.yzx.chat.core.GroupManager;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.extra.VideoMessage;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.core.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.FileMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
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
            String hint = AndroidHelper.getString(R.string.EMMessageUtil_Draft);
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
            return AndroidHelper.getString(R.string.MessageSummary_VoiceInfo);
        } else if (messageContent instanceof ImageMessage) {
            return AndroidHelper.getString(R.string.MessageSummary_ImageInfo);
        } else if (messageContent instanceof LocationMessage) {
            return AndroidHelper.getString(R.string.MessageSummary_LocationInfo);
        } else if (messageContent instanceof FileMessage) {
            return AndroidHelper.getString(R.string.MessageSummary_FileInfo);
        } else if (messageContent instanceof VideoMessage) {
            return AndroidHelper.getString(R.string.MessageSummary_VideoInfo);
        } else if (messageContent instanceof GroupNotificationMessage) {
            return groupNotificationMessageToString((GroupNotificationMessage) messageContent);
        } else if (messageContent instanceof ContactNotificationMessage) {
            return contactNotificationMessageToString((ContactNotificationMessage) messageContent);
        }
        return "";
    }

    private static final Gson GSON = ApiHelper.GSON;

    public static CharSequence contactNotificationMessageToString(ContactNotificationMessage message) {
        switch (message.getOperation()) {
            case ContactManager.CONTACT_OPERATION_ACCEPT:
            case ContactManager.CONTACT_OPERATION_ACCEPT_ACTIVE:
                try {
                    ContactEntity contact = AppClient.getInstance().getContactManager().getContact(message.getTargetUserId());
                    if (contact != null) {
                        return String.format(AndroidHelper.getString(R.string.MessageSummary_ContactNtf_Accept), contact.getName());
                    } else {
                        LogUtil.e("contact==null " + message.getExtra());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            default:
                LogUtil.e("Unknown contact operation " + message.getOperation());
                return "";
        }
    }

    public static CharSequence groupNotificationMessageToString(GroupNotificationMessage message) {
        GroupEntity group;
        StringBuilder builder;
        UserEntity user;
        boolean isFirst = true;
        try {
            switch (message.getOperation()) {
                case GroupManager.GROUP_OPERATION_CREATE:
                    GroupManager.GroupMessageExtra.Created createExtra = GSON.fromJson(message.getExtra(), GroupManager.GroupMessageExtra.Created.class);
                    if (createExtra == null || createExtra.group == null) {
                        break;
                    }
                    group = createExtra.group;
                    builder = new StringBuilder(group.getMembers().size() * 18);
                    for (GroupMemberEntity groupMember : group.getMembers()) {
                        user = groupMember.getUserProfile();
                        if (user.getUserID().equals(group.getOwner())) {
                            if (user.getUserID().equals(AppClient.getInstance().getUserID())) {
                                builder.insert(0, "我邀请");
                            } else {
                                builder.insert(0, user.getNickname() + "邀请");
                            }
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
                    GroupManager.GroupMessageExtra.Add addExtra = GSON.fromJson(message.getExtra(), GroupManager.GroupMessageExtra.Add.class);
                    if (addExtra == null || addExtra.group == null || addExtra.membersID == null) {
                        break;
                    }
                    group = addExtra.group;
                    builder = new StringBuilder(addExtra.membersID.length * 18);
                    List<String> memberIDList = Arrays.asList(addExtra.membersID);
                    for (GroupMemberEntity groupMember : group.getMembers()) {
                        user = groupMember.getUserProfile();
                        if (user.getUserID().equals(addExtra.operatorUserID)) {
                            if (user.getUserID().equals(AppClient.getInstance().getUserID())) {
                                builder.insert(0, "我邀请");
                            } else {
                                builder.insert(0, user.getNickname() + "邀请");
                            }
                        } else if (memberIDList.contains(user.getUserID())) {
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
                case GroupManager.GROUP_OPERATION_JOIN:
                    GroupManager.GroupMessageExtra.Join joinExtra = GSON.fromJson(message.getExtra(), GroupManager.GroupMessageExtra.Join.class);
                    if (joinExtra == null || joinExtra.group == null || joinExtra.memberID == null) {
                        break;
                    }
                    group = joinExtra.group;
                    for (GroupMemberEntity groupMember : group.getMembers()) {
                        user = groupMember.getUserProfile();
                        if (user.getUserID().equals(joinExtra.memberID)) {
                            return user.getNickname() + "通过扫描二维码加入了群组";
                        }
                    }
                case GroupManager.GROUP_OPERATION_QUIT:
                    GroupManager.GroupMessageExtra.Quit quitExtra = GSON.fromJson(message.getExtra(), GroupManager.GroupMessageExtra.Quit.class);
                    if (quitExtra == null || quitExtra.member == null) {
                        break;
                    }
                    return quitExtra.member.getNicknameInGroup() + "退出了群组";
                case GroupManager.GROUP_OPERATION_RENAME:
                    GroupManager.GroupMessageExtra.Rename renameExtra = GSON.fromJson(message.getExtra(), GroupManager.GroupMessageExtra.Rename.class);
                    if (renameExtra == null || renameExtra.group == null) {
                        break;
                    }
                    group = renameExtra.group;
                    for (GroupMemberEntity groupMember : group.getMembers()) {
                        user = groupMember.getUserProfile();
                        if (user.getUserID().equals(renameExtra.operatorUserID)) {
                            if (user.getUserID().equals(AppClient.getInstance().getUserID())) {
                                return "我修改群名称为" + group.getName();
                            } else {
                                return groupMember.getNicknameInGroup() + "修改群名称为" + group.getName();
                            }
                        }
                    }
                    break;
                case GroupManager.GROUP_OPERATION_BULLETIN:
                    GroupManager.GroupMessageExtra.Bulletin bulletinExtra = GSON.fromJson(message.getExtra(), GroupManager.GroupMessageExtra.Bulletin.class);
                    if (bulletinExtra == null || bulletinExtra.group == null) {
                        break;
                    }
                    group = bulletinExtra.group;
                    for (GroupMemberEntity groupMember : group.getMembers()) {
                        user = groupMember.getUserProfile();
                        if (user.getUserID().equals(bulletinExtra.operatorUserID)) {
                            if (user.getUserID().equals(AppClient.getInstance().getUserID())) {
                                return "我修改群公告为" + group.getNotice();
                            } else {
                                return groupMember.getNicknameInGroup() + "修改群公告为" + group.getNotice();
                            }
                        }
                    }
                    break;
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            LogUtil.e("json to object fail,Operation:" + message.getOperation());
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
