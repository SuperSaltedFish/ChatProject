package com.yzx.chat.util;


import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.yzx.chat.R;
import com.yzx.chat.network.chat.extra.VideoMessage;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.StickerMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by YZX on 2017年12月13日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class IMMessageUtil {
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
            MessageContent message = conversation.getLatestMessage();
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                return textMessage.getContent();
            } else if (message instanceof VoiceMessage) {
                return AndroidUtil.getString(R.string.EMMessageUtil_VoiceInfo);
            } else if (message instanceof ImageMessage) {
                return AndroidUtil.getString(R.string.EMMessageUtil_ImageInfo);
            } else if (message instanceof LocationMessage) {
                return AndroidUtil.getString(R.string.EMMessageUtil_LocationInfo);
            } else if (message instanceof StickerMessage) {
                return AndroidUtil.getString(R.string.EMMessageUtil_EmotionInfo);
            } else if (message instanceof FileMessage) {
                return AndroidUtil.getString(R.string.EMMessageUtil_FileInfo);
            }else if (message instanceof VideoMessage) {
                return AndroidUtil.getString(R.string.EMMessageUtil_VideoInfo);
            }
            return "";
        }
    }
}
