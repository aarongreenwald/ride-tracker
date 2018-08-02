package com.greenwald.aaron.ridetracker.model;

import java.util.ArrayList;
import java.util.Date;

public class Segment {
    private Date startedTimestamp;
    private long id;
    private ArrayList<SegmentPoint> segmentPoints = new ArrayList<SegmentPoint>();

    public Segment(Date startedTimestamp) {
        this.startedTimestamp = startedTimestamp;
    }

    public Segment(long id, Date startedTimestamp) {
        this.id = id;
        this.startedTimestamp = startedTimestamp;
    }

    public Date getStartedTimestamp() {
        return startedTimestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArrayList<SegmentPoint> getSegmentPoints() {
        return segmentPoints;
    }

    public void setSegmentPoints(ArrayList<SegmentPoint> segmentPoints) {
        this.segmentPoints = segmentPoints;
    }
}
