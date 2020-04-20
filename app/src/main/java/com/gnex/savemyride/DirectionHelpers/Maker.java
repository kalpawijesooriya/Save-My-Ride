package com.gnex.savemyride.DirectionHelpers;

import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.sql.Driver;

public class Maker {
    private float v;
    private double  lng , lat ;
    private LatLng lastPos = new LatLng(6.841749, 79.964201);
    private int flag1 = 0 ;

    private Marker m;
    private Driver d;

    private static final String TAG = "DriverMarker class";

    public Maker() {
    }

    public Maker(Marker m, Driver d) {
        this.m = m;
        this.d = d;
    }

    public Marker getM() {return m;}
    public void setM(Marker m) {this.m = m;}
    public Driver getD() {return d;}
    public void setD(Driver d) {this.d = d;}

    public void animator(final LatLng startPosition, final LatLng endPosition, final Marker marker, final GoogleMap mMap){

        // Log.d(TAG, "animator_method_running");

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(3000);
        valueAnimator.setInterpolator(new LinearInterpolator());


        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                v = valueAnimator.getAnimatedFraction();

                if(endPosition != null && startPosition != null ){
                    lng = v * endPosition.longitude + (1 - v)* startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v)* startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);

                    if(getStraightLineDistance(newPos, lastPos)> 0.0005 ){//0.0005

                        if(flag1 ==1){
                            marker.setPosition(newPos);
                            marker.setAnchor(0.5f, 0.5f);
                            marker.setRotation(getBearing(startPosition, newPos));
                        }
                        lastPos = newPos;
                        //Log.d(TAG, "onAnimationUpdate newPos :"+ newPos);
                    }
//                    mMap.animateCamera(CameraUpdateFactory
//                            .newCameraPosition(new CameraPosition.Builder().target(newPos).zoom(15.5f).build()));
                }
            }
        });
        valueAnimator.start();

        flag1 = 1;
    }

    private static float  getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    public static double getStraightLineDistance(LatLng latlang1, LatLng latlang2) {

        double dLat  = Math.toRadians((latlang2.latitude - latlang1.latitude));
        double dLong = Math.toRadians((latlang2.longitude - latlang1.longitude));

        double startLat = Math.toRadians(latlang1.latitude);
        double endLat   = Math.toRadians(latlang2.latitude);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

}
