package com.greenwald.aaron.ridetracker.model;

import java.util.Date;

/**
 * Created by aarong on 19/07/2018.
 */

public class Trip {
    private String name;
    private long id;

    public Trip(String name) {
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
}
