package com.yzx.chat.network.api.user;

import com.yzx.chat.bean.UserBean;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.POST;
import com.yzx.chat.network.framework.Param;
import com.yzx.chat.network.framework.Part;
import com.yzx.chat.network.framework.UploadPart;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public interface UserApi {

    @POST("user/getUserProfile")
    Call<JsonResponse<GetUserProfileBean>> getUserProfile(@Param("targetUserID") String userID);

    @POST("user/searchUser")
    Call<JsonResponse<SearchUserBean>> searchUser(@Param("queryCondition") String nicknameOrTelephone);


    @POST("user/getTempUserID")
    Call<JsonResponse<GetTempUserID>> getTempUserID();

    @POST("user/getUserProfileByTempUserID")
    Call<JsonResponse<UserBean>> getUserProfileByTempUserID(@Param("tempUserID") String tempUserID);

    @POST("user/updateUserProfile")
    Call<JsonResponse<Void>> updateUserProfile(@Param("nickname") String nickname,
                                               @Param("sex") int sex,
                                               @Param("birthday") String birthday,
                                               @Param("location") String location,
                                               @Param("signature") String signature);


    @POST("user/uploadAvatar")
    Call<JsonResponse<UploadAvatarBean>> uploadAvatar(@UploadPart("uploadAvatar") String avatarPath, @Part("params") Object params);
}
