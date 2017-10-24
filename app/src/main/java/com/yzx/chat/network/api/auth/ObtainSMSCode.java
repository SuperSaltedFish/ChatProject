package com.yzx.chat.network.api.auth;

/**
 * Created by YZX on 2017年10月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ObtainSMSCode {

   private boolean isSkipVerify;

   private String verifyCode;

    public boolean isSkipVerify() {
        return isSkipVerify;
    }

    public void setSkipVerify(boolean skipVerify) {
        isSkipVerify = skipVerify;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
