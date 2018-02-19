package com.yuneec.uartcontroller;

public class RoiData {
    public float centerAltitude;
    public float centerLatitude;
    public float centerLongitude;
    public int radius;
    public int speed;

    public void reset() {
        this.centerLongitude = 0.0f;
        this.centerLatitude = 0.0f;
        this.centerAltitude = 0.0f;
        this.radius = 0;
        this.speed = 0;
    }

    public String toString() {
        return "centerLongitude: " + this.centerLongitude + "centerLatitude： " + this.centerLatitude + "centerAltitude: " + this.centerAltitude + "radius: " + this.radius + "speed: " + this.speed;
    }
}
