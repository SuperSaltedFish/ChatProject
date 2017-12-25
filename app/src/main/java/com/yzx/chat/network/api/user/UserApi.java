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

    @HttpApi(RequestMethod = "POST", Path = "user/searchUser")
    Call<JsonResponse<SearchUserBean>> searchUser(@HttpParam("queryCondition") String nicknameOrTelephone);

    @HttpApi(RequestMethod = "POST", Path = "user/getUserContacts")
    Call<JsonResponse<GetUserFriendsBean>> getUserContacts();

    @HttpApi(RequestMethod = "POST", Path = "user/requestContact")
    Call<JsonResponse<Void>> requestContact(@HttpParam("friendID") String friendUserID, @HttpParam("reason") String reason);

    @HttpApi(RequestMethod = "POST", Path = "user/rejectContact")
    Call<JsonResponse<Void>> rejectContact(@HttpParam("friendID") String friendUserID, @HttpParam("reason") String reason);

    @HttpApi(RequestMethod = "POST", Path = "user/requestContact")
    Call<JsonResponse<Void>> acceptContact(@HttpParam("friendID") String friendUserID);

    @HttpApi(RequestMethod = "POST", Path = "user/deleteContact")
    Call<JsonResponse<Void>> deleteContact(@HttpParam("friendID") String friendUserID);

    @HttpApi(RequestMethod = "POST", Path = "user/updateRemarkName")
    Call<JsonResponse<Void>> updateRemarkName(@HttpParam("friendID") String friendUserID,
                                              @HttpParam("remarkName") String remarkName);
}
