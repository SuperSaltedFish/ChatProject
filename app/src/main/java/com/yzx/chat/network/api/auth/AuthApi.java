package com.yzx.chat.network.api.auth;

import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;
import com.yzx.chat.network.framework.HttpParam;
import com.yzx.chat.network.framework.RequestType;


public interface AuthApi {

    String SMS_CODE_TYPE_LOGIN = "Login";
    String SMS_CODE_TYPE_REGISTER = "Register";

    @HttpApi(RequestType = RequestType.GET, url = "auth/getSecretKey")
    Call<JsonResponse<GetSecretKeyBean>> getSignature();

    @HttpApi(RequestType = RequestType.POST, url = "auth/login")
    Call<JsonResponse<UserInfoBean>> login(@HttpParam("telephone") String username,
                                           @HttpParam("password") String password,
                                           @HttpParam("deviceID") String deviceID,
                                           @HttpParam("clientPublicKey") String publicKey,
                                           @HttpParam("verifyCode") String verifyCode);

    @HttpApi(RequestType = RequestType.POST, url = "auth/register")
    Call<JsonResponse<UserInfoBean>> register(@HttpParam("telephone") String username,
                                              @HttpParam("password") String password,
                                              @HttpParam("nickname") String nickname,
                                              @HttpParam("deviceID") String deviceID,
                                              @HttpParam("clientPublicKey") String publicKey,
                                              @HttpParam("verifyCode") String verifyCode);

    @HttpApi(RequestType = RequestType.POST, url = "auth/obtainSMSCode")
    Call<JsonResponse<ObtainSMSCode>> obtainSMSCode(@HttpParam("telephone") String username,
                                                    @HttpParam("codeType") String type,
                                                    @HttpParam("clientPublicKey") String publicKey,
                                                    @HttpParam("data") Object data);

    @HttpApi(RequestType = RequestType.POST, url = "auth/tokenVerify")
    Call<JsonResponse<UserInfoBean>> tokenVerify();


}
