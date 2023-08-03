package com.example.mini_cap.model;

import android.annotation.SuppressLint;

public class Day {
    private int day;
    private int month;
    private int year;

    public Day(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        @SuppressLint("DefaultLocale") String formatDay = String.format("%02d", day);
        @SuppressLint("DefaultLocale") String formatMonth = String.format("%02d", month);

        return year+"/"+formatMonth+"/"+formatDay;
    }
}
