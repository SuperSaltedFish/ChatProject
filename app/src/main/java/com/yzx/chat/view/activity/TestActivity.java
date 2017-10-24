package com.yzx.chat.view.activity;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.chat.NetworkAsyncTask;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.AuthenticationManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.RSAUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Locale;
import java.util.UUID;


public class TestActivity extends BaseCompatActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String s = AuthenticationManager.getInstance().getDeviceID();
        LogUtil.e(s);
        s = AuthenticationManager.getInstance().getDeviceID();
        LogUtil.e(s);
        // testRSA();
    }

    public void onClick(View v) {

    }

    private void testRSA() {
        byte[] data = "3||".getBytes();
        KeyPair pair = RSAUtil.generateRSAKeyPair();
        PublicKey publicKey = pair.getPublic();
        PrivateKey privateKey = pair.getPrivate();
        try {
            Log.e("dawd", new String(data));
            data = RSAUtil.encryptByPublicKey(data, publicKey);
            data = RSAUtil.decryptByPrivateKey(data, privateKey);
            Log.e("dawd", new String(data));
            data = RSAUtil.encryptByPrivateKey(data, privateKey);
            data = RSAUtil.decryptByPublicKey(data, publicKey);
            Log.e("dawd", new String(data));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void testDiyAsyncTask() {
        String s = new String("dawd");
        ChatTask chatTask = new ChatTask(s);
        chatTask.execute();
    }

    private void testHTTP() {

    }


    private static class ChatTask extends NetworkAsyncTask<Void, String> {

        public ChatTask(Object lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "你好";
        }

        @Override
        protected void onPostExecute(String s, Object mLifeCycleObject) {
            Log.e("onPostExecute", " adwd " + mLifeCycleObject.toString());
        }
    }


}


