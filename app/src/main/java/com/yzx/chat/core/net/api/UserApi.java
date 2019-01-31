package com.yzx.chat.core.net.api;

import com.yzx.chat.core.entity.GetTempUserIDEntity;
import com.yzx.chat.core.entity.GetUserProfileEntity;
import com.yzx.chat.core.entity.SearchUserEntity;
import com.yzx.chat.core.entity.UploadAvatarEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.annotation.FilePart;
import com.yzx.chat.core.net.framework.annotation.POST;
import com.yzx.chat.core.net.framework.annotation.Param;
import com.yzx.chat.core.net.framework.annotation.Part;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public interface UserApi {

    @POST("user/getUserProfile")
    Call<JsonResponse<GetUserProfileEntity>> getUserProfile(@Param("targetUserID") String userID);

    @POST("user/searchUser")
    Call<JsonResponse<SearchUserEntity>> searchUser(@Param("queryCondition") String nicknameOrTelephone);


    @POST("user/getTempUserID")
    Call<JsonResponse<GetTempUserIDEntity>> getTempUserID();

    @POST("user/getUserProfileByTempUserID")
    Call<JsonResponse<UserEntity>> getUserProfileByTempUserID(@Param("tempUserID") String tempUserID);

    @POST("user/updateUserProfile")
    Call<JsonResponse<Void>> updateUserProfile(@Param("nickname") String nickname,
                                               @Param("sex") int sex,
                                               @Param("birthday") String birthday,
                                               @Param("location") String location,
                                               @Param("signature") String signature);


    @POST("user/uploadAvatar")
    Call<JsonResponse<UploadAvatarEntity>> uploadAvatar(@FilePart("uploadAvatar") String avatarPath, @Part("params") Object params);
}
