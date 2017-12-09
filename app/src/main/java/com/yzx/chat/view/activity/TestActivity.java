package com.yzx.chat.view.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;

import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.DBHelper;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.GetUserFriendsBean;
import com.yzx.chat.network.api.user.GetUserProfileBean;
import com.yzx.chat.network.api.user.SearchUserBean;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.NetworkAsyncTask;

import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.tool.NotifyManager;
import com.yzx.chat.tool.SharePreferenceManager;
import com.yzx.chat.util.AESUtil;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkUtil;
import com.yzx.chat.util.RSAUtil;
import com.yzx.chat.util.VoiceRecorder;
import com.yzx.chat.widget.view.BadgeTextView;
import com.yzx.chat.widget.view.CarouselView;
import com.yzx.chat.widget.view.EmojiRecyclerview;
import com.yzx.chat.widget.view.NineGridImageView;
import com.yzx.chat.widget.view.RecorderButton;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;


public class TestActivity extends BaseCompatActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final VoiceRecorder voiceRecorder = new VoiceRecorder(Environment.getExternalStorageDirectory() + "/a/a.amr", 1000 * 60, new VoiceRecorder.OnRecorderStateListener() {
            @Override
            public void onAmplitudeChange(int amplitude) {
                LogUtil.e("" + amplitude);
            }

            @Override
            public void onComplete(String filePath, long duration) {
                LogUtil.e("onComplete");
            }

            @Override
            public void onError(String error) {
                LogUtil.e("onError:" + error);
            }
        });


        RecorderButton button = findViewById(R.id.rm);
        button.setOnRecorderTouchListener(new RecorderButton.onRecorderTouchListener() {
            @Override
            public void onStart() {
                voiceRecorder.prepare();
                voiceRecorder.start();
            }

            @Override
            public void onStop() {
                voiceRecorder.stop();
            }

            @Override
            public void onCancel() {
                LogUtil.e(voiceRecorder.cancelAndDelete() + " ");
            }
        });

//        UserApi api = (UserApi) ApiManager.getProxyInstance(UserApi.class);
//        Call<JsonResponse<GetUserProfileBean>> task = api.getUserProfile("baiz2m0mnjyB6YYyMPki9PSe2sByw7Pm7zMcGwhSi87kKajiph7t7ySaSF0TxRH3");
//        task.setCallback(new BaseHttpCallback<GetUserProfileBean>() {
//            @Override
//            protected void onSuccess(GetUserProfileBean response) {
//                LogUtil.e("dwadwd");
//            }
//
//            @Override
//            protected void onFailure(String message) {
//                    LogUtil.e("dwadwd");
//            }
//        });
//        NetworkExecutor.getInstance().submit(task);


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


