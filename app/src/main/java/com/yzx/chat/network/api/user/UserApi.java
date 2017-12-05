package com.yzx.chat.network.api.user;

import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;
import com.yzx.chat.network.framework.HttpParam;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public interface UserApi {


    @HttpApi(RequestMethod = "POST", Path = "user/getUserProfile")
    Call<JsonResponse<GetUserProfileBean>> getUserProfile(@HttpParam("targetUserID") String userID);

    @HttpApi(RequestMethod = "POST", Path = "user/searchFriend")
    Call<JsonResponse<SearchUserBean>> searchUser(@HttpParam("queryCondition") String nicknameOrTelephone);

    @HttpApi(RequestMethod = "POST", Path = "user/getUserFriends")
    Call<JsonResponse<GetUserFriendsBean>> getUserFriends();

    @HttpApi(RequestMethod = "POST", Path = "user/addFriend")
    Call<JsonResponse<Void>> addFriend(@HttpParam("friendID") String friendUserID);

    @HttpApi(RequestMethod = "POST", Path = "user/deleteFriend")
    Call<JsonResponse<Void>> deleteFriend(@HttpParam("friendID") String friendUserID);

    @HttpApi(RequestMethod = "POST", Path = "user/updateRemarkName")
    Call<JsonResponse<Void>> updateRemarkName(@HttpParam("friendID") String friendUserID,
                                              @HttpParam("remarkName") String remarkName);
}
