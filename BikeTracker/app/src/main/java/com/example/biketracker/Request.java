package com.example.biketracker;

import com.google.gson.Gson;

import java.time.LocalDateTime;

class Request {
    private String device = "PHONE";
    private Time time = null;
    private String request;

    Request(RequestType requestType) {
        switch (requestType) {
            case GET:
                request = "GET";
                break;
            case HISTORY:
                request = "HISTORY";
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

    String toJson() {
        return new Gson().toJson(this);
    }
}

enum RequestType {
    GET,
    HISTORY,
    START,
    STOP
};


class Time {
    int year;
    int month;
    int day;
    int hour;
    int minute;
    int second;

    Time() {
        LocalDateTime now = LocalDateTime.now();
        year = now.getYear();
        month = now.getMonthValue();
        day = now.getDayOfMonth();
        hour = now.getHour();
        minute = now.getMinute();
        second = now.getSecond();
    }
}