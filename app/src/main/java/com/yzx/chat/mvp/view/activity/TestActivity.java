package com.yzx.chat.mvp.view.activity;

import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.View;

import com.amap.api.maps.MapView;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.view.CarouselView;
import com.yzx.chat.widget.view.RoundLinearLayout;

import java.util.ArrayList;
import java.util.List;


public class TestActivity extends BaseCompatActivity {

    private CarouselView mCarouselView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCarouselView = findViewById(R.id.idid);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

        final List<Object> ss = new ArrayList<>();
        ss.add("https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=2416694947,3049436541&fm=173&app=25&f=JPEG?w=600&h=432&s=E61C1DC74CD29C98DCA9A57E0300D07B");
        ss.add("https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=4150293542,4129468158&fm=173&app=25&f=JPEG?w=547&h=740&s=2010E033199EC4CE12F525DB0100C0B2");
        ss.add("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=2614949697,4022265400&fm=173&app=25&f=JPEG?w=600&h=399&s=FB6927D14EF21F9ED3BC000C03007043");
        ss.add("https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=3006195832,3122652020&fm=173&app=25&f=JPEG?w=639&h=394&s=B61370849A74AE5D0A5B9B810300E08C");
        ss.add("https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=2270387543,400560268&fm=173&app=25&f=JPEG?w=640&h=427&s=FFD8E8035C7D2C8C34257D730100E0B0");
        ss.add("https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=2197645708,3566920228&fm=173&app=25&f=JPEG?w=639&h=416&s=20A240B44CC78AD054B5F4BD03001005");
        ss.add("https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=4126090309,142503810&fm=173&app=25&f=JPEG?w=640&h=383&s=8A9010CD7C23BB51183594BC0300C002");
        ss.add("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=485248716,2985800010&fm=173&app=25&f=JPEG?w=454&h=600&s=F79230C244C2C7F34B3D785A0300C0F4");

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                mCarouselView.setPicUrls(ss);
                return false;
            }
        });
        //  mCarouselView.setPicUrls(ss);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void onClick(View v) {


    }

    public void onClick1(View v) {

    }
}


