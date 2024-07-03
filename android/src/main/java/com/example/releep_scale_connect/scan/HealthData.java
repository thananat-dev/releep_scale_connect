package com.example.releep_scale_connect.scan;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

import aicare.net.cn.iweightlibrary.entity.DecimalInfo;

public class HealthData {

    @SerializedName("adc")
    private int adc;

    @SerializedName("age")
    private int age;
    @SerializedName("bfr")
    private double bfr;

    @SerializedName("bm")
    private double bm;

    @SerializedName("bmi")
    private double bmi;
    @SerializedName("bmr")
    private double bmr;
    @SerializedName("bodyAge")
    private int bodyAge;
    @SerializedName("date")
    private String date;
    @SerializedName("decimalInfo")
    private DecimalInfo decimalInfo;

    @SerializedName("height")
    private int height;
    @SerializedName("number")
    private int number;
    @SerializedName("pp")
    private double pp;
    @SerializedName("rom")
    private double rom;
    @SerializedName("sex")
    private int sex;
    @SerializedName("sfr")
    private double sfr;
    @SerializedName("time")
    private String time;
    @SerializedName("uvi")
    private int uvi;
    @SerializedName("vwc")
    private double vwc;
    @SerializedName("weight")
    private double weight;

    // Getters and Setters
    public int getAdc() {
        return adc;
    }

    public void setAdc(int adc) {
        this.adc = adc;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getBfr() {
        return bfr;
    }

    public void setBfr(double bfr) {
        this.bfr = bfr;
    }

    public double getBm() {
        return bm;
    }

    public void setBm(double bm) {
        this.bm = bm;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getBmr() {
        return bmr;
    }

    public void setBmr(double bmr) {
        this.bmr = bmr;
    }

    public int getBodyAge() {
        return bodyAge;
    }

    public void setBodyAge(int bodyAge) {
        this.bodyAge = bodyAge;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public DecimalInfo getDecimalInfo() {
        return decimalInfo;
    }

    public void setDecimalInfo(DecimalInfo decimalInfo) {
        this.decimalInfo = decimalInfo;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getPp() {
        return pp;
    }

    public void setPp(double pp) {
        this.pp = pp;
    }

    public double getRom() {
        return rom;
    }

    public void setRom(double rom) {
        this.rom = rom;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public double getSfr() {
        return sfr;
    }

    public void setSfr(double sfr) {
        this.sfr = sfr;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getUvi() {
        return uvi;
    }

    public void setUvi(int uvi) {
        this.uvi = uvi;
    }

    public double getVwc() {
        return vwc;
    }

    public void setVwc(double vwc) {
        this.vwc = vwc;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    // Inner class for DecimalInfo


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthData that = (HealthData) o;
        return adc == that.adc && age == that.age && Double.compare(that.bfr, bfr) == 0 && Double.compare(that.bm, bm) == 0 && Double.compare(that.bmi, bmi) == 0 && Double.compare(that.bmr, bmr) == 0 && bodyAge == that.bodyAge && height == that.height && number == that.number && Double.compare(that.pp, pp) == 0 && Double.compare(that.rom, rom) == 0 && sex == that.sex && Double.compare(that.sfr, sfr) == 0 && uvi == that.uvi && Double.compare(that.vwc, vwc) == 0 && Double.compare(that.weight, weight) == 0 && Objects.equals(date, that.date) && Objects.equals(decimalInfo, that.decimalInfo) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adc, age, bfr, bm, bmi, bmr, bodyAge, date, decimalInfo, height, number, pp, rom, sex, sfr, time, uvi, vwc, weight);
    }

    @Override
    public String toString() {
        return "HealthData{" +
                "adc=" + adc +
                ", age=" + age +
                ", bfr=" + bfr +
                ", bm=" + bm +
                ", bmi=" + bmi +
                ", bmr=" + bmr +
                ", bodyAge=" + bodyAge +
                ", date='" + date + '\'' +
                ", decimalInfo=" + decimalInfo +
                ", height=" + height +
                ", number=" + number +
                ", pp=" + pp +
                ", rom=" + rom +
                ", sex=" + sex +
                ", sfr=" + sfr +
                ", time='" + time + '\'' +
                ", uvi=" + uvi +
                ", vwc=" + vwc +
                ", weight=" + weight +
                '}';
    }
}
