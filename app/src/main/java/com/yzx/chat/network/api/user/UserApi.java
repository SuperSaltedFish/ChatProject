package com.yzx.chat.network.api.user;

import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public interface UserApi {


    @HttpApi(RequestMethod = "POST", Path = "user/getUserProfile")
    Call<JsonResponse<GetUserProfileBean>> getUserProfile(String userID);

    @HttpApi(RequestMethod = "POST", Path = "user/getUserFriends")
    Call<JsonResponse<GetUserFriendsBean>> getUserFriends();

}
