package com.gnex.savemyride.Models;

import com.google.android.gms.maps.model.LatLng;

public class MarkerModel {
    private  LatLng location;
    private  String type;
    private  boolean ststus;

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    private  String speed;
    private  boolean ststusPassed;
    public boolean isStstusPassed() {
        return ststusPassed;
    }

    public void setStstusPassed(boolean ststusPassed) {
        this.ststusPassed = ststusPassed;
    }


    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private  String road;
    private  String name;

    public boolean isStstus() {
        return ststus;
    }

    public void setStstus(boolean ststus) {
        this.ststus = ststus;
    }




    public MarkerModel(){}

    public MarkerModel(LatLng location, String type,boolean ststus,String road,String name,boolean ststusPassed) {
        this.location = location;
        this.type = type;
        this.ststus=ststus;
        this.name=name;
        this.road=road;
        this.ststusPassed=ststusPassed;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



}
