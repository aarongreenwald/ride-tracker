package com.greenwald.aaron.ridetracker.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.Date;

public class TrackPoint {
    public double latitude;
    public double longitude;
    public double accuracy;
    public Date dateTime;
    public double altitude;

    public TrackPoint(double latitude, double longitude, double accuracy, Date dateTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.dateTime = dateTime;
    }

    public static TrackPoint fromString(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, TrackPoint.class);
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }
}
