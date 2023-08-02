package com.example.mini_cap.model;

import java.time.LocalDateTime;

public class Stats {
    private int LogID;
    private String exposure;
    private String timestamp;


    // Constructor for Stats object
    public Stats(int logID, String exposure, String timestamp){
        LogID = logID;
        this.exposure = exposure;
        this.timestamp = timestamp;
    }

    // Getters

    public int getLogID() {return LogID;}

    public String getExposure() {return exposure;}

    public String getTimestamp() {return timestamp;}

    // Setters

    public void setLogID(int logID) {LogID = logID;}

    public void setExposure(String exposure) {this.exposure = exposure;}

    public void setTimestamp(String timestamp) {this.timestamp = timestamp;}

}

