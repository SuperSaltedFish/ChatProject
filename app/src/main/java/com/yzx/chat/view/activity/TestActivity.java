package com.yzx.chat.view.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
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
        EmojiRecyclerview emojiRecyclerview = findViewById(R.id.sss);
        int[] emojiUnicode = new int[0x4F+1];
        StringBuilder stringBuilder = new StringBuilder(128);
        for(int i =0;i<=0x4F;i++){
            emojiUnicode[i] = 0x1F600+i;
        }
        emojiRecyclerview.setEmojiData(emojiUnicode,6);

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


