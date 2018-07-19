package com.yzx.chat.network.api.Group;

import android.support.annotation.StringDef;

import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.POST;
import com.yzx.chat.network.framework.Param;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by YZX on 2018年02月28日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public interface GroupApi {

    String JOIN_TYPE_QR_CODE = "QrCode";
    String JOIN_TYPE_DEFAULT = "Default";

    @StringDef({JOIN_TYPE_QR_CODE, JOIN_TYPE_DEFAULT})
    @Retention(RetentionPolicy.SOURCE)
    @interface JoinType {
    }


    @POST("group/rename")
    Call<JsonResponse<Void>> rename(@Param("groupID") String groupID, @Param("name") String newName);

    @POST("group/updateNotice")
    Call<JsonResponse<Void>> updateNotice(@Param("groupID") String groupID, @Param("notice") String newNotice);

    @POST("group/updateAlias")
    Call<JsonResponse<Void>> updateAlias(@Param("groupID") String groupID, @Param("alias") String newAlias);

    @POST("group/create")
    Call<JsonResponse<Void>> createGroup(@Param("name") String groupName, @Param("members") String[] membersID);

    @POST("group/join")
    Call<JsonResponse<Void>> join(@Param("groupID") String groupName, @Param("joinType") @JoinType String joinType);

    @POST("group/add")
    Call<JsonResponse<Void>> add(@Param("groupID") String groupName, @Param("members") String[] membersID);

    @POST("group/quit")
    Call<JsonResponse<Void>> quit(@Param("groupID") String groupID);

    @POST("group/getTempGroupID")
    Call<JsonResponse<GetTempGroupID>> getTempGroupID(@Param("groupID") String groupID);

}
