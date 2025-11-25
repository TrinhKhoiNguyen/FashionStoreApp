package com.example.fashionstoreapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String id;
    private String email;
    private String phone;
    private String name;
    private String password;
    private String profileImageUrl;
    private String address;
    private String city;
    private String district;
    private String ward;
    private String birthday;
    private String gender;
    private String role; // "admin" or "user"
    private List<String> favoriteProductIds;
    private long createdAt;
    private long lastLoginAt;

    // Constructor
    public User() {
        this.favoriteProductIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.role = "user"; // Default role
    }

    public User(String email, String password) {
        this();
        this.id = "user_" + System.currentTimeMillis();
        this.email = email;
        this.password = password;
    }

    public User(String email, String phone, String name, String password) {
        this(email, password);
        this.phone = phone;
        this.name = name;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Alias methods for photoUrl (for Firebase Auth compatibility)
    public String getPhotoUrl() {
        return profileImageUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.profileImageUrl = photoUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public List<String> getFavoriteProductIds() {
        return favoriteProductIds;
    }

    public void setFavoriteProductIds(List<String> favoriteProductIds) {
        this.favoriteProductIds = favoriteProductIds;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    // Get full address
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (address != null && !address.isEmpty()) {
            fullAddress.append(address);
        }
        if (ward != null && !ward.isEmpty()) {
            if (fullAddress.length() > 0)
                fullAddress.append(", ");
            fullAddress.append(ward);
        }
        if (district != null && !district.isEmpty()) {
            if (fullAddress.length() > 0)
                fullAddress.append(", ");
            fullAddress.append(district);
        }
        if (city != null && !city.isEmpty()) {
            if (fullAddress.length() > 0)
                fullAddress.append(", ");
            fullAddress.append(city);
        }
        return fullAddress.toString();
    }

    // Add product to favorites
    public void addFavorite(String productId) {
        if (!favoriteProductIds.contains(productId)) {
            favoriteProductIds.add(productId);
        }
    }

    // Remove product from favorites
    public void removeFavorite(String productId) {
        favoriteProductIds.remove(productId);
    }

    // Check if product is favorite
    public boolean isFavorite(String productId) {
        return favoriteProductIds.contains(productId);
    }

    // Get display name (name or email)
    public String getDisplayName() {
        if (name != null && !name.isEmpty()) {
            return name;
        } else if (email != null && !email.isEmpty()) {
            return email.split("@")[0];
        } else if (phone != null && !phone.isEmpty()) {
            return phone;
        }
        return "User";
    }
}
