package com.yzx.chat.mvp.view.fragment;

import android.os.Bundle;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;

/**
 * Created by YZX on 2017年09月02日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class SettingFragment extends BaseFragment {
    @Override
    protected int getLayoutID() {
        return R.layout.fragment_setting;
    }

    @Override
    protected void init(View parentView) {
//        Button button = parentView.findViewById(R.id.mButton);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                        UserApi api = (UserApi) ApiManager.getProxyInstance(UserApi.class);
//        Call<JsonResponse<Void>> task = api.requestAddFriend("5a3789ff2889040e1822fe88","我想干你");
//        task.setCallback(new BaseHttpCallback<Void>() {
//            @Override
//            protected void onSuccess(Void response) {
//                LogUtil.e("onSuccess");
//
//            }
//
//            @Override
//            protected void onFailure(String message) {
//                LogUtil.e("onFailure");
//            }
//        });
//        NetworkExecutor.getInstance().submit(task);
//
//            }
//        });
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }

}
