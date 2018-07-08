package com.greenwald.aaron.ridetracker;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.time.ZonedDateTime;
import java.util.Date;

public class TrackPoint {
    double latitude;
    double longitude;
    double accuracy;
    Date dateTime;

    TrackPoint(double latitude, double longitude, double accuracy, Date dateTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.dateTime = dateTime;
    }

    static TrackPoint fromString(String string) {
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
