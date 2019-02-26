package com.yzx.chat.core.net.api;

import com.yzx.chat.core.entity.GetUserContactsEntity;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.annotation.POST;
import com.yzx.chat.core.net.framework.annotation.Param;

/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public interface ContactApi {
    @POST("contact/getUserContacts")
    Call<JsonResponse<GetUserContactsEntity>> getUserContacts();

    @POST("contact/requestContact")
    Call<JsonResponse<Void>> requestContact(@Param("contactID") String contactID, @Param("reason") String reason);

    @POST("contact/rejectContact")
    Call<JsonResponse<Void>> refusedContact(@Param("contactID") String contactID, @Param("reason") String reason);

    @POST("contact/acceptContact")
    Call<JsonResponse<UserEntity>> acceptContact(@Param("contactID") String contactID);

    @POST("contact/deleteContact")
    Call<JsonResponse<Void>> deleteContact(@Param("contactID") String contactID);

    @POST("contact/updateRemark")
    Call<JsonResponse<Void>> updateRemark(@Param("contactID") String contactID,
                                          @Param("contactRemark") String contactRemark,
                                          @Param("description") String description,
                                          @Param("telephones") String telephones,
                                          @Param("tags") String tags);
}
