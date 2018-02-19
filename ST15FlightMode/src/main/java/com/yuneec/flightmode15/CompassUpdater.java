package com.yuneec.flightmode15;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import java.util.Timer;
import java.util.TimerTask;

public class CompassUpdater implements SensorEventListener {
    public static final int EVENT_ACCURACY_CHANGED = 201;
    public static final int EVENT_COMPASS_UPDATED = 200;
    private static final int UPDATE_INTERVAL = 200;
    private int mDegree;
    private Handler mHandler;
    private SensorManager mSensorManager;
    private Timer mUpdateTimer;
    private UpdateTimerTask mUpdateTimerTask;

    private class UpdateTimerTask extends TimerTask {
        private UpdateTimerTask() {
        }

        public void run() {
            if (CompassUpdater.this.mHandler != null) {
                Message message = Message.obtain();
                message.what = 200;
                message.arg1 = CompassUpdater.this.mDegree;
                CompassUpdater.this.mHandler.sendMessage(message);
            }
        }
    }

    public CompassUpdater(Context context) {
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
    }

    private void registerHandler(Handler handler) {
        this.mHandler = handler;
    }

    private void unregisterHandler() {
        this.mHandler = null;
    }

    public void start(Handler handler) {
        registerHandler(handler);
        this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(3), 2);
        this.mUpdateTimer = new Timer("CompassUpdater");
        this.mUpdateTimerTask = new UpdateTimerTask();
        this.mUpdateTimer.scheduleAtFixedRate(this.mUpdateTimerTask, 200, 200);
    }

    public void stop() {
        unregisterHandler();
        this.mSensorManager.unregisterListener(this, this.mSensorManager.getDefaultSensor(3));
        if (this.mUpdateTimer != null) {
            this.mUpdateTimer.cancel();
            if (this.mUpdateTimerTask != null) {
                this.mUpdateTimerTask.cancel();
            }
            this.mUpdateTimer = null;
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if (this.mHandler != null) {
            this.mDegree = (int) Utilities.normalizeDegree(event.values[0] * Utilities.B_SWITCH_MIN);
            this.mDegree = (int) Utilities.normalizeDegree(((float) this.mDegree) * Utilities.B_SWITCH_MIN);
            this.mDegree = (this.mDegree + 90) % 360;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (this.mHandler != null) {
            Message message = Message.obtain();
            message.what = 201;
            message.arg1 = accuracy;
            this.mHandler.sendMessage(message);
        }
    }
}
