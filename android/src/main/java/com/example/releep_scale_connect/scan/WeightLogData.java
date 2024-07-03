package com.example.releep_scale_connect.scan;

import com.google.gson.annotations.SerializedName;

import aicare.net.cn.iweightlibrary.entity.DecimalInfo;

public class WeightLogData {

    @SerializedName("adc")
    private int adc;
    @SerializedName("algorithmType")
    private int algorithmType;
    @SerializedName("cmdType")
    private int cmdType;
    @SerializedName("decimalInfo")
    private DecimalInfo decimalInfo;
    @SerializedName("deviceType")
    private int deviceType;
    @SerializedName("mMac")
    private String mMac;
    @SerializedName("temp")
    private double temp;
    @SerializedName("unitType")
    private int unitType;
    @SerializedName("weight")
    private double weight;

    public WeightLogData(int adc, int algorithmType, int cmdType, DecimalInfo decimalInfo, int deviceType, String mMac, double temp, int unitType, double weight) {
        this.adc = adc;
        this.algorithmType = algorithmType;
        this.cmdType = cmdType;
        this.decimalInfo = decimalInfo;
        this.deviceType = deviceType;
        this.mMac = mMac;
        this.temp = temp;
        this.unitType = unitType;
        this.weight = weight;
    }

    public int getAdc() {
        return adc;
    }

    public void setAdc(int adc) {
        this.adc = adc;
    }

    public int getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(int algorithmType) {
        this.algorithmType = algorithmType;
    }

    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }


    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getMMac() {
        return mMac;
    }

    public void setMMac(String mMac) {
        this.mMac = mMac;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public int getUnitType() {
        return unitType;
    }

    public void setUnitType(int unitType) {
        this.unitType = unitType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    }