package com.yzx.chat.view.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.util.AndroidUtil;

import java.lang.reflect.Field;
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
    private LinearLayout mLlSignature;
    private TextView mTvSignature;
    private LinearLayout mLlNickname;
    private EditText mEtNickname;
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
        mLlSignature = findViewById(R.id.ProfileModifyActivity_mLlSignature);
        mTvSignature = findViewById(R.id.ProfileModifyActivity_mTvSignature);
        mLlNickname = findViewById(R.id.ProfileModifyActivity_mLlNickname);
        mEtNickname = findViewById(R.id.ProfileModifyActivity_mEtNickname);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLlSexSelected.setOnClickListener(mOnSexSelectedClickListener);
        mLlBirthday.setOnClickListener(mOnBirthdayClickListener);
        mLlLocation.setOnClickListener(mOnLocationClickListener);
        mLlSignature.setOnClickListener(mOnSignatureClickListener);
        setData();
    }

    private void setData() {
        mUserBean = new UserBean();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SignatureEditActivity.RESULT_CODE && data != null) {
            String newSignature = data.getStringExtra(SignatureEditActivity.INTENT_EXTRA_SIGNATURE_CONTENT);
            if (TextUtils.isEmpty(newSignature)) {
                mTvSignature.setText(R.string.ProfileModifyActivity_NoSet);
            } else {
                mTvSignature.setText(newSignature);
            }
            mUserBean.setSignature(newSignature);
        }
    }


    private final View.OnClickListener mOnSexSelectedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(ProfileModifyActivity.this)
                    .title(R.string.ProfileModifyActivity_SexDialogTitle)
                    .items(R.array.ProfileModifyActivity_SexList)
                    .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            if (!TextUtils.isEmpty(text)) {
                                mTvSexSelected.setText(text);
                                return true;
                            }
                            return false;
                        }
                    })
                    .positiveText(R.string.Confirm)
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

    private final View.OnClickListener mOnLocationClickListener = new View.OnClickListener() {
        private MaterialDialog mLocationSelectorDialog;
        private NumberPicker mProvincePicker;
        private NumberPicker mCityPicker;

        private String[] mCities = {"北京", "上海", "广州", "深圳", "杭州", "青岛", "西安"};

        @Override
        public void onClick(View v) {
            if (mLocationSelectorDialog == null) {
                mLocationSelectorDialog = new MaterialDialog.Builder(ProfileModifyActivity.this)
                        .title("请选择省市")
                        .customView(R.layout.dialog_location_selector, false)
                        .positiveText(R.string.Confirm)
                        .build();
                View view = mLocationSelectorDialog.getCustomView();
                mProvincePicker = view.findViewById(R.id.LocationSelectorDialog_mNpProvince);
                mCityPicker = view.findViewById(R.id.LocationSelectorDialog_mNpCity);

                mProvincePicker.setDisplayedValues(mCities);//设置需要显示的数组
                mProvincePicker.setMinValue(0);
                mProvincePicker.setMaxValue(mCities.length - 1);

            }
            mLocationSelectorDialog.show();
        }
    };

    private final View.OnClickListener mOnSignatureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ProfileModifyActivity.this, SignatureEditActivity.class);
            intent.putExtra(SignatureEditActivity.INTENT_EXTRA_SIGNATURE_CONTENT, mUserBean.getSignature());
            startActivityForResult(intent, 0);
        }
    };

}
