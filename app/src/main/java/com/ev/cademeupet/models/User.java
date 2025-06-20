package com.ev.cademeupet.models;

import androidx.annotation.NonNull;

public class User 
{
    private String street;
    private String homeNumber;
    private String district;
    private String city;
    private String UF;
    private String fullName;
    private String phone;
    private String email;
    
    public User(){};
    
    public User(String street, String homeNumber, String district, String city, String UF, String fullName, String phone, String email) {
        this.street = street;
        this.homeNumber = homeNumber;
        this.district = district;
        this.city = city;
        this.UF = UF;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getHomeNumber() {
        return homeNumber;
    }
    
    public void setHomeNumber(String homeNumber) {
        this.homeNumber = homeNumber;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getUF() {
        return UF;
    }
    
    public void setUF(String UF) {
        this.UF = UF;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getFullAddress()
    {
        return street + "," + homeNumber + "," + district + "," + city + "," + UF;
    }
}
