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

    @HttpApi(RequestMethod = "POST", Path = "user/searchUser")
    Call<JsonResponse<SearchUserBean>> searchUser(String nicknameOrTelephone);

    @HttpApi(RequestMethod = "POST", Path = "user/getUserFriends")
    Call<JsonResponse<GetUserFriendsBean>> getUserFriends();

    @HttpApi(RequestMethod = "POST", Path = "user/addFriend")
    Call<JsonResponse<Void>> addFriend(String friendUserID);

    @HttpApi(RequestMethod = "POST", Path = "user/deleteFriend")
    Call<JsonResponse<Void>> deleteFriend(String friendUserID);

    @HttpApi(RequestMethod = "POST", Path = "user/setFriendRemark")
    Call<JsonResponse<Void>> setFriendRemark(String friendUserID,String Remark);
}
