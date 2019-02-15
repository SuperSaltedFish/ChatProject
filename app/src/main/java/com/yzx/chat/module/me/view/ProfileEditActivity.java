package com.yzx.chat.module.me.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.yzx.chat.core.entity.CityEntity;
import com.yzx.chat.core.entity.ProvinceEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.common.view.ImageSingleSelectorActivity;
import com.yzx.chat.module.me.contract.ProfileEditContract;
import com.yzx.chat.module.me.presenter.ProfileEditPresenter;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.GsonUtil;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.view.RoundImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Created by YZX on 2018年02月05日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class ProfileEditActivity extends BaseCompatActivity<ProfileEditContract.Presenter> implements ProfileEditContract.View {

    private ImageView mIvAvatarBackground;
    private RoundImageView mIvChangeAvatar;
    private ImageView mIvAvatar;
    private EditText mEtNickname;
    private TextView mTvBirthday;
    private TextView mTvLocation;
    private TextView mTvSex;
    private EditText mEtSignature;
    private EditText mEtEmail;
    private UserEntity mUserEntity;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_profile_edit;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mIvAvatarBackground = findViewById(R.id.ProfileEditActivity_mIvAvatarBackground);
        mIvChangeAvatar = findViewById(R.id.ProfileEditActivity_mIvChangeAvatar);
        mTvBirthday = findViewById(R.id.ProfileEditActivity_mTvBirthday);
        mTvLocation = findViewById(R.id.ProfileEditActivity_mTvLocation);
        mEtNickname = findViewById(R.id.ProfileEditActivity_mEtNickname);
        mTvSex = findViewById(R.id.ProfileEditActivity_mTvSex);
        mEtSignature = findViewById(R.id.ProfileEditActivity_mEtSignature);
        mEtEmail = findViewById(R.id.ProfileEditActivity_mEtEmail);
        mIvAvatar = findViewById(R.id.ProfileEditActivity_mIvAvatar);
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

        mIvChangeAvatar.setRoundRadius(AndroidHelper.dip2px(16));


        setData();
    }

    private void setData() {
        mUserEntity = mPresenter.getUserInfo();
        if (mUserEntity == null || mUserEntity.isEmpty()) {
            finish();
            return;
        }

        mEtNickname.setText(mUserEntity.getNickname());
        if (TextUtils.isEmpty(mUserEntity.getLocation())) {
            mTvLocation.setText(R.string.ProfileEditActivity_NoSet);
        } else {
            mTvLocation.setText(mUserEntity.getLocation());
        }

        if (TextUtils.isEmpty(mUserEntity.getSignature())) {
            mEtSignature.setText(null);
        } else {
            mEtSignature.setText(mUserEntity.getSignature());
        }

        switch (mUserEntity.getSex()) {
            case 1:
                mTvSex.setText(R.string.Man);
                break;
            case 2:
                mTvSex.setText(R.string.Woman);
                break;
            default:
                mTvSex.setText(R.string.ProfileEditActivity_NoSet);
        }

        String birthday = mUserEntity.getBirthday();
        if (!TextUtils.isEmpty(mUserEntity.getBirthday())) {
            birthday = DateUtil.isoFormatTo(getString(R.string.DateFormat_yyyyMMdd), birthday);
            if (!TextUtils.isEmpty(birthday)) {
                mTvBirthday.setText(birthday);
            } else {
                mTvBirthday.setText(R.string.ProfileEditActivity_NoSet);
            }
        } else {
            mTvBirthday.setText(R.string.ProfileEditActivity_NoSet);
        }

        setAvatar(mUserEntity.getAvatar());
    }

    private void setAvatar(String url) {
        GlideUtil.loadAvatarFromUrl(this, mIvAvatar, url);
        GlideUtil.loadBlurFromUrl(this, mIvAvatarBackground, url, 4);
    }

    private void save() {
        String none = getString(R.string.ProfileEditActivity_NoSet);
        String nickname = mEtNickname.getText().toString();
        String location = mTvLocation.getText().toString();
        String signature = mEtSignature.getText().toString();

        String birthday = mTvBirthday.getText().toString();
        String strSex = mTvSex.getText().toString();
        int sex = mUserEntity.getSex();

        if (!none.equals(birthday)) {
            birthday = DateUtil.formatToISO(getString(R.string.DateFormat_yyyyMMdd), birthday);
            if (birthday == null) {
                LogUtil.e("date formatToISO fail");
            }
        } else {
            birthday = null;
        }
        if (!strSex.equals(none)) {
            sex = mTvSex.getText().equals(getString(R.string.Woman)) ? UserEntity.SEX_WOMAN : UserEntity.SEX_MAN;
        }


        if (TextUtils.isEmpty(nickname)) {
            mEtNickname.setError(getString(R.string.ProfileEditActivity_NoneNickname));
            return;
        }
        boolean isChange = false;
        if (!nickname.equals(mUserEntity.getNickname())) {
            isChange = true;
            mUserEntity.setNickname(nickname);
        }
        if (!none.equals(location) && !location.equals(mUserEntity.getLocation())) {
            isChange = true;
            mUserEntity.setLocation(location);
        }
        if (!signature.equals(mUserEntity.getSignature())) {
            isChange = true;
            mUserEntity.setSignature(signature);
        }
        if (sex != mUserEntity.getSex()) {
            isChange = true;
            mUserEntity.setSex(sex);
        }

        if (birthday != null && !birthday.equals(mUserEntity.getBirthday())) {
            isChange = true;
            mUserEntity.setBirthday(birthday);
        }

        if (isChange) {
            mPresenter.updateProfile(mUserEntity);
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
        } else if (resultCode == CropImageActivity.RESULT_CODE) {
            String imagePath = data.getStringExtra(CropImageActivity.INTENT_EXTRA_IMAGE_PATH);
            if (!TextUtils.isEmpty(imagePath)) {
                mPresenter.uploadAvatar(imagePath);
            }
        }

    }

    private final View.OnClickListener mOnAvatarClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            startActivityForResult(new Intent(ProfileEditActivity.this, ImageSingleSelectorActivity.class), 0);
        }
    };

    private final View.OnClickListener mOnSexSelectedClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            new MaterialDialog.Builder(ProfileEditActivity.this)
                    .title(R.string.ProfileEditActivity_SexDialogTitle)
                    .items(R.array.ProfileEditActivity_SexList)
                    .itemsCallbackSingleChoice(mUserEntity.getSex() - 1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            if (!TextUtils.isEmpty(text)) {
                                mTvSex.setText(text);
                                mUserEntity.setSex(which + 1);
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

    private final View.OnClickListener mOnBirthdayClickListener = new OnOnlySingleClickListener() {
        private DatePickerDialog mDatePickerDialog;
        private Calendar mCalendar;

        @Override
        public void onSingleClick(View v) {
            if (mCalendar == null) {
                if (TextUtils.isEmpty(mUserEntity.getBirthday())) {
                    mCalendar = Calendar.getInstance();
                } else {
                    mCalendar = DateUtil.isoToCalendar(mUserEntity.getBirthday());
                }
            }
            if (mDatePickerDialog == null) {
                mDatePickerDialog = new DatePickerDialog(ProfileEditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mTvBirthday.setText(String.format(getString(R.string.ProfileEditActivity_DateFormat), year, month + 1, dayOfMonth));
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, month);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        mUserEntity.setBirthday(DateUtil.msecToISO(mCalendar.getTimeInMillis()));
                    }
                }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
            }
            mDatePickerDialog.updateDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
            mDatePickerDialog.setTitle(getString(R.string.ProfileEditActivity_BirthdayDialogTitle));
            mDatePickerDialog.show();
        }
    };

    private final View.OnClickListener mOnLocationClickListener = new OnOnlySingleClickListener() {
        private MaterialDialog mLocationSelectorDialog;
        private NumberPicker mProvincePicker;
        private NumberPicker mCityPicker;

        private String[] mProvinceArray;
        private Map<String, String[]> mProvinceCityMap;

        @Override
        public void onSingleClick(View v) {
            if (mLocationSelectorDialog == null) {
                List<ProvinceEntity> provinceList = GsonUtil.readJsonStream(getResources().openRawResource(R.raw.city));
                if (provinceList == null || provinceList.size() == 0) {
                    showToast(getString(R.string.ProfileEditActivity_ReadCityInfoError));
                    return;
                }
                mProvinceCityMap = new HashMap<>(provinceList.size() * 2);
                ProvinceEntity province;
                mProvinceArray = new String[provinceList.size()];
                for (int n = 0, count = provinceList.size(); n < count; n++) {
                    province = provinceList.get(n);
                    ArrayList<CityEntity> cityList = province.getCity();
                    if (cityList != null && cityList.size() > 0) {
                        mProvinceArray[n] = province.getProvince();
                        String[] strCityArray = new String[cityList.size()];
                        for (int i = 0, size = cityList.size(); i < size; i++) {
                            strCityArray[i] = cityList.get(i).getCountry();
                        }
                        mProvinceCityMap.put(province.getProvince(), strCityArray);
                    }

                }


                mLocationSelectorDialog = new MaterialDialog.Builder(ProfileEditActivity.this)
                        .title(R.string.ProfileEditActivity_LocationDialogTitle)
                        .customView(R.layout.dialog_location_selector, false)
                        .positiveText(R.string.Confirm)
                        .negativeText(R.string.Cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                int provinceIndex = mProvincePicker.getValue();
                                int cityIndex = mCityPicker.getValue();
                                mUserEntity.setLocation(mProvinceArray[provinceIndex] + " " + mProvinceCityMap.get(mProvinceArray[provinceIndex])[cityIndex]);
                                mTvLocation.setText(mUserEntity.getLocation());
                            }
                        })
                        .build();

                View view = mLocationSelectorDialog.getCustomView();
                mProvincePicker = view.findViewById(R.id.LocationSelectorDialog_mNpProvince);
                mCityPicker = view.findViewById(R.id.LocationSelectorDialog_mNpCity);
                mProvincePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
                mCityPicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);

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
            if (!TextUtils.isEmpty(mUserEntity.getLocation())) {
                String[] location = mUserEntity.getLocation().split(" ");
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
    public ProfileEditContract.Presenter getPresenter() {
        return new ProfileEditPresenter();
    }

    @Override
    public void showNewAvatar(String avatarPath) {
        mUserEntity.setAvatar(avatarPath);
        setAvatar(avatarPath);
    }

    @Override
    public void goBack() {
        finish();
    }
}
