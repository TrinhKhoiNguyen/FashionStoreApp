package com.example.fashionstoreapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Province {
    @SerializedName("code")
    private int code;

    @SerializedName("name")
    private String provinceName;

    @SerializedName("division_type")
    private String divisionType;

    @SerializedName("phone_code")
    private int phoneCode;

    @SerializedName("districts")
    private List<District> districts;

    public Province() {
    }

    public Province(int provinceId, String provinceName, String divisionType) {
        this.code = provinceId;
        this.provinceName = provinceName;
        this.divisionType = divisionType;
    }

    public int getProvinceId() {
        return code;
    }

    public void setProvinceId(int provinceId) {
        this.code = provinceId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceType() {
        return divisionType;
    }

    public void setProvinceType(String provinceType) {
        this.divisionType = provinceType;
    }

    public int getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(int phoneCode) {
        this.phoneCode = phoneCode;
    }

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }

    @Override
    public String toString() {
        return provinceName;
    }
}
