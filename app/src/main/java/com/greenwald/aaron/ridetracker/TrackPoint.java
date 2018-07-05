package com.greenwald.aaron.ridetracker;

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

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
