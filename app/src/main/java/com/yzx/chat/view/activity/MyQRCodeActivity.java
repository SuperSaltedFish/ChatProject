package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.MyQRCodeActivityContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.presenter.MyQRCodeActivityPresenter;
import com.yzx.chat.tool.UserManager;
import com.yzx.chat.util.QREncodingUtils;

/**
 * Created by YZX on 2018年02月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class MyQRCodeActivity extends BaseCompatActivity<MyQRCodeActivityContract.Presenter> implements MyQRCodeActivityContract.View {

    private ImageView mIvQRCode;
    private ProgressBar mProgressBar;
    private FrameLayout mFlScan;
    private FrameLayout mFlReset;
    private FrameLayout mFlSave;
    private TextView mTvLocation;
    private TextView mTvNickname;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_my_qrcode;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mIvQRCode = findViewById(R.id.MyQRCodeActivity_mIvQRCode);
        mProgressBar = findViewById(R.id.MyQRCodeActivity_mProgressBar);
        mFlScan = findViewById(R.id.MyQRCodeActivity_mFlScan);
        mFlReset = findViewById(R.id.MyQRCodeActivity_mFlReset);
        mFlSave = findViewById(R.id.MyQRCodeActivity_mFlSave);
        mTvLocation = findViewById(R.id.MyQRCodeActivity_mTvLocation);
        mTvNickname = findViewById(R.id.MyQRCodeActivity_mTvNickname);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        UserBean user = IMClient.getInstance().userManager().getUser();
        if (user == null) {
            finish();
            return;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFlScan.setOnClickListener(mOnScanClickListener);
        mFlReset.setOnClickListener(mOnResetClickListener);
        mFlSave.setOnClickListener(mOnSaveClickListener);

        mTvLocation.setText(user.getLocation());
        mTvNickname.setText(user.getNickname());

        mPresenter.updateQRCode();

    }

    private final View.OnClickListener mOnScanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mOnResetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mProgressBar.setVisibility(View.VISIBLE);
            mIvQRCode.setVisibility(View.INVISIBLE);
            mPresenter.updateQRCode();
        }
    };

    private final View.OnClickListener mOnSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public MyQRCodeActivityContract.Presenter getPresenter() {
        return new MyQRCodeActivityPresenter();
    }

    @Override
    public void showQRCode(String content) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mIvQRCode.setVisibility(View.VISIBLE);
        mIvQRCode.setImageBitmap(QREncodingUtils.createQRCode(content, 200, 200, null));
    }

    @Override
    public void showError(String error) {
        showToast(error);
        finish();
    }
}
