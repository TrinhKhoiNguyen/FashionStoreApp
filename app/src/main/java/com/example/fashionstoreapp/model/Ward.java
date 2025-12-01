package com.example.fashionstoreapp.model;

import com.google.gson.annotations.SerializedName;

public class Ward {
    @SerializedName("code")
    private int code;

    @SerializedName("name")
    private String wardName;

    @SerializedName("division_type")
    private String divisionType;

    @SerializedName("district_code")
    private int districtCode;

    public Ward() {
    }

    public Ward(int wardCode, String wardName, int districtId) {
        this.code = wardCode;
        this.wardName = wardName;
        this.districtCode = districtId;
    }

    public String getWardCode() {
        return String.valueOf(code);
    }

    public void setWardCode(String wardCode) {
        try {
            this.code = Integer.parseInt(wardCode);
        } catch (Exception e) {
            this.code = 0;
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public int getDistrictId() {
        return districtCode;
    }

    public void setDistrictId(int districtId) {
        this.districtCode = districtId;
    }

    public String getWardType() {
        return divisionType;
    }

    public void setWardType(String wardType) {
        this.divisionType = wardType;
    }

    @Override
    public String toString() {
        return wardName;
    }
}
