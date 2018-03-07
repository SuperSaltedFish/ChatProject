package com.yzx.chat.bean;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年02月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ProvinceBean {
    private String province;
    private ArrayList<CityBean> city;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public ArrayList<CityBean> getCity() {
        return city;
    }

    public void setCity(ArrayList<CityBean> city) {
        this.city = city;
    }
}
