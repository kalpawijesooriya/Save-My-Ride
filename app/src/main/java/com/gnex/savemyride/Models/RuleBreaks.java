package com.gnex.savemyride.Models;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class RuleBreaks {
    public String dateTile;
    public  Float speed;
    public  String maxSpeed;
    public LatLng place;
    public  String sign;

    public RuleBreaks(String dateTile, Float speed, String maxSpeed, LatLng place, String sign) {
        this.dateTile = dateTile;
        this.speed = speed;
        this.maxSpeed = maxSpeed;
        this.place = place;
        this.sign = sign;
    }

    public RuleBreaks(){}

}
