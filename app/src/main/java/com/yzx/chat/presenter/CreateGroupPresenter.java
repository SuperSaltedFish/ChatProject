package com.yzx.chat.presenter;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.CreateGroupMemberBean;
import com.yzx.chat.contract.CreateGroupContract;
import com.yzx.chat.network.api.Group.CreateGroupBean;
import com.yzx.chat.network.api.Group.GroupApi;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年02月27日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class CreateGroupPresenter implements CreateGroupContract.Presenter {

    private CreateGroupContract.View mCreateGroupView;
    private Call<JsonResponse<CreateGroupBean>> mCreateGroupCall;
    private GroupApi mGroupApi;

    private boolean isCreating;

    @Override
    public void attachView(CreateGroupContract.View view) {
        mCreateGroupView = view;
        mGroupApi = (GroupApi) ApiHelper.getProxyInstance(GroupApi.class);
    }

    @Override
    public void detachView() {
        mCreateGroupView = null;

    }

    @Override
    public void createGroup(List<ContactBean> members) {
        if (isCreating) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(64);
        List<CreateGroupMemberBean> memberList = new ArrayList<>(members.size());
        for (int i = 0, count = members.size(); i < count; i++) {
            stringBuilder.append(members.get(i).getUserProfile().getNickname());
            if (i != count - 1) {
                stringBuilder.append("、");
            } else {
                stringBuilder.append("的群聊");
            }
            CreateGroupMemberBean groupMemberBean = new CreateGroupMemberBean();
            groupMemberBean.setUserID(members.get(i).getUserProfile().getUserID());
            memberList.add(groupMemberBean);
        }

        mCreateGroupCall = mGroupApi.createGroup(stringBuilder.toString(), memberList);
        mCreateGroupCall.setCallback(new BaseHttpCallback<CreateGroupBean>() {
            @Override
            protected void onSuccess(CreateGroupBean response) {

            }

            @Override
            protected void onFailure(String message) {
                isCreating = false;
                mCreateGroupView.showError(message);
            }
        });
        isCreating = true;
        sHttpExecutor.submit(mCreateGroupCall);


    }
}
