package com.example_jds_coen390.sprint1_1;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Session {
    private String id;
    private long timestamp;
    private List<Double> heartrate;
    private List<Double> bloodoxygen;

    public Session() {
        this.heartrate = new ArrayList<>();
        this.bloodoxygen = new ArrayList<>();
    }

    public Session(String id, long timestamp, List<Double> heartrate, List<Double> bloodoxygen) {
        this.id = id;
        this.timestamp = timestamp;
        this.heartrate = heartrate == null ? new ArrayList<>() : heartrate;
        this.bloodoxygen = bloodoxygen == null ? new ArrayList<>() : bloodoxygen;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LocalDateTime getDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Double> getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(List<Double> heartrate) {
        this.heartrate = heartrate == null ? new ArrayList<>() : heartrate;
    }

    public List<Double> getBloodOxygen() {
        return bloodoxygen;
    }

    public void setBloodOxygen(List<Double> bloodoxygen) {
        this.bloodoxygen = bloodoxygen == null ? new ArrayList<>() : bloodoxygen;
    }
}