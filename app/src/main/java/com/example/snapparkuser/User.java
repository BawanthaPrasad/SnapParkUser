package com.example.snapparkuser;

public class User {
    private String name;
    private String vehicleNo;
    private String email;

    private String deviceToken;

    public User() {

    }

    public User(String name, String vehicleNo, String email,String deviceToken) {
        this.name = name;
        this.vehicleNo = vehicleNo;
        this.email = email;
        this.deviceToken = deviceToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceToken(){
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}

