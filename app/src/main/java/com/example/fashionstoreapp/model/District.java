package com.example.fashionstoreapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class District {
    @SerializedName("code")
    private int code;

    @SerializedName("name")
    private String districtName;

    @SerializedName("division_type")
    private String divisionType;

    @SerializedName("province_code")
    private int provinceCode;

    @SerializedName("wards")
    private List<Ward> wards;

    public District() {
    }

    public District(int districtId, String districtName, int provinceId, String divisionType) {
        this.code = districtId;
        this.districtName = districtName;
        this.provinceCode = provinceId;
        this.divisionType = divisionType;
    }

    public int getDistrictId() {
        return code;
    }

    public void setDistrictId(int districtId) {
        this.code = districtId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public int getProvinceId() {
        return provinceCode;
    }

    public void setProvinceId(int provinceId) {
        this.provinceCode = provinceId;
    }

    public String getDistrictType() {
        return divisionType;
    }

    public void setDistrictType(String districtType) {
        this.divisionType = districtType;
    }

    // Backward compatibility
    public String getDivisionType() {
        return divisionType;
    }

    public List<Ward> getWards() {
        return wards;
    }

    public void setWards(List<Ward> wards) {
        this.wards = wards;
    }

    @Override
    public String toString() {
        return districtName;
    }
}
