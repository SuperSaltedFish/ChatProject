package com.yzx.chat.core.net.api.auth;

import com.yzx.chat.core.net.api.JsonResponse;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.GET;
import com.yzx.chat.core.net.framework.POST;
import com.yzx.chat.core.net.framework.Param;


public interface AuthApi {

    String SMS_CODE_TYPE_LOGIN = "Login";
    String SMS_CODE_TYPE_REGISTER = "Register";

    @GET("auth/getSecretKey")
    Call<JsonResponse<GetSecretKeyBean>> getSignature();

    @POST("auth/login")
    Call<JsonResponse<UserInfoBean>> login(@Param("telephone") String username,
                                           @Param("password") String password,
                                           @Param("deviceID") String deviceID,
                                           @Param("clientPublicKey") String publicKey,
                                           @Param("verifyCode") String verifyCode);

    @POST("auth/register")
    Call<JsonResponse<UserInfoBean>> register(@Param("telephone") String username,
                                              @Param("password") String password,
                                              @Param("nickname") String nickname,
                                              @Param("deviceID") String deviceID,
                                              @Param("clientPublicKey") String publicKey,
                                              @Param("verifyCode") String verifyCode);

    @POST("auth/obtainSMSCode")
    Call<JsonResponse<ObtainSMSCode>> obtainSMSCode(@Param("telephone") String username,
                                                    @Param("codeType") String type,
                                                    @Param("clientPublicKey") String publicKey,
                                                    @Param("data") Object data);

    @POST("auth/tokenVerify")
    Call<JsonResponse<UserInfoBean>> tokenVerify();


}
