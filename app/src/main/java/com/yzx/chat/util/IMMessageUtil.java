package com.yzx.chat.util;


import com.yzx.chat.R;

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
    public static String getMessageDigest(MessageContent message) {
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
        }
        return "";
    }

}
