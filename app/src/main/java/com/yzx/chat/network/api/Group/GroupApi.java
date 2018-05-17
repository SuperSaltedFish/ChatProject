package com.yzx.chat.network.api.Group;

import com.yzx.chat.bean.CreateGroupMemberBean;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;
import com.yzx.chat.network.framework.HttpParam;
import com.yzx.chat.network.framework.RequestType;

import java.util.List;

/**
 * Created by YZX on 2018年02月28日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public interface GroupApi {

    @HttpApi(RequestType = RequestType.POST, url = "group/getGroupList")
    Call<JsonResponse<Void>> getGroupList();

    @HttpApi(RequestType = RequestType.POST, url = "group/rename")
    Call<JsonResponse<Void>> rename(@HttpParam("groupID") String groupID, @HttpParam("name") String newName);

    @HttpApi(RequestType = RequestType.POST, url = "group/updateNotice")
    Call<JsonResponse<Void>> updateNotice(@HttpParam("groupID") String groupID, @HttpParam("notice") String newNotice);

    @HttpApi(RequestType = RequestType.POST, url = "group/updateAlias")
    Call<JsonResponse<Void>> updateAlias(@HttpParam("groupID") String groupID, @HttpParam("alias") String newAlias);

    @HttpApi(RequestType = RequestType.POST, url = "group/create")
    Call<JsonResponse<CreateGroupBean>> createGroup(@HttpParam("name") String groupName, @HttpParam("members") List<CreateGroupMemberBean> memberList);

    @HttpApi(RequestType = RequestType.POST, url = "group/add")
    Call<JsonResponse<CreateGroupBean>> add(@HttpParam("groupID") String groupName, @HttpParam("members") List<CreateGroupMemberBean> memberList);

    @HttpApi(RequestType = RequestType.POST, url = "group/quit")
    Call<JsonResponse<Void>> quit(@HttpParam("groupID") String groupID);

    @HttpApi(RequestType = RequestType.POST, url = "group/getTempGroupID")
    Call<JsonResponse<GetTempGroupID>> getTempGroupID(@HttpParam("groupID") String groupID);

}
