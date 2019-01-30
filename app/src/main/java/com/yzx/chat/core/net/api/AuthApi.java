package com.yzx.chat.core.net.api;

import com.yzx.chat.core.entity.GetSecretKeyEntity;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.entity.ObtainSMSCodeEntity;
import com.yzx.chat.core.entity.UserInfoEntity;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.annotation.GET;
import com.yzx.chat.core.net.framework.annotation.POST;
import com.yzx.chat.core.net.framework.annotation.Param;


public interface AuthApi {

    String SMS_CODE_TYPE_LOGIN = "Login";
    String SMS_CODE_TYPE_REGISTER = "Register";

    @GET("auth/getSecretKey")
    Call<JsonResponse<GetSecretKeyEntity>> getSignature();

    @POST("auth/login")
    Call<JsonResponse<UserInfoEntity>> login(@Param("telephone") String username,
                                             @Param("password") String password,
                                             @Param("deviceID") String deviceID,
                                             @Param("clientPublicKey") String publicKey,
                                             @Param("verifyCode") String verifyCode);

    @POST("auth/register")
    Call<JsonResponse<UserInfoEntity>> register(@Param("telephone") String username,
                                                @Param("password") String password,
                                                @Param("nickname") String nickname,
                                                @Param("deviceID") String deviceID,
                                                @Param("clientPublicKey") String publicKey,
                                                @Param("verifyCode") String verifyCode);

    @POST("auth/obtainSMSCode")
    Call<JsonResponse<ObtainSMSCodeEntity>> obtainSMSCode(@Param("telephone") String username,
                                                          @Param("codeType") String type,
                                                          @Param("clientPublicKey") String publicKey,
                                                          @Param("data") Object data);

    @POST("auth/tokenVerify")
    Call<JsonResponse<UserInfoEntity>> tokenVerify();


}
