package com.yzx.chat.core.entity;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年02月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ProvinceEntity {
    private String province;
    private ArrayList<CityEntity> city;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public ArrayList<CityEntity> getCity() {
        return city;
    }

    public void setCity(ArrayList<CityEntity> city) {
        this.city = city;
    }
}
