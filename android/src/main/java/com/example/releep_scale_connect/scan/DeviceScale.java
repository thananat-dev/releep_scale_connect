package com.example.releep_scale_connect.scan;

import com.google.gson.annotations.SerializedName;

public class DeviceScale {
    @SerializedName("address")
    private String address;
    @SerializedName("deviceType")
    private int deviceType;
    @SerializedName("isBright")
    private boolean isBright;
    @SerializedName("name")
    private String name;
    @SerializedName("rssi")
    private int rssi;

    // Constructor
    public DeviceScale(String address, int deviceType, boolean isBright, String name, int rssi) {
        this.address = address;
        this.deviceType = deviceType;
        this.isBright = isBright;
        this.name = name;
        this.rssi = rssi;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isBright() {
        return isBright;
    }

    public void setBright(boolean isBright) {
        this.isBright = isBright;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "Device{" +
                "address='" + address + '\'' +
                ", deviceType=" + deviceType +
                ", isBright=" + isBright +
                ", name='" + name + '\'' +
                ", rssi=" + rssi +
                '}';
    }
}
