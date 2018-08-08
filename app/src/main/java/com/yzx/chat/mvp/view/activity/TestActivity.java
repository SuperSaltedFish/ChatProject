package com.yzx.chat.mvp.view.activity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.view.CropImageView;
import com.yzx.chat.widget.view.VisualizerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.rong.common.FileUtils;


public class TestActivity extends BaseCompatActivity {

    private CropImageView mCropImageView;
    private ImageView mImageView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCropImageView = findViewById(R.id.aaa);
        mImageView = findViewById(R.id.bbb);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }


    public void onClick(View v) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Bitmap bitmap = mCropImageView.crop();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            byte[] data = outputStream.toByteArray();
            outputStream.flush();
            outputStream.close();
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onClick1(View v) {

    }
}


