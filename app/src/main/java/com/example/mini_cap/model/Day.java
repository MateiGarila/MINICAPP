package com.example.mini_cap.model;

import android.annotation.SuppressLint;

import java.time.LocalDate;
import java.util.Date;

public class Day {
    private int day;
    private int month;
    private int year;

    public Day(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public Day(Date javaDate) {
        this.year = javaDate.getYear() + 1900;
        this.month = javaDate.getMonth() + 1;
        this.day = javaDate.getDate();
    }

    public Day(LocalDate javaLocalDate) {
        this.year = javaLocalDate.getYear();
        this.month = javaLocalDate.getMonthValue();
        this.day = javaLocalDate.getDayOfMonth();
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

    /* Function for converting dates into database addressable numbers*/
    public long toDatabaseNumber(){
        return (year * 10000L + month * 100L + day) * 100000;
    }
}
