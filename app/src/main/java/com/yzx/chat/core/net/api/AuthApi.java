package com.yzx.chat.core.net.api;

import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.entity.LoginResponseEntity;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.annotation.POST;
import com.yzx.chat.core.net.framework.annotation.Param;


public interface AuthApi {

    String SMS_CODE_TYPE_LOGIN = "Login";
    String SMS_CODE_TYPE_REGISTER = "Register";

    @POST("auth/login")
    Call<JsonResponse<LoginResponseEntity>> login(@Param("telephone") String account,
                                                  @Param("password") String password,
                                                  @Param("verifyCode") String verifyCode);

    @POST("auth/register")
    Call<JsonResponse<Void>> register(@Param("telephone") String username,
                                      @Param("password") String password,
                                      @Param("nickname") String nickname,
                                      @Param("verifyCode") String verifyCode);

    @POST("auth/obtainSMSCode")
    Call<JsonResponse<Void>> obtainSMSCode(@Param("telephone") String telephone,
                                           @Param("codeType") String type);

    @POST("auth/tokenVerify")
    Call<JsonResponse<LoginResponseEntity>> tokenVerify();


}
