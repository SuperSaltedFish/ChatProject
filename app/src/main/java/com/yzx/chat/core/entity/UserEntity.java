package com.yzx.chat.core.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.util.AndroidHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by YZX on 2017年11月17日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class UserEntity implements Parcelable {
    public static final int SEX_NOT_SET = 0;
    public static final int SEX_MAN = 1;
    public static final int SEX_WOMAN = 2;

    private String userID;
    private String telephone;
    private String nickname;
    private String avatar;
    private String signature;
    private String location;
    private String birthday;
    private String email;
    private String profession;
    private String school;
    private int sex;
    private String age;


    public static UserEntity copy(UserEntity user) {
        Parcel parcel = Parcel.obtain();
        user.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        UserEntity copy = CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return copy;
    }

    public boolean isEmpty() {
        return (TextUtils.isEmpty(userID) || TextUtils.isEmpty(telephone) || TextUtils.isEmpty(nickname));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserEntity)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        UserEntity user = (UserEntity) obj;
        return userID != null && userID.equals(user.getUserID());
    }

    @Override
    public int hashCode() {
        if (!TextUtils.isEmpty(userID)) {
            return userID.hashCode() + 1;//+1是为了将和字符串的userID的hashCode区分
        }
        return super.hashCode();
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getAge() {
        return calculateAgeFromBirthday(birthday);
    }

    public void setAge(String age) {
        this.age = age;
    }

    public static String calculateAgeFromBirthday(String strBirthday) {
        if (!TextUtils.isEmpty(strBirthday)) {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            try {
                Date birthDay = isoFormat.parse(strBirthday);
                Calendar cal = Calendar.getInstance();
                if (cal.before(birthDay)) {
                    return "0" + AndroidHelper.getString(R.string.Unit_Age);
                }
                int yearNow = cal.get(Calendar.YEAR);
                int monthNow = cal.get(Calendar.MONTH);
                int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
                cal.setTime(birthDay);

                int yearBirth = cal.get(Calendar.YEAR);
                int monthBirth = cal.get(Calendar.MONTH);
                int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

                int age = yearNow - yearBirth;

                if (monthNow <= monthBirth) {
                    if (monthNow == monthBirth) {
                        if (dayOfMonthNow < dayOfMonthBirth) age--;
                    } else {
                        age--;
                    }
                }
                return String.valueOf(age) + AndroidHelper.getString(R.string.Unit_Age);
            } catch (ParseException e) {
                e.printStackTrace();

            }
        }
        return AndroidHelper.getString(R.string.ContactProfileActivity_AgeInvisible);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userID);
        dest.writeString(this.telephone);
        dest.writeString(this.nickname);
        dest.writeString(this.avatar);
        dest.writeString(this.signature);
        dest.writeString(this.location);
        dest.writeString(this.birthday);
        dest.writeString(this.email);
        dest.writeString(this.profession);
        dest.writeString(this.school);
        dest.writeInt(this.sex);
        dest.writeString(this.age);
    }

    public UserEntity() {
    }

    protected UserEntity(Parcel in) {
        this.userID = in.readString();
        this.telephone = in.readString();
        this.nickname = in.readString();
        this.avatar = in.readString();
        this.signature = in.readString();
        this.location = in.readString();
        this.birthday = in.readString();
        this.email = in.readString();
        this.profession = in.readString();
        this.school = in.readString();
        this.sex = in.readInt();
        this.age = in.readString();
    }

    public static final Creator<UserEntity> CREATOR = new Creator<UserEntity>() {
        @Override
        public UserEntity createFromParcel(Parcel source) {
            return new UserEntity(source);
        }

        @Override
        public UserEntity[] newArray(int size) {
            return new UserEntity[size];
        }
    };
}
