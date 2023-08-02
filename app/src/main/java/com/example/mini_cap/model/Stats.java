package com.example.mini_cap.model;

import java.time.LocalDateTime;

public class Stats {
    private String exposure;
    private String timestamp;


    // Constructor for Stats object
    public Stats(String exposure, String timestamp){
        this.exposure = exposure;
        this.timestamp = timestamp;
    }

    // Getters
    public String getExposure() {return exposure;}

    public String getTimestamp() {return timestamp;}

    // Setters
    public void setExposure(String exposure) {this.exposure = exposure;}

    public void setTimestamp(String timestamp) {this.timestamp = timestamp;}

}

