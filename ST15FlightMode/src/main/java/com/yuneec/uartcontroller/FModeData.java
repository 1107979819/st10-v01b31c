package com.yuneec.uartcontroller;

import java.util.HashMap;

public class FModeData {
    private static final int FMDOE_COLOR_BLUE = -16776961;
    private static final int FMDOE_COLOR_BLUE_FLASHING = -1342177025;
    private static final int FMDOE_COLOR_DEFAULT = -16724737;
    private static final int FMDOE_COLOR_GREEN = -16711936;
    private static final int FMDOE_COLOR_GREEN_FLASHING = -1342112000;
    private static final int FMDOE_COLOR_PURPLE = -65296;
    private static final int FMDOE_COLOR_PURPLE_FLASHING = -1325465360;
    private static final int FMDOE_COLOR_RED = -65536;
    private static final int FMDOE_COLOR_RED_FLASHING = -1325465600;
    public static final int FMODE_ACCELBIAS_CALI = 11;
    public static final int FMODE_BINDING = 15;
    public static final int FMODE_BLUE_FLASHING = 1;
    public static final int FMODE_BLUE_SOLID = 0;
    public static final int FMODE_BLUE_WOULD_BE_SOLID_NO_GPS = 2;
    public static final int FMODE_CAMERA_TRACKING = 23;
    public static final int FMODE_CAMERA_TRACKING_NO_GPS = 24;
    private static final HashMap<Integer, Integer> FMODE_COLOR_MAP = new HashMap();
    public static final int FMODE_EMERGENCY_KILLED = 12;
    public static final int FMODE_FOLLOW = 21;
    public static final int FMODE_FOLLOW_NO_GPS = 22;
    public static final int FMODE_GO_HOME = 13;
    public static final int FMODE_LANDING = 14;
    public static final int FMODE_MAG_CALIB = 18;
    public static final int FMODE_MOTORS_STARTING = 8;
    public static final int FMODE_PRESS_CALIB = 10;
    public static final int FMODE_PURPLE_FLASHING = 4;
    public static final int FMODE_PURPLE_SOLID = 3;
    public static final int FMODE_PURPLE_WOULD_BE_SOLID_NO_GPS = 5;
    public static final int FMODE_RATE = 20;
    public static final int FMODE_READY_TO_START = 16;
    public static final int FMODE_SMART = 6;
    public static final int FMODE_SMART_BUT_NO_GPS = 7;
    private static final HashMap<Integer, String> FMODE_STRING_MAP = new HashMap();
    private static final HashMap<Integer, String> FMODE_STRING_MAP_380 = new HashMap();
    public static final int FMODE_TEMP_CALIB = 9;
    public static final int FMODE_UNKNOWN = 19;
    public static final int FMODE_WAITING_FOR_RC = 17;
    public static final int VEHICLE_TYPE_350QX = 3;
    public static final int VEHICLE_TYPE_380QX = 4;
    public static final int VEHICLE_TYPE_H920 = 1;
    public static final int VEHICLE_TYPE_Q500 = 2;
    public static final int VEHICLE_TYPE_TYPHOON_H = 5;
    public int fMode;
    public int fModeColor;
    public String fModeString;
    public boolean flashing;

