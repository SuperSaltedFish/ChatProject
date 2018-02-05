package com.yzx.chat.view.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.util.LogUtil;

import java.util.Calendar;

/**
 * Created by YZX on 2018年02月05日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ProfileModifyActivity extends BaseCompatActivity {

    private LinearLayout mLlBirthday;
    private TextView mTvBirthday;
    private UserBean mUserBean;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_profile_modify;
    }

    @Override
    protected void init() {
        mLlBirthday = findViewById(R.id.ProfileModifyActivity_mLlBirthday);
        mTvBirthday = findViewById(R.id.ProfileModifyActivity_mTvBirthday);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mLlBirthday.setOnClickListener(mOnBirthdayClickListener);

        setData();
    }

    private void setData() {
        mUserBean = new UserBean();

    }

    private final View.OnClickListener mOnBirthdayClickListener = new View.OnClickListener() {
        private DatePickerDialog mDatePickerDialog;

        @Override
        public void onClick(View v) {
            if (mDatePickerDialog == null) {
                mDatePickerDialog = new DatePickerDialog(ProfileModifyActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        LogUtil.e(year + " " + month + " " + dayOfMonth);
                    }
                }, -1, -1, -1);
            }
            Calendar calendar = Calendar.getInstance();
            mDatePickerDialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            mDatePickerDialog.show();
        }
    };
}
