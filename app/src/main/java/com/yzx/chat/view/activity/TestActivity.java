package com.yzx.chat.view.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import com.yzx.chat.bean.ContactBean;

import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.DBHelper;
import com.yzx.chat.network.chat.NetworkAsyncTask;

import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.tool.NotifyManager;
import com.yzx.chat.tool.SharePreferenceManager;
import com.yzx.chat.util.AESUtil;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.RSAUtil;
import com.yzx.chat.widget.view.BadgeTextView;
import com.yzx.chat.widget.view.CarouselView;
import com.yzx.chat.widget.view.EmojiRecyclerview;
import com.yzx.chat.widget.view.NineGridImageView;

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
        CarouselView carouselView = findViewById(R.id.sss);
        List<String> s = new ArrayList<>();
        s.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=950004826,3164354653&fm=27&gp=0.jpg");
        s.add("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2260926939,1550208231&fm=27&gp=0.jpg");
        carouselView.setPicUrls(s);

       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {
               startActivity(new Intent(TestActivity.this,FindNewContactActivity.class));
           }
       }, 3000);

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


