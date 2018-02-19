package com.yuneec.flight_settings;

public class WifiPassword {
    private String password;
    private String ssid;

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "WifiPassword [ssid=" + this.ssid + ", password=" + this.password + "]";
    }

    public WifiPassword(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
    }
}
