package com.gnex.savemyride.Models;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class RuleBreaks {
    public  String dateTile;

    public String getDateTile() {
        return dateTile;
    }

    public void setDateTile(String dateTile) {
        this.dateTile = dateTile;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public String getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(String maxSpeed) {
        this.maxSpeed = maxSpeed;
    }




    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    private  Float speed;
    private  String maxSpeed;
    private  String lon;
    private  String place;
    private  String sign;
    private  String lat;

    public RuleBreaks(String dateTile, Float speed, String maxSpeed, String lat,String lon,String place,  String sign) {
        this.dateTile = dateTile;
        this.speed = speed;
        this.maxSpeed = maxSpeed;
        this.lat = lat;
        this.lon = lon;
        this.sign = sign;
        this.place=place;
    }

    public RuleBreaks(){}

    public RuleBreaks(String place){

        this.place=place;
    }

}
