package com.yzx.chat.network.api.user;

import com.yzx.chat.bean.UserBean;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;
import com.yzx.chat.network.framework.HttpParam;
import com.yzx.chat.network.framework.RequestType;
import com.yzx.chat.network.framework.UploadPath;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public interface UserApi {

    @HttpApi(RequestType = RequestType.POST, url = "user/getUserProfile")
    Call<JsonResponse<GetUserProfileBean>> getUserProfile(@HttpParam("targetUserID") String userID);

    @HttpApi(RequestType = RequestType.POST, url = "user/searchUser")
    Call<JsonResponse<SearchUserBean>> searchUser(@HttpParam("queryCondition") String nicknameOrTelephone);


    @HttpApi(RequestType = RequestType.POST, url = "user/getTempUserID")
    Call<JsonResponse<GetTempUserID>> getTempUserID();

    @HttpApi(RequestType = RequestType.POST, url = "user/getUserProfileByTempUserID")
    Call<JsonResponse<UserBean>> getUserProfileByTempUserID(@HttpParam("tempUserID") String tempUserID);

    @HttpApi(RequestType = RequestType.POST, url = "user/updateUserProfile")
    Call<JsonResponse<Void>> updateUserProfile(@HttpParam("nickname") String nickname,
                                               @HttpParam("sex") int sex,
                                               @HttpParam("birthday") String birthday,
                                               @HttpParam("location") String location,
                                               @HttpParam("signature") String signature);


    @HttpApi(RequestType = RequestType.POST_MULTI_PARAMS, url = "user/uploadAvatar")
    Call<JsonResponse<UploadAvatarBean>> uploadAvatar(@UploadPath("uploadAvatar") String avatarPath);
}
