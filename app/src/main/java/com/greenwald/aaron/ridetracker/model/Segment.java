package com.greenwald.aaron.ridetracker.model;

import java.util.Date;

/**
 * Created by aarong on 19/07/2018.
 */

public class Segment {
    private Date startedTimestamp;
    private long id;

    public Segment(Date startedTimestamp) {
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
}
