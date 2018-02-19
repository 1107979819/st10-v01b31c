package com.yuneec.uartcontroller;

import android.location.Location;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GPSUpLinkData {
    public float accuracy;
    public float alt;
    public float angle;
    public float lat;
    public float lon;
    public int no_satelites;
    public boolean reset = true;
    public float speed;

    public void reset() {
        this.lon = 0.0f;
        this.lat = 0.0f;
        this.alt = 0.0f;
        this.accuracy = 0.0f;
        this.speed = 0.0f;
        this.no_satelites = 0;
        this.reset = true;
    }

    public void setData(Location location) {
        this.lon = (float) location.getLongitude();
        this.lat = (float) location.getLatitude();
        this.alt = (float) location.getAltitude();
        this.accuracy = Math.abs(location.getAccuracy());
        this.speed = Math.abs(location.getSpeed());
        this.reset = false;
    }

    public static String getParamsName() {
        return ",lon,lat,alt,accuracy,speed,angle\n";
    }

    public String toString() {
        return new StringBuilder(String.valueOf(new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS").format(new Date()))).append(",").append(this.lon).append(",").append(this.lat).append(",").append(this.alt).append(",").append(this.accuracy * 10.0f).append(",").append(this.speed * 10.0f).append(",").append(this.angle).append("\n").toString();
    }
}
