package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactMessageBean;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.ContactMessageDao;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.util.LogUtil;
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
        mFlowLayout = findViewById(R.id.FlowLayout);
        mEditText = findViewById(R.id.edit);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                   CharSequence ContentType = mEditText.getText();
                   if(TextUtils.isEmpty(ContentType)){
                       return true;
                   }

                   TextView textView = (TextView) LayoutInflater.from(TestActivity.this).inflate(R.layout.item_profile_label,mFlowLayout,false);
                    textView.setText(ContentType);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TextView textView = (TextView) v;
                            textView.setText(textView.getText()+" x");
                        }
                    });
                    int count =mFlowLayout.getChildCount();
                    if(count!=0){
                        count--;
                    }
                    mFlowLayout.addView(textView,count);
                    return true;
                }
                return false;
            }
        });

        mFlowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence ContentType = mEditText.getText();
                if(TextUtils.isEmpty(ContentType)){
                    return ;
                }
                TextView textView = (TextView) LayoutInflater.from(TestActivity.this).inflate(R.layout.item_profile_label,mFlowLayout,false);
                textView.setText(ContentType);
                int count =mFlowLayout.getChildCount();
                if(count!=0){
                    count--;
                }
                mFlowLayout.addView(textView,count);
            }
        });
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


