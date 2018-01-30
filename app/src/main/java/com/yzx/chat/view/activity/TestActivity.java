package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;

import com.yzx.chat.util.RSAUtil;
import com.yzx.chat.widget.view.Alerter;
import com.yzx.chat.widget.view.FlowLayout;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;


public class TestActivity extends BaseCompatActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init() {

    }

    private EditText mEditText;
    private FlowLayout mFlowLayout;

    @Override
    protected void setup() {


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        UserApi api = (UserApi) ApiManager.getProxyInstance(UserApi.class);
//        Call<JsonResponse<Void>> task = api.addFriend("5a3789ff2889040e1822fe88");
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


//        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
//            @Override
//            public boolean queueIdle() {
//                new Alerter(TestActivity.this,R.layout.item_conversation_single).show();
//                return false;
//            }
//        });
    }

    Alerter mAlerter;

    public void onClick(View v) {
        v.setTranslationZ(13);
    }

    public void onClick2(View v) {
        mAlerter.hide();
    }

    private void testRSA() {
        byte[] data = "ddddddddddd||".getBytes();
        KeyPair pair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(this, "nSDanmadkiD");
        PublicKey publicKey = pair.getPublic();
        PrivateKey privateKey = pair.getPrivate();
        try {
            Log.e("dawd", new String(data));
            data = RSAUtil.encryptByPublicKey(data, publicKey);
            data = RSAUtil.decryptByPrivateKey(data, privateKey);
            Log.e("dawd", new String(data));
//            data = RSAUtil.encryptByPrivateKey(data, privateKey);
//            data = RSAUtil.decryptByPublicKey(data, publicKey);
//            Log.e("dawd", new String(data));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void testHTTP() {

    }


}


