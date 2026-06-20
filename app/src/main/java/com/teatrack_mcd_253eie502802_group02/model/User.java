package com.teatrack_mcd_253eie502802_group02.model;

public class User {
    private String id;
    private String name; // Login Name
    private String fullName;
    private String email;
    private String phone;
    private String password; // Hashed password
    private String dob;
    private String gender;
    private String address;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String id, String name, String fullName, String email, String phone, String password) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public User(String id, String name, String fullName, String email, String phone, String password, String dob, String gender, String address) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    private String avatarUrl;

    private String avatarBase64;

    public String getAvatarBase64() { return avatarBase64; }
    public void setAvatarBase64(String avatarBase64) { this.avatarBase64 = avatarBase64; }
}

