package com.yzx.chat.view.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.ProfileModifyContract;
import com.yzx.chat.presenter.ProfileModifyPresenter;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.DateUtil;

import java.util.Calendar;

/**
 * Created by YZX on 2018年02月05日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ProfileModifyActivity extends BaseCompatActivity<ProfileModifyContract.Presenter> implements ProfileModifyContract.View {

    private LinearLayout mLlBirthday;
    private TextView mTvBirthday;
    private LinearLayout mLlSexSelected;
    private TextView mTvSexSelected;
    private LinearLayout mLlLocation;
    private TextView mTvLocation;
    private LinearLayout mLlSignature;
    private TextView mTvSignature;
    private EditText mEtNickname;
    private Button mBtnConfirm;
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
        mEtNickname = findViewById(R.id.ProfileModifyActivity_mEtNickname);
        mBtnConfirm = findViewById(R.id.ProfileModifyActivity_mBtnConfirm);
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
        mBtnConfirm.setOnClickListener(mOnConfirmListener);
        setData();
    }

    private void setData() {
        mUserBean = IdentityManager.getInstance().getUser();
        if (mUserBean == null || mUserBean.isEmpty()) {
            finish();
            return;
        }
        mUserBean = (UserBean) mUserBean.clone();

        mEtNickname.setText(mUserBean.getNickname());
        if (TextUtils.isEmpty(mUserBean.getLocation())) {
            mTvLocation.setText(R.string.ProfileModifyActivity_NoSet);
        } else {
            mTvLocation.setText(mUserBean.getLocation());
        }

        if (TextUtils.isEmpty(mUserBean.getSignature())) {
            mTvSignature.setText(R.string.ProfileModifyActivity_NoSet);
        } else {
            mTvSignature.setText(mUserBean.getSignature());
        }

        switch (mUserBean.getSex()) {
            case 1:
                mTvSexSelected.setText(R.string.ProfileModifyActivity_Man);
                break;
            case 2:
                mTvSexSelected.setText(R.string.ProfileModifyActivity_Woman);
                break;
            default:
                mTvSexSelected.setText(R.string.ProfileModifyActivity_NoSet);
        }


        if (TextUtils.isEmpty(mUserBean.getBirthday())) {
            mTvBirthday.setText(R.string.ProfileModifyActivity_NoSet);
        } else {
            String date = DateUtil.isoToDate_yyyy_MM_dd(mUserBean.getBirthday());
            if (TextUtils.isEmpty(date)) {
                mTvBirthday.setText(R.string.ProfileModifyActivity_NoSet);
            } else {
                mTvBirthday.setText(date);
            }
        }
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

    private final View.OnClickListener mOnConfirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String nickname = mEtNickname.getText().toString();
            if (TextUtils.isEmpty(nickname)) {
                mEtNickname.setError(getString(R.string.ProfileModifyActivity_NoneNickname));
                return;
            }
            boolean isChangle = false;
            UserBean userBean = IdentityManager.getInstance().getUser();
            if (!userBean.getNickname().equals(nickname)) {
                isChangle = true;
            }
            if (!userBean.getLocation().equals(mUserBean.getLocation())) {
                isChangle = true;
            }
            if (!userBean.getSignature().equals(mUserBean.getSignature())) {
                isChangle = true;
            }
            if (userBean.getSex() != mUserBean.getSex()) {
                isChangle = true;
            }
            if (!userBean.getBirthday().equals(mUserBean.getBirthday())) {
                isChangle = true;
            }
            if (isChangle) {
                mPresenter.updateProfile(nickname, mUserBean.getSex(), mUserBean.getBirthday(), mUserBean.getLocation(), mUserBean.getSignature());
            } else {
                finish();
            }
        }
    };


    private final View.OnClickListener mOnSexSelectedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(ProfileModifyActivity.this)
                    .title(R.string.ProfileModifyActivity_SexDialogTitle)
                    .items(R.array.ProfileModifyActivity_SexList)
                    .itemsCallbackSingleChoice(mUserBean.getSex()-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            if (!TextUtils.isEmpty(text)) {
                                mTvSexSelected.setText(text);
                                mUserBean.setSex(which + 1);
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
        private Calendar mCalendar;

        @Override
        public void onClick(View v) {
            if (mDatePickerDialog == null) {
                mDatePickerDialog = new DatePickerDialog(ProfileModifyActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mTvBirthday.setText(String.format(getString(R.string.ProfileModifyActivity_DateFormat), year, month, dayOfMonth));
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, month);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        mUserBean.setBirthday(DateUtil.msecToISO(mCalendar.getTimeInMillis()));
                    }
                }, -1, -1, -1);
            }
            if (mCalendar == null) {
                if (TextUtils.isEmpty(mUserBean.getBirthday())) {
                    mCalendar = Calendar.getInstance();
                } else {
                    mCalendar = DateUtil.isoToCalendar(mUserBean.getBirthday());
                }
            }
            mDatePickerDialog.updateDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
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
            mUserBean.setLocation("广东 韶关");
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

    @Override
    public ProfileModifyContract.Presenter getPresenter() {
        return new ProfileModifyPresenter();
    }
}
