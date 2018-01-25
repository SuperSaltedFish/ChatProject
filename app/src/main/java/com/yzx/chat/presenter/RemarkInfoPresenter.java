package com.yzx.chat.presenter;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.contract.RemarkInfoContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkUtil;

/**
 * Created by YZX on 2018年01月24日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class RemarkInfoPresenter implements RemarkInfoContract.Presenter {

    private Call<JsonResponse<Void>> mUpdateRemarkCall;
    private UserApi mUserApi;

    @Override
    public void attachView(RemarkInfoContract.View view) {
        mUserApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
    }

    @Override
    public void detachView() {

    }

    @Override
    public void save(ContactBean contact) {
        contact.getRemark().setUploadFlag(0);
        DBManager.getInstance().getContactDao().replace(contact);
        NetworkUtil.cancelCall(mUpdateRemarkCall);
        mUpdateRemarkCall = mUserApi.updateRemark(contact.getUserID(),contact.getRemark());
        mUpdateRemarkCall.setCallback(new UpdateResponseCallback(contact));
        NetworkExecutor.getInstance().submit(mUpdateRemarkCall);
        IMClient.getInstance().contactManager().updateContact(contact);
    }

    private final static class UpdateResponseCallback extends BaseHttpCallback<Void>{

        private ContactBean mContactBean;

        private UpdateResponseCallback(ContactBean contact) {
            mContactBean = contact;
        }

        @Override
        protected void onSuccess(Void response) {
            mContactBean.getRemark().setUploadFlag(1);
            IMClient.getInstance().contactManager().updateContact(mContactBean,false);
        }

        @Override
        protected void onFailure(String message) {
            LogUtil.e(message);
        }
    }

}
