package com.yzx.chat.util;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.yzx.chat.R;

/**
 * Created by YZX on 2017年12月13日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class EMMessageUtil {
    public static String getMessageDigest(EMMessage message) {
        String digest = "";
        switch (message.getType()) {
            case LOCATION:
                digest = AndroidUtil.getString(R.string.EMMessageUtil_LocationInfo);
                break;
            case IMAGE:
                digest = AndroidUtil.getString(R.string.EMMessageUtil_ImageInfo);
                break;
            case VOICE:
                digest = AndroidUtil.getString(R.string.EMMessageUtil_VoiceInfo);
                break;
            case VIDEO:
                digest = AndroidUtil.getString(R.string.EMMessageUtil_VideoInfo);
                break;
            case TXT:
                digest = (((EMTextMessageBody) message.getBody()).getMessage());
                break;
            case FILE:
                digest = AndroidUtil.getString(R.string.EMMessageUtil_FileInfo);
                break;
            default:
                LogUtil.e("unknow type:" + message.getType());
        }
        return digest;
    }

}
