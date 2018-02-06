package com.yzx.chat.view.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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
    private LinearLayout mLlSexSelected;
    private TextView mTvSexSelected;
    private LinearLayout mLlLocation;
    private TextView mTvLocation;
    private UserBean mUserBean;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_profile_modify;
    }

    @Override
    protected void init() {
        mLlBirthday = findViewById(R.id.ProfileModifyActivity_mLlBirthday);
        mTvBirthday = findViewById(R.id.ProfileModifyActivity_mTvBirthday);
        mLlSexSelected = findViewById(R.id.ProfileModifyActivity_mLlSexSelected);
        mTvSexSelected = findViewById(R.id.ProfileModifyActivity_mTvSexSelected);
        mLlLocation = findViewById(R.id.ProfileModifyActivity_mLlLocation);
        mTvLocation = findViewById(R.id.ProfileModifyActivity_mTvLocation);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLlSexSelected.setOnClickListener(mOnSexSelectedClickListener);
        mLlBirthday.setOnClickListener(mOnBirthdayClickListener);
        mLlLocation.setOnClickListener(mOnLocationClickListener);

        setData();
    }

    private void setData() {
        mUserBean = new UserBean();

    }

    private final View.OnClickListener mOnLocationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mOnSexSelectedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(ProfileModifyActivity.this)
                    .title(R.string.ProfileModifyActivity_SexDialogTitle)
                    .items(R.array.ProfileModifyActivity_SexList)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                            mTvSexSelected.setText(text);
                        }
                    })
                    .show();
        }
    };


    private final View.OnClickListener mOnBirthdayClickListener = new View.OnClickListener() {
        private DatePickerDialog mDatePickerDialog;

        @Override
        public void onClick(View v) {
            if (mDatePickerDialog == null) {
                mDatePickerDialog = new DatePickerDialog(ProfileModifyActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mTvBirthday.setText(String.format(getString(R.string.ProfileModifyActivity_DateFormat), year, month, dayOfMonth));
                    }
                }, -1, -1, -1);
            }
            Calendar calendar = Calendar.getInstance();
            mDatePickerDialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            mDatePickerDialog.setTitle(getString(R.string.ProfileModifyActivity_BirthdayDialogTitle));
            mDatePickerDialog.show();
        }
    };
}
