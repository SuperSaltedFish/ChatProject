package com.yzx.chat.network.api.auth;

import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;
import com.yzx.chat.network.framework.HttpParam;

public interface AuthApi {

    String SMS_CODE_TYPE_LOGIN = "Login";
    String SMS_CODE_TYPE_REGISTER = "Register";

    @HttpApi(RequestMethod = "GET", Path = "auth/getSecretKey")
    Call<JsonResponse<GetSecretKeyBean>> getSignature();

    @HttpApi(RequestMethod = "POST", Path = "auth/login")
    Call<JsonResponse<LoginRegisterBean>> login(@HttpParam("telephone") String username,
                                                @HttpParam("password") String password,
                                                @HttpParam("deviceID") String deviceID,
                                                @HttpParam("clientPublicKey") String publicKey,
                                                @HttpParam("verifyCode") String verifyCode);

    @HttpApi(RequestMethod = "POST", Path = "auth/register")
    Call<JsonResponse<LoginRegisterBean>> register(@HttpParam("telephone") String username,
                                                   @HttpParam("password") String password,
                                                   @HttpParam("nickname") String nickname,
                                                   @HttpParam("deviceID") String deviceID,
                                                   @HttpParam("clientPublicKey") String publicKey,
                                                   @HttpParam("verifyCode") String verifyCode);

    @HttpApi(RequestMethod = "POST", Path = "auth/obtainSMSCode")
    Call<JsonResponse<ObtainSMSCode>> obtainSMSCode(@HttpParam("telephone") String username,
                                                    @HttpParam("codeType") String type,
                                                    @HttpParam("clientPublicKey") String publicKey,
                                                    @HttpParam("data") Object data);

    @HttpApi(RequestMethod = "POST", Path = "auth/tokenVerify")
    Call<JsonResponse<TokenVerifyBean>> tokenVerify();



}
