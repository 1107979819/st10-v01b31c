package com.yuneec.uartcontroller;

public class FCSensorData {
    public static final int ACCELEROMETER_IN_IMU_INIT_STATUS = 4;
    public static final int ACCELEROMETER_IN_IMU_WARNING_STATUS = 32;
    public static final int COMPASS1_IN_FC0_STATUS = 4;
    public static final int COMPASS2_IN_IMU_STATUS = 8;
    public static final int FC0_AND_IMU_DATA_MISMATCH_WARNING_STATUS = 64;
    public static final int GPS1_IN_FC0_STATUS = 16;
    public static final int GPS2_IN_IMU_STATUS = 32;
    public static final int MPU6050_IN_FC0_INIT_STATUS = 1;
    public static final int MPU6050_IN_FC0_WARNING_STATUS = 8;
    public static final int MPU6050_IN_IMU_INIT_STATUS = 2;
    public static final int MPU6050_IN_IMU_WARNING_STATUS = 16;
    public static final int PRESSURE_IN_FC0_STATUS = 1;
    public static final int PRESSURE_IN_IMU_STATUS = 2;
    public String mError;
    public int mImuStatus;
    public int mPressCompassGpsStatus;

    public static String getImuStatusError(int status) {
        if ((status & 1) == 0) {
            return "IMU Fail";
        }
        return null;
    }

    public static String getPressCompassGpsStatusError(int status) {
        if ((status & 4) == 0) {
            return "Mag Fail";
        }
        if ((status & 1) == 0) {
            return "Pre Fail";
        }
        if ((status & 16) == 0) {
            return "GPS Fail";
        }
        return null;
    }

    public static String getError(int imuStatus, int pressCompassGpsStatus) {
        String error = getImuStatusError(imuStatus);
        if (error == null) {
            return getPressCompassGpsStatusError(pressCompassGpsStatus);
        }
        return error;
    }

    public void setData(int imuStatus, int pressCompassGpsStatus) {
        this.mImuStatus = imuStatus;
        this.mPressCompassGpsStatus = pressCompassGpsStatus;
        this.mError = getError(imuStatus, pressCompassGpsStatus);
    }
}
