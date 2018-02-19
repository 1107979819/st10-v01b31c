package com.yuneec.flightmode15;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.yuneec.uartcontroller.GPSUpLinkData;
import java.util.Timer;
import java.util.TimerTask;

public class GPSUpdater implements LocationListener, Listener, NmeaListener {
    public static final int EVENT_GPS_AVAILABLE = 104;
    public static final int EVENT_GPS_DISABLED = 103;
    public static final int EVENT_GPS_ENABLED = 102;
    public static final int EVENT_GPS_OUT_OF_SERVICE = 105;
    public static final int EVENT_GPS_TEMPORARILY_UNAVAILABLE = 106;
    public static final int EVENT_LOCATION_UPDATED = 100;
    public static final int EVENT_STAELLITES_NUM_UPDATED = 101;
    private static final int UPDATE_INTERVAL = 200;
    private GPSUpLinkData mGPSData = new GPSUpLinkData();
    private Handler mHandler;
    private LocationManager mLM;
    private Timer mUpdateTimer;
    private UpdateTimerTask mUpdateTimerTask;

    private class UpdateTimerTask extends TimerTask {
        private UpdateTimerTask() {
        }

        public void run() {
            if (GPSUpdater.this.mHandler != null && GPSUpdater.this.checkValidGpsData()) {
                Message.obtain(GPSUpdater.this.mHandler, 100, GPSUpdater.this.mGPSData).sendToTarget();
            }
        }
    }

    public GPSUpdater(Context context) {
        this.mLM = (LocationManager) context.getSystemService("location");
    }

    private void registerHandler(Handler handler) {
        this.mHandler = handler;
    }

    private void unregisterHandler() {
        this.mHandler = null;
    }

    public void start(Handler handler) {
        this.mGPSData.reset();
        registerHandler(handler);
        this.mLM.requestLocationUpdates("gps", 200, 0.0f, this);
        this.mLM.addGpsStatusListener(this);
        this.mLM.addNmeaListener(this);
        this.mUpdateTimer = new Timer("GPSUpdater");
        this.mUpdateTimerTask = new UpdateTimerTask();
        this.mUpdateTimer.scheduleAtFixedRate(this.mUpdateTimerTask, 200, 200);
    }

    public void stop() {
        unregisterHandler();
        this.mLM.removeGpsStatusListener(this);
        this.mLM.removeUpdates(this);
        if (this.mUpdateTimer != null) {
            this.mUpdateTimer.cancel();
            if (this.mUpdateTimerTask != null) {
                this.mUpdateTimerTask.cancel();
            }
            this.mUpdateTimer = null;
        }
    }

    public void onGpsStatusChanged(int event) {
        switch (event) {
            case 1:
                if (this.mGPSData != null) {
                    this.mGPSData.reset();
                    return;
                }
                return;
            case 2:
                if (this.mGPSData != null) {
                    this.mGPSData.reset();
                    return;
                }
                return;
            case 3:
                if (this.mGPSData != null) {
                    this.mGPSData.reset = false;
                    return;
                }
                return;
            case 4:
                GpsStatus gs = this.mLM.getGpsStatus(null);
                int gps_num = -1;
                if (gs != null) {
                    gps_num = 0;
                    for (GpsSatellite satellite : gs.getSatellites()) {
                        if (satellite.usedInFix()) {
                            gps_num++;
                        }
                    }
                }
                if (this.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = 101;
                    msg.arg1 = gps_num;
                    this.mHandler.sendMessage(msg);
                }
                if (gps_num >= 0 && this.mGPSData != null) {
                    this.mGPSData.no_satelites = gps_num;
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onLocationChanged(Location location) {
        if (location == null) {
            this.mGPSData.reset();
        } else {
            this.mGPSData.setData(location);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (this.mHandler != null) {
            switch (status) {
                case 0:
                    this.mHandler.sendEmptyMessage(105);
                    return;
                case 1:
                    this.mHandler.sendEmptyMessage(104);
                    return;
                case 2:
                    this.mHandler.sendEmptyMessage(104);
                    return;
                default:
                    return;
            }
        }
    }

    public void onProviderEnabled(String provider) {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(102);
        }
    }

    public void onProviderDisabled(String provider) {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(103);
        }
    }

    public GPSUpLinkData getCurrentLocation() {
        return this.mGPSData;
    }

    public void onNmeaReceived(long timestamp, String nmea) {
        if (nmea != null && nmea.startsWith("$GPRMC") && checksumRMC(nmea)) {
            String[] segs = nmea.split(",");
            if (segs.length >= 12) {
                try {
                    float angle = Utilities.normalizeDegree(Float.parseFloat(segs[8]));
                    if (angle < 0.0f || angle >= 180.0f) {
                        this.mGPSData.angle = angle - 360.0f;
                        return;
                    }
                    this.mGPSData.angle = angle;
                } catch (NumberFormatException e) {
                    this.mGPSData.angle = 0.0f;
                }
            }
        }
    }

    private boolean checksumRMC(String rmc) {
        return true;
    }

    private boolean checkValidGpsData() {
        return (this.mGPSData == null || this.mGPSData.reset || this.mGPSData.no_satelites <= 0 || this.mGPSData.accuracy <= 0.0f || (this.mGPSData.lon == 0.0f && this.mGPSData.lat == 0.0f)) ? false : true;
    }
}
