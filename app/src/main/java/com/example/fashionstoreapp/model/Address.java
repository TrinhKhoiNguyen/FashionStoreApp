package com.example.fashionstoreapp.model;

public class Address {
    private String addressId;
    private String name;
    private String phone;
    private String address;
    private String city;
    private boolean isDefault;

    // Thông tin địa chỉ 3 cấp
    private int provinceId;
    private String provinceName;
    private int districtId;
    private String districtName;
    private String wardCode;
    private String wardName;

    public Address() {
    }

    public Address(String addressId, String name, String phone, String address, String city, boolean isDefault) {
        this.addressId = addressId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.isDefault = isDefault;
    }

    public String getAddressId() {
        return addressId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getWardCode() {
        return wardCode;
    }

    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    /**
     * Lấy địa chỉ đầy đủ (bao gồm cả Phường/Quận/Tỉnh)
     */
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();

        if (address != null && !address.isEmpty()) {
            fullAddress.append(address);
        }

        if (wardName != null && !wardName.isEmpty()) {
            if (fullAddress.length() > 0)
                fullAddress.append(", ");
            fullAddress.append(wardName);
        }

        if (districtName != null && !districtName.isEmpty()) {
            if (fullAddress.length() > 0)
                fullAddress.append(", ");
            fullAddress.append(districtName);
        }

        if (provinceName != null && !provinceName.isEmpty()) {
            if (fullAddress.length() > 0)
                fullAddress.append(", ");
            fullAddress.append(provinceName);
        } else if (city != null && !city.isEmpty()) {
            // Fallback về city cũ nếu chưa có provinceName
            if (fullAddress.length() > 0)
                fullAddress.append(", ");
            fullAddress.append(city);
        }

        return fullAddress.toString();
    }
}
