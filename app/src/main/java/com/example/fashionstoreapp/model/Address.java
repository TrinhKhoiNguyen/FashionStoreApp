package com.example.fashionstoreapp.model;

public class Address {
    private String addressId;
    private String name;
    private String phone;
    private String address;
    private String city;
    private boolean isDefault;

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
}