    static {
        FMODE_STRING_MAP.put(Integer.valueOf(0), "THR");
        FMODE_STRING_MAP.put(Integer.valueOf(1), "THR");
        FMODE_STRING_MAP.put(Integer.valueOf(2), "THR");
        FMODE_STRING_MAP.put(Integer.valueOf(3), "Angle");
        FMODE_STRING_MAP.put(Integer.valueOf(4), "Angle");
        FMODE_STRING_MAP.put(Integer.valueOf(5), "Angle");
        FMODE_STRING_MAP.put(Integer.valueOf(6), "Smart");
        FMODE_STRING_MAP.put(Integer.valueOf(7), "Angle");
        FMODE_STRING_MAP.put(Integer.valueOf(8), "Start");
        FMODE_STRING_MAP.put(Integer.valueOf(9), "Temp");
        FMODE_STRING_MAP.put(Integer.valueOf(10), "Pre Cali");
        FMODE_STRING_MAP.put(Integer.valueOf(11), "Acc Cali");
        FMODE_STRING_MAP.put(Integer.valueOf(12), "EMER");
        FMODE_STRING_MAP.put(Integer.valueOf(13), "Home");
        FMODE_STRING_MAP.put(Integer.valueOf(14), "Land");
        FMODE_STRING_MAP.put(Integer.valueOf(15), "Bind");
        FMODE_STRING_MAP.put(Integer.valueOf(16), "Ready");
        FMODE_STRING_MAP.put(Integer.valueOf(17), "No RC");
        FMODE_STRING_MAP.put(Integer.valueOf(18), "Mag Cali");
        FMODE_STRING_MAP.put(Integer.valueOf(20), "Rate");
        FMODE_STRING_MAP.put(Integer.valueOf(21), "Follow");
        FMODE_STRING_MAP.put(Integer.valueOf(22), "Follow");
        FMODE_STRING_MAP.put(Integer.valueOf(23), "Watch");
        FMODE_STRING_MAP.put(Integer.valueOf(24), "Watch");
        FMODE_STRING_MAP_380.put(Integer.valueOf(0), "Stab");
        FMODE_STRING_MAP_380.put(Integer.valueOf(1), "Stab");
        FMODE_STRING_MAP_380.put(Integer.valueOf(2), "Stab");
        FMODE_STRING_MAP_380.put(Integer.valueOf(3), "AP");
        FMODE_STRING_MAP_380.put(Integer.valueOf(4), "AP");
        FMODE_STRING_MAP_380.put(Integer.valueOf(5), "AP");
        FMODE_STRING_MAP_380.put(Integer.valueOf(6), "Smart");
        FMODE_STRING_MAP_380.put(Integer.valueOf(7), "AP");
        FMODE_STRING_MAP_380.put(Integer.valueOf(8), "Start");
        FMODE_STRING_MAP_380.put(Integer.valueOf(9), "Temp");
        FMODE_STRING_MAP_380.put(Integer.valueOf(10), "Pre Cali");
        FMODE_STRING_MAP_380.put(Integer.valueOf(11), "Acc Cali");
        FMODE_STRING_MAP_380.put(Integer.valueOf(12), "EMER");
        FMODE_STRING_MAP_380.put(Integer.valueOf(13), "Home");
        FMODE_STRING_MAP_380.put(Integer.valueOf(14), "Land");
        FMODE_STRING_MAP_380.put(Integer.valueOf(15), "Bind");
        FMODE_STRING_MAP_380.put(Integer.valueOf(16), "Ready");
        FMODE_STRING_MAP_380.put(Integer.valueOf(17), "No RC");
        FMODE_STRING_MAP_380.put(Integer.valueOf(18), "Mag Cali");
        FMODE_STRING_MAP_380.put(Integer.valueOf(20), "Agil");
        FMODE_STRING_MAP_380.put(Integer.valueOf(21), "Follow");
        FMODE_STRING_MAP_380.put(Integer.valueOf(22), "Follow");
        FMODE_STRING_MAP_380.put(Integer.valueOf(23), "Track");
        FMODE_STRING_MAP_380.put(Integer.valueOf(24), "Track");
        FMODE_COLOR_MAP.put(Integer.valueOf(0), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(1), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(2), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(3), Integer.valueOf(FMDOE_COLOR_PURPLE));
        FMODE_COLOR_MAP.put(Integer.valueOf(4), Integer.valueOf(FMDOE_COLOR_PURPLE_FLASHING));
        FMODE_COLOR_MAP.put(Integer.valueOf(5), Integer.valueOf(FMDOE_COLOR_PURPLE_FLASHING));
        FMODE_COLOR_MAP.put(Integer.valueOf(6), Integer.valueOf(FMDOE_COLOR_GREEN));
        FMODE_COLOR_MAP.put(Integer.valueOf(7), Integer.valueOf(FMDOE_COLOR_PURPLE));
        FMODE_COLOR_MAP.put(Integer.valueOf(8), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(9), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(10), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(11), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(12), Integer.valueOf(FMDOE_COLOR_RED));
        FMODE_COLOR_MAP.put(Integer.valueOf(13), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(14), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(15), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(16), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(17), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(18), Integer.valueOf(FMDOE_COLOR_DEFAULT));
        FMODE_COLOR_MAP.put(Integer.valueOf(20), Integer.valueOf(FMDOE_COLOR_RED));
        FMODE_COLOR_MAP.put(Integer.valueOf(21), Integer.valueOf(FMDOE_COLOR_GREEN));
        FMODE_COLOR_MAP.put(Integer.valueOf(22), Integer.valueOf(FMDOE_COLOR_RED));
        FMODE_COLOR_MAP.put(Integer.valueOf(23), Integer.valueOf(FMDOE_COLOR_GREEN));
        FMODE_COLOR_MAP.put(Integer.valueOf(24), Integer.valueOf(FMDOE_COLOR_RED));
    }

    public static String getFModeString(int key, int vehicleType) {
        String str;
        if (vehicleType == 4) {
            if (FMODE_STRING_MAP_380 == null || !FMODE_STRING_MAP_380.containsKey(Integer.valueOf(key))) {
                return "N/A";
            }
            str = (String) FMODE_STRING_MAP_380.get(Integer.valueOf(key));
        } else if (FMODE_STRING_MAP == null || !FMODE_STRING_MAP.containsKey(Integer.valueOf(key))) {
            return "N/A";
        } else {
            str = (String) FMODE_STRING_MAP.get(Integer.valueOf(key));
        }
        return str;
    }

    public static int getFModeColor(int key) {
        if (FMODE_COLOR_MAP == null || !FMODE_COLOR_MAP.containsKey(Integer.valueOf(key))) {
            return FMDOE_COLOR_DEFAULT;
        }
        return ((Integer) FMODE_COLOR_MAP.get(Integer.valueOf(key))).intValue();
    }

    public static boolean isFlashing(int key) {
        if (key == 1 || key == 2 || key == 4 || key == 5 || key == 7) {
            return true;
        }
        return false;
    }

    public static boolean isMotorWorking(int key) {
        if ((key < 0 || key > 8) && ((key < 20 || key > 24) && key != 13)) {
            return false;
        }
        return true;
    }

    public void setData(int key, int vehicleType) {
        this.fMode = key;
        this.fModeString = getFModeString(key, vehicleType);
        this.fModeColor = getFModeColor(key);
        this.flashing = isFlashing(key);
    }
}
