package com.example.mini_cap.model;

import java.time.LocalDateTime;

public class Stats {
    private int LogID;
    private float exposure;
    private String timestamp;


    // Constructor for Stats object
    public Stats(int logID, float exposure, String timestamp){
        LogID = logID;
        this.exposure = exposure;
        this.timestamp = timestamp;
    }

    // Getters

    public int getLogID() {return LogID;}

    public float getExposure() {return exposure;}

    public String getTimestamp() {return timestamp;}

    // Setters

    public void setLogID(int logID) {LogID = logID;}

    public void setExposure(float exposure) {this.exposure = exposure;}

    public void setTimestamp(String timestamp) {this.timestamp = timestamp;}

}

