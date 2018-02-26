package com.yzx.chat.network.api.user;

import com.yzx.chat.bean.UserBean;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;
import com.yzx.chat.network.framework.HttpParam;
import com.yzx.chat.network.framework.UploadPath;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public interface UserApi {

    @HttpApi(RequestMethod = "POST", Path = "user/getUserProfile")
    Call<JsonResponse<GetUserProfileBean>> getUserProfile(@HttpParam("targetUserID") String userID);

    @HttpApi(RequestMethod = "POST", Path = "user/searchUser")
    Call<JsonResponse<SearchUserBean>> searchUser(@HttpParam("queryCondition") String nicknameOrTelephone);


    @HttpApi(RequestMethod = "POST", Path = "user/getTempUserID")
    Call<JsonResponse<GetTempUserID>> getTempUserID();

    @HttpApi(RequestMethod = "POST", Path = "user/getUserProfileByTempUserID")
    Call<JsonResponse<UserBean>> getUserProfileByTempUserID(@HttpParam("tempUserID") String tempUserID);

    @HttpApi(RequestMethod = "POST", Path = "user/updateUserProfile")
    Call<JsonResponse<Void>> updateUserProfile(@HttpParam("nickname") String nickname,
                                               @HttpParam("sex") int sex,
                                               @HttpParam("birthday") String birthday,
                                               @HttpParam("location") String location,
                                               @HttpParam("signature") String signature);

    @HttpApi(RequestMethod = "POST", Path = "user/uploadAvatar")
    Call<JsonResponse<Void>> uploadAvatar(@UploadPath String path);
}
