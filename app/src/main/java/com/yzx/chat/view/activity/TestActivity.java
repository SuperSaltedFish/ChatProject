package com.yzx.chat.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.broadcast.NetworkStateReceive;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.NetworkAsyncTask;

import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.LogUtil;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EMClient.getInstance().logout(true);
        EMClient.getInstance().login("244546875", "12345678", new EMCallBack() {
            @Override
            public void onSuccess() {
                LogUtil.e("dwadwd");
            }

            @Override
            public void onError(int code, String error) {
                LogUtil.e("dwadwd");
            }

            @Override
            public void onProgress(int progress, String status) {
                LogUtil.e("dwadwd");
            }
        });

    }

    public void onClick(View v) {
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
        protected void onPostExecute(String s, Object lifeCycleObject) {
            Log.e("onPostExecute", " adwd " + lifeCycleObject.toString());
        }
    }


}


