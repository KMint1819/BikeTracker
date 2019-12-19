package com.example.biketracker;

import com.google.gson.Gson;

import java.time.LocalDateTime;

public class Request {
    private String device = "PHONE";
    private Time time = null;
    private String request;

    public Request(RequestType requestType) {
        switch (requestType) {
            case GET:
                request = "GET";
                break;
            case START:
                request = "START";
                break;
            case STOP:
                request = "STOP";
                break;
        }
        time = new Time();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}

enum RequestType {
    GET,
    START,
    STOP
};


class Time {
    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;

    public Time() {
        LocalDateTime now = LocalDateTime.now();
        year = now.getYear();
        month = now.getMonthValue();
        day = now.getDayOfMonth();
        hour = now.getHour();
        minute = now.getMinute();
        second = now.getSecond();
    }
}