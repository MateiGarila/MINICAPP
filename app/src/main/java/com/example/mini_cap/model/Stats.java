package com.example.mini_cap.model;

import java.time.LocalDateTime;

public class Stats {
    private String exposure;
    private long timestamp;


    // Constructor for Stats object
    public Stats(String exposure, long timestamp){
        this.exposure = exposure;
        this.timestamp = timestamp;
    }

    // Getters
    public String getExposure() {return exposure;}

    public long getTimestamp() {return timestamp;}

    // Setters
    public void setExposure(String exposure) {this.exposure = exposure;}

    public void setTimestamp(long timestamp) {this.timestamp = timestamp;}

}

