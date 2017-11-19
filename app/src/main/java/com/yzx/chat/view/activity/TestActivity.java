package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.util.Log;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;

import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.DBHelper;
import com.yzx.chat.network.chat.NetworkAsyncTask;

import com.yzx.chat.tool.DBManager;
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

        ContactBean bean = new ContactBean();
        bean.setUserTo("2");
        bean.setUserFrom("26");
        bean.setType(ContactBean.CONTACT_TYPE_INVITED);
        bean.setReason("5");
        ContactDao contactDao = DBManager.getInstance().getContactDao();
        LogUtil.e("awdwadwad:"+contactDao.update(bean));
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


