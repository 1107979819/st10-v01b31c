package com.yuneec.uartcontroller;

public class WaypointData {
    public float altitude;
    public float gimbalPitch;
    public float gimbalYam;
    public float latitude;
    public float longitude;
    public float pitch;
    public int pointerIndex;
    public float roll;
    public float yaw;

    public String toString() {
        return "pointerIndex: " + this.pointerIndex + ", latitude: " + this.latitude + ", longitude: " + this.longitude + ", altitude: " + this.altitude + ", roll: " + this.roll + ", pitch: " + this.pitch + ", yaw: " + this.yaw + ", gimbalPitch: " + this.gimbalPitch + ", gimbalYam: " + this.gimbalYam;
    }
}
