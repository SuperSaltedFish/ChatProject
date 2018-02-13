package com.yzx.chat.network.api.contact;

import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;
import com.yzx.chat.network.framework.HttpParam;

/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public interface ContactApi {
    @HttpApi(RequestMethod = "POST", Path = "contact/getUserContacts")
    Call<JsonResponse<GetUserContactsBean>> getUserContacts();

    @HttpApi(RequestMethod = "POST", Path = "contact/requestContact")
    Call<JsonResponse<Void>> requestContact(@HttpParam("contactID") String contactID, @HttpParam("reason") String reason);

    @HttpApi(RequestMethod = "POST", Path = "contact/rejectContact")
    Call<JsonResponse<Void>> rejectContact(@HttpParam("contactID") String contactID, @HttpParam("reason") String reason);

    @HttpApi(RequestMethod = "POST", Path = "contact/acceptContact")
    Call<JsonResponse<Void>> acceptContact(@HttpParam("contactID") String contactID);

    @HttpApi(RequestMethod = "POST", Path = "contact/deleteContact")
    Call<JsonResponse<Void>> deleteContact(@HttpParam("contactID") String contactID);

    @HttpApi(RequestMethod = "POST", Path = "contact/updateRemark")
    Call<JsonResponse<Void>> updateRemark(@HttpParam("contactID") String friendUserID,
                                          @HttpParam("remark") ContactRemarkBean contactRemark);
}
