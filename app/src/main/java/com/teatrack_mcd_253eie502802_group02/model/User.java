package com.teatrack_mcd_253eie502802_group02.model;

public class User {
    private String id;
    private String name;
    private String fullName;
    private String username;
    private String email;
    private String role;
    private String status;
    private String phoneNumber;
    private String address;
    private String createdAt;

    // Legacy fields
    private String phone;
    private String password; // Hashed password
    private String dob;
    private String gender;
    private String avatarUrl;
    private String avatarBase64;

    public User() {
    }

    public User(String id, String fullName, String username, String email, String role,
                String status, String phoneNumber, String address, String createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.createdAt = createdAt;
        this.name = username;
        this.phone = phoneNumber;
    }

    public User(String id, String name, String email, String phone, String password, String createdAt) {
        this.id = id;
        this.fullName = name;
        this.username = name;
        this.name = name;
        this.email = email;
        this.role = "Customer";
        this.status = "Active";
        this.phoneNumber = phone;
        this.address = "";
        this.createdAt = createdAt;
        this.phone = phone;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return isBlank(fullName) ? name : fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return isBlank(username) ? name : username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.name = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return isBlank(status) ? "Active" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoneNumber() {
        return isBlank(phoneNumber) ? phone : phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.phone = phoneNumber;
    }

    public String getAddress() {
        return address == null ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return getUsername();
    }

    public void setName(String name) {
        this.name = name;
        if (isBlank(username)) {
            this.username = name;
        }
        if (isBlank(fullName)) {
            this.fullName = name;
        }
    }

    public String getPhone() {
        return getPhoneNumber();
    }

    public void setPhone(String phone) {
        this.phone = phone;
        if (isBlank(phoneNumber)) {
            this.phoneNumber = phone;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarBase64() {
        return avatarBase64;
    }

    public void setAvatarBase64(String avatarBase64) {
        this.avatarBase64 = avatarBase64;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
