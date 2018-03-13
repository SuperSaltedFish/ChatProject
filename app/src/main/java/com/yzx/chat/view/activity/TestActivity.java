package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.RSAUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;


public class TestActivity extends BaseCompatActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        ImageView imageView = findViewById(R.id.ssss);
        GlideUtil.loadFromUrl(this,imageView,R.drawable.temp_share_image);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        UserApi userApi= (UserApi) ApiHelper.getProxyInstance(UserApi.class);
//        ArrayList<String> strings = new ArrayList<>();
//        strings.add("/storage/emulated/0/DCIM/Camera/IMG_20180127_164621.jpg");
//        Call<JsonResponse<Void>> call = userApi.uploadAvatar(strings);
//        call.setCallback(new BaseHttpCallback<Void>() {
//            @Override
//            protected void onSuccess(Void response) {
//
//            }
//
//            @Override
//            protected void onFailure(String message) {
//
//            }
//        });
//        NetworkExecutor.getInstance().submit(call);
    }

    public void onClick(View v) {

    }

    public void onClick2(View v) {

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


