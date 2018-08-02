package com.greenwald.aaron.ridetracker.model;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class Trip implements Serializable {
    private String name;
    private long id;
    private ArrayList<Segment> segmentList = new ArrayList<>();

    public Trip(String name) {
        this.name = name;
    }

    public Trip(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArrayList<LatLng> getAllLocations() {
        ArrayList<LatLng> result = new ArrayList<>();
        //I wish I was using scala right now....need to switch to Kotlin
        for (Segment segment : segmentList)
            for (SegmentPoint segmentPoint : segment.getSegmentPoints())
                result.add(segmentPoint.getLatLng());

        return result;
    }

    public void setSegments(ArrayList<Segment> segments) {
        this.segmentList = segments;
    }

    public SegmentPoint getStartingPoint() {
        if (!segmentList.isEmpty() && !segmentList.get(0).getSegmentPoints().isEmpty()) {
            return segmentList.get(0).getSegmentPoints().get(0);
        }
        return null;
    }
}
