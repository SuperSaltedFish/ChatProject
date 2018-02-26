package com.yzx.chat.view.activity;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.QREncodingUtils;

/**
 * Created by YZX on 2018年02月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class MyQRCodeActivity extends BaseCompatActivity {

    private ImageView mIvQRCode;
    private FrameLayout mFlScan;
    private FrameLayout mFlReset;
    private FrameLayout mFlSave;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_my_qrcode;
    }

    @Override
    protected void init() {
        mIvQRCode = findViewById(R.id.MyQRCodeActivity_mIvQRCode);
        mFlScan = findViewById(R.id.MyQRCodeActivity_mFlScan);
        mFlReset = findViewById(R.id.MyQRCodeActivity_mFlReset);
        mFlSave = findViewById(R.id.MyQRCodeActivity_mFlSave);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFlScan.setOnClickListener(mOnScanClickListener);
        mFlReset.setOnClickListener(mOnResetClickListener);
        mFlSave.setOnClickListener(mOnSaveClickListener);

        mIvQRCode.setImageBitmap(QREncodingUtils.createQRCode("1f13e13a1d321fr351a", 200, 200, null));
    }

    private final View.OnClickListener mOnScanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mOnResetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mOnSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}
