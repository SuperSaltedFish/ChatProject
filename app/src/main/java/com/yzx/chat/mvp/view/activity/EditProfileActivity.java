package com.yzx.chat.mvp.view.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.CityBean;
import com.yzx.chat.bean.ProvinceBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.ProfileModifyContract;
import com.yzx.chat.mvp.presenter.ProfileModifyPresenter;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.GsonUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.view.ClearEditText;
import com.yzx.chat.widget.view.ProgressDialog;
import com.yzx.chat.widget.view.RoundImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2018年02月05日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class EditProfileActivity extends BaseCompatActivity<ProfileModifyContract.Presenter> implements ProfileModifyContract.View {

    private ProgressDialog mProgressDialog;
    private ImageView mIvAvatarBackground;
    private RoundImageView mIvChangeAvatar;
    private ClearEditText mEtNickname;
    private TextView mTvBirthday;
    private TextView mTvLocation;
    private TextView mTvSex;
    private EditText mEtSignature;
    private ClearEditText mEtEmail;
    private UserBean mUserBean;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_edit_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mIvAvatarBackground = findViewById(R.id.EditProfileActivity_mIvAvatarBackground);
        mIvChangeAvatar = findViewById(R.id.EditProfileActivity_mIvChangeAvatar);
        mTvBirthday = findViewById(R.id.EditProfileActivity_mTvBirthday);
        mTvLocation = findViewById(R.id.EditProfileActivity_mTvLocation);
        mEtNickname = findViewById(R.id.EditProfileActivity_mEtNickname);
        mTvSex = findViewById(R.id.EditProfileActivity_mTvSex);
        mEtSignature = findViewById(R.id.EditProfileActivity_mEtSignature);
        mEtEmail = findViewById(R.id.EditProfileActivity_mEtEmail);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Save));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mIvChangeAvatar.setOnClickListener(mOnAvatarClickListener);
        mTvSex.setOnClickListener(mOnSexSelectedClickListener);
        mTvBirthday.setOnClickListener(mOnBirthdayClickListener);
        mTvLocation.setOnClickListener(mOnLocationClickListener);


        mEtNickname.setAutoShow(false);
        mEtEmail.setAutoShow(false);

        mIvChangeAvatar.setRoundRadius(AndroidUtil.dip2px(16));
        GlideUtil.loadBlurFromUrl(this, mIvAvatarBackground, R.drawable.temp_head_image, 4);

        setData();
    }

    private void setData() {
        mUserBean = mPresenter.getUserInfo();
        if (mUserBean == null || mUserBean.isEmpty()) {
            finish();
            return;
        }

        mEtNickname.setText(mUserBean.getNickname());
        if (TextUtils.isEmpty(mUserBean.getLocation())) {
            mTvLocation.setText(R.string.EditProfileActivity_NoSet);
        } else {
            mTvLocation.setText(mUserBean.getLocation());
        }

        if (TextUtils.isEmpty(mUserBean.getSignature())) {
            mEtSignature.setText(null);
        } else {
            mEtSignature.setText(mUserBean.getSignature());
        }

        switch (mUserBean.getSex()) {
            case 1:
                mTvSex.setText(R.string.Man);
                break;
            case 2:
                mTvSex.setText(R.string.Woman);
                break;
            default:
                mTvSex.setText(R.string.EditProfileActivity_NoSet);
        }

        String birthday = mUserBean.getBirthday();
        if (!TextUtils.isEmpty(mUserBean.getBirthday())) {
            birthday = DateUtil.isoFormatTo(getString(R.string.DateFormat_yyyyMMdd), birthday);
            if (!TextUtils.isEmpty(birthday)) {
                mTvBirthday.setText(birthday);
            } else {
                mTvBirthday.setText(R.string.EditProfileActivity_NoSet);
            }
        } else {
            mTvBirthday.setText(R.string.EditProfileActivity_NoSet);
        }
    }

    private void save() {
        String none = getString(R.string.EditProfileActivity_NoSet);
        String nickname = mEtNickname.getText().toString();
        String location = mTvLocation.getText().toString();
        String signature = mEtSignature.getText().toString();

        String birthday = mTvBirthday.getText().toString();
        String strSex = mTvSex.getText().toString();
        int sex = mUserBean.getSex();

        if (!none.equals(birthday)) {
            birthday = DateUtil.formatToISO(getString(R.string.DateFormat_yyyyMMdd), birthday);
            if (birthday == null) {
                LogUtil.e("date formatToISO fail");
            }
        }else {
            birthday=null;
        }
        if(!strSex.equals(none)){
            sex = mTvSex.getText().equals(getString(R.string.Woman)) ? UserBean.SEX_WOMAN : UserBean.SEX_MAN;
        }


        if (TextUtils.isEmpty(nickname)) {
            mEtNickname.setError(getString(R.string.EditProfileActivity_NoneNickname));
            return;
        }
        boolean isChange = false;
        if (!nickname.equals(mUserBean.getNickname())) {
            isChange = true;
            mUserBean.setNickname(nickname);
        }
        if (!none.equals(location)&&!location.equals(mUserBean.getLocation())) {
            isChange = true;
            mUserBean.setLocation(location);
        }
        if (!signature.equals(mUserBean.getSignature())) {
            isChange = true;
            mUserBean.setSignature(signature);
        }
        if (sex != mUserBean.getSex()) {
            isChange = true;
            mUserBean.setSex(sex);
        }

        if (birthday!=null&&!birthday.equals(mUserBean.getBirthday())) {
            isChange = true;
            mUserBean.setBirthday(birthday);
        }

        if (isChange) {
            mProgressDialog.show();
            mPresenter.updateProfile(mUserBean);
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_modify, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ProfileModifyMenu_Save) {
            save();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (resultCode == ImageSingleSelectorActivity.RESULT_CODE) {
            String imagePath = data.getStringExtra(ImageSingleSelectorActivity.INTENT_EXTRA_IMAGE_PATH);
            if (!TextUtils.isEmpty(imagePath)) {
                Intent intent = new Intent(this, CropImageActivity.class);
                intent.putExtra(CropImageActivity.INTENT_EXTRA_IMAGE_PATH, imagePath);
                startActivityForResult(intent, 0);
            }
        }

    }

    private final View.OnClickListener mOnAvatarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(EditProfileActivity.this, ImageSingleSelectorActivity.class), 0);
        }
    };

    private final View.OnClickListener mOnSexSelectedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(EditProfileActivity.this)
                    .title(R.string.EditProfileActivity_SexDialogTitle)
                    .items(R.array.EditProfileActivity_SexList)
                    .itemsCallbackSingleChoice(mUserBean.getSex() - 1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            if (!TextUtils.isEmpty(text)) {
                                mTvSex.setText(text);
                                mUserBean.setSex(which + 1);
                                return true;
                            }
                            return false;
                        }
                    })
                    .positiveText(R.string.Confirm)
                    .negativeText(R.string.Cancel)
                    .show();

        }
    };

    private final View.OnClickListener mOnBirthdayClickListener = new View.OnClickListener() {
        private DatePickerDialog mDatePickerDialog;
        private Calendar mCalendar;

        @Override
        public void onClick(View v) {
            if (mDatePickerDialog == null) {
                mDatePickerDialog = new DatePickerDialog(EditProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mTvBirthday.setText(String.format(getString(R.string.EditProfileActivity_DateFormat), year, month + 1, dayOfMonth));
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
            mDatePickerDialog.setTitle(getString(R.string.EditProfileActivity_BirthdayDialogTitle));
            mDatePickerDialog.show();
        }
    };

    private final View.OnClickListener mOnLocationClickListener = new View.OnClickListener() {
        private MaterialDialog mLocationSelectorDialog;
        private NumberPicker mProvincePicker;
        private NumberPicker mCityPicker;

        private String[] mProvinceArray;
        private Map<String, String[]> mProvinceCityMap;

        @Override
        public void onClick(View v) {
            if (mLocationSelectorDialog == null) {
                List<ProvinceBean> provinceList = GsonUtil.readJsonStream(getResources().openRawResource(R.raw.city));
                if (provinceList == null || provinceList.size() == 0) {
                    showToast(getString(R.string.EditProfileActivity_ReadCityInfoError));
                    return;
                }
                mProvinceCityMap = new HashMap<>(provinceList.size() * 2);
                ProvinceBean province;
                mProvinceArray = new String[provinceList.size()];
                for (int n = 0, count = provinceList.size(); n < count; n++) {
                    province = provinceList.get(n);
                    ArrayList<CityBean> cityList = province.getCity();
                    if (cityList != null && cityList.size() > 0) {
                        mProvinceArray[n] = province.getProvince();
                        String[] strCityArray = new String[cityList.size()];
                        for (int i = 0, size = cityList.size(); i < size; i++) {
                            strCityArray[i] = cityList.get(i).getCountry();
                        }
                        mProvinceCityMap.put(province.getProvince(), strCityArray);
                    }

                }


                mLocationSelectorDialog = new MaterialDialog.Builder(EditProfileActivity.this)
                        .title(R.string.EditProfileActivity_LocationDialogTitle)
                        .customView(R.layout.dialog_location_selector, false)
                        .positiveText(R.string.Confirm)
                        .negativeText(R.string.Cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                int provinceIndex = mProvincePicker.getValue();
                                int cityIndex = mCityPicker.getValue();
                                mUserBean.setLocation(mProvinceArray[provinceIndex] + " " + mProvinceCityMap.get(mProvinceArray[provinceIndex])[cityIndex]);
                                mTvLocation.setText(mUserBean.getLocation());
                            }
                        })
                        .build();

                View view = mLocationSelectorDialog.getCustomView();
                mProvincePicker = view.findViewById(R.id.LocationSelectorDialog_mNpProvince);
                mCityPicker = view.findViewById(R.id.LocationSelectorDialog_mNpCity);

                mProvincePicker.setDisplayedValues(mProvinceArray);//设置需要显示的数组
                mProvincePicker.setMinValue(0);
                mProvincePicker.setMaxValue(mProvinceArray.length - 1);
                mProvincePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        String[] cityArray = mProvinceCityMap.get(mProvinceArray[newVal]);
                        mCityPicker.setValue(0);
                        mCityPicker.setDisplayedValues(cityArray);//设置需要显示的数组
                        mCityPicker.setMinValue(0);
                        mCityPicker.setMaxValue(cityArray.length - 1);
                    }
                });

                String[] defaultCityArray = mProvinceCityMap.get(mProvinceArray[0]);
                mCityPicker.setDisplayedValues(defaultCityArray);//设置需要显示的数组
                mCityPicker.setMinValue(0);
                mCityPicker.setMaxValue(defaultCityArray.length - 1);

            }

            int provinceIndex = 0;
            int cityIndex = 0;
            if (!TextUtils.isEmpty(mUserBean.getLocation())) {
                String[] location = mUserBean.getLocation().split(" ");
                if (location.length == 2) {
                    for (int i = 0, size = mProvinceArray.length; i < size; i++) {
                        if (location[0].equals(mProvinceArray[i])) {
                            provinceIndex = i;
                            String[] cityList = mProvinceCityMap.get(location[0]);
                            for (int j = 0, length = cityList.length; j < length; j++) {
                                if (location[1].equals(cityList[j])) {
                                    cityIndex = j;
                                    mCityPicker.setDisplayedValues(cityList);//设置需要显示的数组
                                    mCityPicker.setMinValue(0);
                                    mCityPicker.setMaxValue(cityList.length - 1);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (provinceIndex >= 0 && cityIndex >= 0) {
                mProvincePicker.setValue(provinceIndex);
                mCityPicker.setValue(cityIndex);
            }

            mLocationSelectorDialog.show();
        }
    };

    @Override
    public ProfileModifyContract.Presenter getPresenter() {
        return new ProfileModifyPresenter();
    }

    @Override
    public void showError(String error) {
        mProgressDialog.dismiss();
        showToast(error);
    }

    @Override
    public void goBack() {
        mProgressDialog.dismiss();
        finish();
    }
}
