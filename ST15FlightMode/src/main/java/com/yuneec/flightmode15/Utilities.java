package com.yuneec.flightmode15;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.ImageView;
import com.yuneec.channelsettings.DRData;
import com.yuneec.channelsettings.DR_Fragment;
import com.yuneec.channelsettings.ServoData;
import com.yuneec.channelsettings.ServoSetupFragment;
import com.yuneec.channelsettings.ThrottleCurveFragment;
import com.yuneec.channelsettings.ThrottleData;
import com.yuneec.database.DBOpenHelper;
import com.yuneec.database.DataProvider;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flight_settings.BindWifiManage;
import com.yuneec.flight_settings.ChannelMap;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flight_settings.WifiConnect;
import com.yuneec.uartcontroller.MixedData;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import com.yuneec.widget.MyProgressDialog;
import com.yuneec.widget.StatusbarView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Utilities {
    public static int BIND_KEY_INDEX = 0;
    public static final float B_EXPO_1_MAX = 1.0f;
    public static final float B_EXPO_1_MID = 0.17f;
    public static final float B_EXPO_1_MIN = 0.0f;
    public static final float B_EXPO_2_MAX = 0.5f;
    public static final float B_EXPO_2_MID = 0.0f;
    public static final float B_EXPO_2_MIN = -0.5f;
    public static final float B_SWITCH_MAX = 1.0f;
    public static final float B_SWITCH_MID = 0.0f;
    public static final float B_SWITCH_MIN = -1.0f;
    public static int CAMERA_KEY_INDEX = 0;
    public static float CONVERSION_MI_TO_FT = 5280.0f;
    public static float CONVERSION_MPS_TO_KPH = 3.6f;
    public static float CONVERSION_MPS_TO_MPH = 2.2369f;
    public static float CONVERSION_MTR_TO_FT = 3.2808f;
    public static final int DEFAULT_ANALOG_NUM = 10;
    public static final int DEFAULT_CHANNEL_NUM = 12;
    public static final int DEFAULT_FMODE_NUM = 3;
    public static final int DEFAULT_SWITCH_NUM = 2;
    public static final int EXPO_MAX = 100;
    public static final int EXPO_MID = 0;
    public static final int EXPO_MIN = -100;
    private static final String FIRST_START_PREFERENCE = "first_start_preference";
    public static final int FLAG_HOMEKEY_DISPATCHED = Integer.MIN_VALUE;
    public static int FMODE_KEY_INDEX = 0;
    public static final boolean FMODE_OPTION_OPEN = false;
    public static final int FPV_MODE = 1;
    public static final long GIMBAL_CONTROL_MODEL = 406;
    public static final ArrayList<String> HARDWARE_ARRAYLIST = new ArrayList();
    public static final int HARDWARE_TYPE_ANALOG = 0;
    public static final int HARDWARE_TYPE_SWITCH = 1;
    public static final int HARDWARE_TYPE_TOUCHSCREEN = 2;
    public static int HW_B_BASE = 50;
    public static SparseIntArray HW_CHANNEL_INDEX = new SparseIntArray();
    public static int HW_J_BASE = 0;
    public static int HW_K_BASE = 10;
    public static int HW_S_BASE = 30;
    public static int HW_T_BASE = 20;
    public static int HW_VB_BASE = 90;
    public static int HW_VS_BASE = 70;
    public static final float K_MAX = 2.0f;
    public static final float K_MID = 0.67f;
    public static final float K_MIN = 0.5f;
    public static final float LEFT_K_MAX = 1.0f;
    public static final float LEFT_K_MID = 0.17f;
    public static final float LEFT_K_MIN = 0.0f;
    public static final int NO_MODEL_SELECTED = -2;
    public static final float N_MAX = 4.0f;
    public static final float N_MID = 1.0f;
    public static final float N_MIN = 0.3f;
    public static final int OFFSET_MAX = 100;
    public static final int OFFSET_MID = 0;
    public static final int OFFSET_MIN = -100;
    public static final int OFFSET_SWITCH_MAX_1 = 125;
    public static final int OFFSET_SWITCH_MAX_2 = 150;
    public static final int OFFSET_SWITCH_MID_1 = 0;
    public static final int OFFSET_SWITCH_MID_2 = 0;
    public static final int OFFSET_SWITCH_MIN_1 = -25;
    public static final int OFFSET_SWITCH_MIN_2 = -150;
    public static final boolean OPTIMIZE_PICKER_SOUND_POOL = true;
    public static final String PROJECT_TAG = "ST10";
    public static final int RATE_MAX = 100;
    public static final int RATE_MID = 0;
    public static final int RATE_MIN = -100;
    public static final int RC_MODE = 0;
    public static final float RIGHT_K_MAX = 1.5f;
    public static final float RIGHT_K_MID = 0.84f;
    public static final float RIGHT_K_MIN = -0.3f;
    public static final String SDCARD2_PATH = "/storage/sdcard1";
    private static final String TAG = "Utilities";
    public static final int THROTTLE_CHANNEL = 1;
    public static final boolean TRIM_ENABLE = false;
    public static String UNIT_FEET = "ft";
    public static String UNIT_FEET_PER_SECOND = "fps";
    public static String UNIT_KELOMETER = "km";
    public static String UNIT_KELOMETER_PER_HOUR = "kph";
    public static String UNIT_METER = "m";
    public static String UNIT_METER_PER_SECOND = "mps";
    public static String UNIT_MILE = "mi";
    public static String UNIT_MILE_PER_HOUR = "mph";
    public static int VIDEO_KEY_INDEX;
    public static int ZOOM_KEY_INDEX;
    public static boolean isWIFIConnected = false;
    private static SharedPreferences mPrefsUtil;
    public static boolean mSendDataCompleted = false;
    private static int sCurrentMode = -1;
    private static boolean sIsRunningMode = false;
    private static String[] sKillAppsBlacklist = new String[]{"com.android.quicksearchbox", "com.android.musicfx", "com.android.defcontainer", "com.android.settings", "com.android.gallery3d", "com.mediatek.weather", "com.mediatek.appwidget.weather", "com.android.contacts", "com.android.providers.calendar", "com.android.deskclock", "com.android.email", "com.android.exchange", "com.android.mms", "com.android.calendar"};
    private static String[] sKillAppsWhitelist = new String[]{"com.android", "android", "system", "com.mediatek", "com.yuneec"};
    private static MyProgressDialog sProgressDialog;
    public static final int[] value_ch5 = new int[3];
    public static final int[] value_ch6 = new int[3];

    private static class ImportPreloadedModelsTask extends AsyncTask<Context, Void, Integer> {
        private ImportPreloadedModelsTask() {
        }

        protected Integer doInBackground(Context... params) {
            return Integer.valueOf(Utilities.importPerloadedModels(params[0]));
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Utilities.dismissProgressDialog();
        }
    }

    public static class ReceiverInfomation {
        public int analogChBit;
        public int analogChNumber;
        public int analogChNumber_min;
        public int channelNumber;
        public int receiver;
        public int switchChBit;
        public int switchChNumber;
        public int switchChNumber_min;
    }

    static {
        HARDWARE_ARRAYLIST.add("INH");
        HARDWARE_ARRAYLIST.add("J1");
        HARDWARE_ARRAYLIST.add("J2");
        HARDWARE_ARRAYLIST.add("J3");
        HARDWARE_ARRAYLIST.add("J4");
        HARDWARE_ARRAYLIST.add("J5");
        HARDWARE_ARRAYLIST.add("J6");
        HARDWARE_ARRAYLIST.add("J7");
        HARDWARE_ARRAYLIST.add("J8");
        HARDWARE_ARRAYLIST.add("J9");
        HARDWARE_ARRAYLIST.add("J10");
        HARDWARE_ARRAYLIST.add("K1");
        HARDWARE_ARRAYLIST.add("K2");
        HARDWARE_ARRAYLIST.add("K3");
        HARDWARE_ARRAYLIST.add("K4");
        HARDWARE_ARRAYLIST.add("K5");
        HARDWARE_ARRAYLIST.add("K6");
        HARDWARE_ARRAYLIST.add("K7");
        HARDWARE_ARRAYLIST.add("K8");
        HARDWARE_ARRAYLIST.add("K9");
        HARDWARE_ARRAYLIST.add("K10");
        HARDWARE_ARRAYLIST.add("S1");
        HARDWARE_ARRAYLIST.add("S2");
        HARDWARE_ARRAYLIST.add("S3");
        HARDWARE_ARRAYLIST.add("S4");
        HARDWARE_ARRAYLIST.add("S5");
        HARDWARE_ARRAYLIST.add("S6");
        HARDWARE_ARRAYLIST.add("S7");
        HARDWARE_ARRAYLIST.add("S8");
        HARDWARE_ARRAYLIST.add("S9");
        HARDWARE_ARRAYLIST.add("S10");
        HARDWARE_ARRAYLIST.add("B1");
        HARDWARE_ARRAYLIST.add("B2");
        HARDWARE_ARRAYLIST.add("B3");
        HARDWARE_ARRAYLIST.add("B4");
        HARDWARE_ARRAYLIST.add("B5");
        HARDWARE_ARRAYLIST.add("B6");
        HARDWARE_ARRAYLIST.add("B7");
        HARDWARE_ARRAYLIST.add("B8");
        HARDWARE_ARRAYLIST.add("B9");
        HARDWARE_ARRAYLIST.add("B10");
    }

    public static int getHardwareIndexT(String value) {
        if (value != null) {
            return HARDWARE_ARRAYLIST.indexOf(value);
        }
        Log.e(TAG, "Invalid hardware:" + value);
        return -1;
    }

    public static String getHardwareValue(int indexT) {
        if (indexT < HARDWARE_ARRAYLIST.size()) {
            return (String) HARDWARE_ARRAYLIST.get(indexT);
        }
        return null;
    }

    public static int getHardwareType(String value) {
        int index = getHardwareIndexT(value);
        if (index > 0 && index <= 20) {
            return 1;
        }
        if (index <= 20 || index > 40) {
            return 0;
        }
        return 2;
    }

    public static void killBackgroundApps(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
        for (int i = 0; i < list.size(); i++) {
            String process = ((RunningAppProcessInfo) list.get(i)).processName;
            for (Object equals : sKillAppsBlacklist) {
                if (process.equals(equals)) {
                    am.killBackgroundProcesses(process);
                    Log.d(TAG, "killed :" + process);
                }
            }
            boolean dontkill = false;
            for (String startsWith : sKillAppsWhitelist) {
                if (process.startsWith(startsWith)) {
                    dontkill = true;
                    break;
                }
            }
            if (!dontkill) {
                am.killBackgroundProcesses(process);
                Log.d(TAG, "killed :" + process);
                if ("com.FrozenPepper.RcPlane2".equals(process)) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public static void showProgressDialog(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        if (sProgressDialog == null || !sProgressDialog.isShowing()) {
            sProgressDialog = MyProgressDialog.show(context, title, message, indeterminate, cancelable);
        } else {
            Log.w(TAG, "A Dialog is showing, can't show another");
        }
    }

    public static void dismissProgressDialog() {
        if (sProgressDialog != null) {
            sProgressDialog.cancel();
            sProgressDialog = null;
        }
    }

    public static void setCurrentMode(Activity activity, int mode) {
        sCurrentMode = 1;
    }

    public static int getCurrentMode() {
        return sCurrentMode;
    }

    public static void setRunningMode(boolean running) {
        sIsRunningMode = running;
    }

    public static boolean isRunningMode() {
        return sIsRunningMode;
    }

    public static void setImageUri(Context context, ImageView image, Uri uri) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            image.setImageDrawable(Drawable.createFromStream(is, null));
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "Unable to set ImageView from URI: " + e2.getMessage());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
            }
        }
    }

    public static void setImageThumbnail(Context context, ImageView image, Uri uri) {
        if ("content".equals(uri.getScheme())) {
            long id = -1;
            try {
                id = Long.valueOf(uri.getLastPathSegment()).longValue();
            } catch (NumberFormatException e) {
                Log.e(TAG, "Fail to parse content uri:" + uri);
            }
            if (id != -1) {
                Bitmap thumbnail = Thumbnails.getThumbnail(context.getContentResolver(), id, 3, null);
                if (thumbnail != null) {
                    image.setImageBitmap(thumbnail);
                    return;
                }
            }
            Log.e(TAG, "Fail to get Thumbnail");
        } else if ("file".equals(uri.getScheme())) {
            Log.w(TAG, "encounter file scheme,should not happen! uri:" + uri);
            setImageUri(context, image, uri);
        } else {
            setImageUri(context, image, uri);
        }
    }

    public static void backToFlightScreen(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    public static int isFPVModel(Context context, long model_id) {
        if (model_id != -2) {
            return 1;
        }
        return -1;
    }

    public static int getUnit(Context context) {
        return context.getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getInt(FlightSettings.VELOCITY_UNIT, 2);
    }

    public static String getDisplayVelocityUnit(Context context) {
        return getUnit(context) == 1 ? UNIT_KELOMETER_PER_HOUR : UNIT_MILE_PER_HOUR;
    }

    public static String FormatVelocityDisplayString(Context context, float velocity_metric) {
        float velocity = getDisplayVelocityUnit(context).equals(UNIT_KELOMETER_PER_HOUR) ? velocity_metric * CONVERSION_MPS_TO_KPH : velocity_metric * CONVERSION_MPS_TO_MPH;
        return context.getResources().getString(R.string.str_float_value, new Object[]{Float.valueOf(velocity), getDisplayVelocityUnit(context)});
    }

    public static String getDisplayLengthUnit(Context context) {
        return getUnit(context) == 1 ? UNIT_METER : UNIT_FEET;
    }

    public static String FormatLengthDisplayString(Context context, float length_metric) {
        float length;
        if (getDisplayLengthUnit(context).equals(UNIT_METER)) {
            length = length_metric;
        } else {
            length = length_metric * CONVERSION_MTR_TO_FT;
        }
        return context.getResources().getString(R.string.str_float_value, new Object[]{Float.valueOf(length), v_unit});
    }

    public static String FormatHomeDistanceDisplayString(Context context, float distance_metric) {
        String str_value = context.getResources().getString(R.string.str_value, new Object[]{"N/A", getDisplayLengthUnit(context)});
        if (distance_metric < 0.0f || distance_metric > 1.0E7f) {
            return str_value;
        }
        float distance;
        String unit;
        if (1 == getUnit(context)) {
            distance = distance_metric;
            unit = UNIT_METER;
            if (distance >= 10000.0f) {
                distance /= 1000.0f;
                unit = UNIT_KELOMETER;
            }
        } else {
            distance = distance_metric * CONVERSION_MTR_TO_FT;
            unit = UNIT_FEET;
            if (distance >= 10000.0f) {
                distance /= CONVERSION_MI_TO_FT;
                unit = UNIT_MILE;
            }
        }
        return context.getResources().getString(R.string.str_float_value, new Object[]{Float.valueOf(distance), unit});
    }

    public static String FormatPositionDisplayString(Context context, float lon, float lat) {
        String unit_lon = lon >= 0.0f ? "E" : "W";
        String unit_lat = lat >= 0.0f ? "N" : "S";
        float longitude = Math.abs(lon);
        float latitude = Math.abs(lat);
        return context.getResources().getString(R.string.str_pos, new Object[]{Float.valueOf(longitude), unit_lon, Float.valueOf(latitude), unit_lat});
    }

    public static boolean FormatPositionDisplayStatic(float longitude, float latitude) {
        if (-180.0f >= longitude || longitude >= 180.0f || -180.0f >= latitude || latitude >= 180.0f) {
            return false;
        }
        return true;
    }

    public static void UartControllerStandBy(UARTController controller) {
        controller.stopReading();
        controller.registerReaderHandler(null);
    }

    public static boolean ensureAwaitState(UARTController controller) {
        return controller.correctTxState(1, 3);
    }

    public static boolean ensureRunState(UARTController controller) {
        if (controller.enterRun(true) || controller.correctTxState(5, 3)) {
            return true;
        }
        return false;
    }

    public static boolean ensureSimState(UARTController controller) {
        if (controller.enterSim(true) || controller.correctTxState(6, 3)) {
            return true;
        }
        return false;
    }

    public static boolean ensureBindState(UARTController controller) {
        if (controller.enterBind(true) || controller.correctTxState(2, 3)) {
            return true;
        }
        return false;
    }

    public static boolean ensureCalibrationState(UARTController controller) {
        if (controller.startCalibration(true) || controller.correctTxState(3, 3)) {
            return true;
        }
        return false;
    }

    public static float[] calculateDistanceAndBearing(float startLatitude, float startLongitude, float endLatitude, float endLongitude) {
        float[] result = new float[2];
        if ((startLatitude == 0.0f && startLongitude == 0.0f) || (endLatitude == 0.0f && endLongitude == 0.0f)) {
            Log.d(TAG, "can't get plane or controller's location:" + startLatitude + "," + startLongitude + "..." + endLatitude + "," + endLongitude);
            result[0] = B_SWITCH_MIN;
            result[1] = B_SWITCH_MIN;
        }
        Location.distanceBetween((double) formatFloat(startLatitude, null), (double) formatFloat(startLongitude, null), (double) formatFloat(endLatitude, null), (double) formatFloat(endLongitude, null), result);
        if (result[1] < 0.0f) {
            result[1] = result[1] + 360.0f;
        }
        return result;
    }

    public static float[] calculateDistanceAndBearing(float startLatitude, float startLongitude, float startAltitude, float endLatitude, float endLongitude, float endAltitude) {
        float[] result = new float[2];
        if ((startLatitude == 0.0f && startLongitude == 0.0f) || (endLatitude == 0.0f && endLongitude == 0.0f)) {
            Log.d(TAG, "can't get plane or controller's location:" + startLatitude + "," + startLongitude + "..." + endLatitude + "," + endLongitude);
            result[0] = B_SWITCH_MIN;
            result[1] = B_SWITCH_MIN;
        }
        startLatitude = formatFloat(startLatitude, null);
        startLongitude = formatFloat(startLongitude, null);
        startAltitude = formatFloat(startAltitude, null);
        endLatitude = formatFloat(endLatitude, null);
        endLongitude = formatFloat(endLongitude, null);
        endAltitude = formatFloat(endAltitude, null);
        Location.distanceBetween((double) startLatitude, (double) startLongitude, (double) endLatitude, (double) endLongitude, result);
        result[0] = (float) Math.sqrt(Math.pow((double) result[0], 2.0d) + Math.pow((double) (endAltitude - startAltitude), 2.0d));
        if (result[1] < 0.0f) {
            result[1] = result[1] + 360.0f;
        }
        return result;
    }

    public static float formatFloat(float f, String format) {
        if (format == null || format.length() == 0) {
            format = "0.00000";
        }
        try {
            f = Float.valueOf(new DecimalFormat(format).format((double) f)).floatValue();
        } catch (NumberFormatException e) {
        }
        return f;
    }

    public static boolean checkDefaultMixingDataExisted(Context context, int model_type) {
        try {
            context.getAssets().open(String.valueOf(model_type) + "/manifest.csv").close();
            InputStream is = context.getAssets().open(String.valueOf(model_type) + "/curves.csv");
            is.close();
            context.getAssets().open(String.valueOf(model_type) + "/switches.csv");
            is.close();
            return true;
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
            return false;
        }
    }

    public static void checkFirstStart(Context context) {
        if (isFirstStart(context)) {
            readSDcard(context);
            showProgressDialog(context, null, context.getResources().getText(R.string.dialog_importing_model), false, false);
            new ImportPreloadedModelsTask().execute(new Context[]{context});
        }
    }

    private static boolean isFirstStart(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FIRST_START_PREFERENCE, 0);
        boolean isFirst = sp.getBoolean("is_first", true);
        if (isFirst) {
            sp.edit().putBoolean("is_first", false).commit();
        }
        return isFirst;
    }

    private static int importPerloadedModels(Context context) {
        int type = -1;
        String[] floder_list = null;
        String location = DBOpenHelper.MODEL_TABLE_NAME;
        try {
            floder_list = context.getAssets().list(location);
            Log.d(TAG, "getLocals");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (floder_list != null) {
            for (String append : floder_list) {
                type = importModelFromAssets(context, new StringBuilder(String.valueOf(location)).append("/").append(append).toString());
            }
        }
        return type;
    }

    private static int importModelFromAssets(Context context, String path) {
        return new ModelImporter(context).importModelFromAssets(path);
    }

    public static boolean sendDataToFlightControl(Context context, UARTController controller, int fMode, int channel, String hardware, long self_id, int opt) {
        return false;
    }

    public static boolean sendFmodeKey(Context context, long model_id, UARTController controller) {
        boolean success = controller.setFmodeKey(true, DataProviderHelper.getFmodeKeyFromDatabase(context, model_id), 3);
        if (!success) {
            Log.e(TAG, "Failed to send F-Mode key to FCS");
        }
        return success;
    }

    public static void resolvePointsArray(String[] points, float[] x, float[] y) {
        if (points == null) {
            return;
        }
        if (points.length > 9) {
            Log.e(TAG, "Error format of points");
            return;
        }
        for (int j = 0; j < points.length; j++) {
            if (points[j] != null) {
                String[] temp = points[j].split("&");
                if (temp.length != 2) {
                    Log.e(TAG, "pointArray's format is wrong");
                    return;
                }
                if (x != null) {
                    try {
                        x[j] = Float.parseFloat(temp[0]);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "NumberFormatException:" + e.getMessage());
                        return;
                    }
                }
                if (y != null) {
                    y[j] = Float.parseFloat(temp[1]);
                }
            }
        }
    }

    public static void saveRxResInfoToDatabase(Context context, UARTController controller, long model_id, int rxAddr) {
        byte[] rxResInfo = controller.getRxResInfo(rxAddr);
        if (rxResInfo != null && model_id != -2) {
            Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id);
            ContentValues cv = new ContentValues();
            cv.put(DBOpenHelper.KEY_RX_RES_INFO_BLOB, rxResInfo);
            context.getContentResolver().update(uri, cv, null, null);
        }
    }

    public static boolean sendRxResInfoFromDatabase(Context context, UARTController controller, long model_id) {
        byte[] rxResInfo = null;
        ContentResolver cr = context.getContentResolver();
        if (model_id != -2) {
            Cursor cursor = cr.query(ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id), new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_RX_RES_INFO_BLOB}, null, null, null);
            if (DataProviderHelper.isCursorValid(cursor)) {
                rxResInfo = cursor.getBlob(cursor.getColumnIndex(DBOpenHelper.KEY_RX_RES_INFO_BLOB));
                cursor.close();
            }
        }
        if (rxResInfo != null) {
            return controller.sendRxResInfo(rxResInfo);
        }
        return false;
    }

    public static float converRateLtoCoefficient_expo1(float rateL) {
        if (rateL <= 100.0f && rateL >= 0.0f) {
            return 0.17f + (((rateL - 0.0f) * 0.83f) / 100.0f);
        }
        if (rateL < -100.0f || rateL >= 0.0f) {
            return 0.0f;
        }
        return 0.17f - (((0.0f - rateL) * 0.17f) / 100.0f);
    }

    public static float converRateRtoCoefficient_expo1(float rateR) {
        if (rateR <= 100.0f && rateR >= 0.0f) {
            return RIGHT_K_MID + (((rateR - 0.0f) * 0.66f) / 100.0f);
        }
        if (rateR < -100.0f || rateR >= 0.0f) {
            return 0.0f;
        }
        return RIGHT_K_MID - (((0.0f - rateR) * 1.14f) / 100.0f);
    }

    public static float convertRateToCoefficientK(float rate) {
        if (rate <= 100.0f && rate >= 0.0f) {
            return K_MID + (((rate - 0.0f) * 1.3299999f) / 100.0f);
        }
        if (rate < -100.0f || rate >= 0.0f) {
            return 0.0f;
        }
        return K_MID - (((0.0f - rate) * 0.17000002f) / 100.0f);
    }

    public static double convertRateToCoefficientK(float rateL, float rateR) {
        return 0.0d;
    }

    public static float convertExpoToCoefficientN(float expo) {
        if (expo <= 100.0f && expo >= 0.0f) {
            return 1.0f + (((expo - 0.0f) * 3.0f) / 100.0f);
        }
        if (expo < -100.0f || expo >= 0.0f) {
            return 0.0f;
        }
        return 1.0f - (((0.0f - expo) * 0.7f) / 100.0f);
    }

    public static int convertCoefficienttoRateLeft_expo1(double k_l) {
        if (k_l >= 0.17000000178813934d && k_l <= 1.0d) {
            return (int) ((((k_l - 0.17000000178813934d) * 100.0d) / 0.8299999833106995d) + 0.0d);
        }
        if (k_l < 0.0d || k_l >= 0.17000000178813934d) {
            return 0;
        }
        return (int) (0.0d - (((0.17000000178813934d - k_l) * 100.0d) / 0.17000000178813934d));
    }

    public static int convertCoefficienttoRateRight_expo1(double k_r) {
        if (k_r >= 0.8399999737739563d && k_r <= 1.5d) {
            return (int) ((((k_r - 0.8399999737739563d) * 100.0d) / 0.6600000262260437d) + 0.0d);
        }
        if (k_r < -0.30000001192092896d || k_r >= 0.8399999737739563d) {
            return 0;
        }
        return (int) (0.0d - (((0.8399999737739563d - k_r) * 100.0d) / 1.1399999856948853d));
    }

    public static int convertCoefficientKtoRate(double k) {
        if (k >= 0.6700000166893005d && k <= 2.0d) {
            return (int) ((((k - 0.6700000166893005d) * 100.0d) / 1.3299999237060547d) + 0.0d);
        }
        if (k < 0.5d || k >= 0.6700000166893005d) {
            return 0;
        }
        return (int) (0.0d - (((0.6700000166893005d - k) * 100.0d) / 0.17000001668930054d));
    }

    public static float convertCoefficientNtoExpo(float n) {
        if (n >= 1.0f && n <= N_MAX) {
            return 0.0f + (((n - 1.0f) * 100.0f) / 3.0f);
        }
        if (n < N_MIN || n >= 1.0f) {
            return 0.0f;
        }
        return 0.0f - (((1.0f - n) * 100.0f) / 0.7f);
    }

    public static int convertCoefficientBtoOffset(double b, int hw_type) {
        if (hw_type == 2) {
            return (int) ((((b - 0.0d) * 200.0d) / 1.0d) + 0.0d);
        }
        return 0;
    }

    public static Point[] countAllPointOfExpo1(int axisX, int axisY, int axisWidth, int axisHeight, float k, float n, float b) {
        Point[] allPoints = new Point[(axisWidth + 1)];
        for (int i = 0; i < allPoints.length; i++) {
            float x = ((float) (i * 1)) / ((float) axisWidth);
            allPoints[i] = new Point();
            allPoints[i].x = axisX + i;
            float y = (float) ((((double) k) * Math.pow((double) x, (double) n)) + ((double) b));
            if (y < 0.0f) {
                y = 0.0f;
            } else if (y > 1.0f) {
                y = 1.0f;
            }
            allPoints[i].y = (axisY + axisHeight) - Math.round(((float) axisHeight) * y);
        }
        return allPoints;
    }

    public static Point[] countAllPointOfExpo2(int axisX, int axisY, int axisWidth, int axisHeight, float k1, float k2, float n1, float n2, float b) {
        int j;
        Point[] allPoints = new Point[(axisWidth + 1)];
        if (allPoints.length % 2 != 0) {
            j = axisWidth / 2;
        } else {
            j = (axisWidth / 2) + 1;
        }
        int i = 0;
        while (i <= axisWidth / 2) {
            float x1 = 1.0f - ((((float) i) * K_MAX) / ((float) axisWidth));
            float x2 = (((float) i) * K_MAX) / ((float) axisWidth);
            allPoints[i] = new Point();
            allPoints[j] = new Point();
            allPoints[i].x = axisX + i;
            allPoints[j].x = axisX + j;
            float y1 = (float) ((((double) k1) * Math.pow((double) x1, (double) n1)) - ((double) b));
            float y2 = (float) ((((double) k2) * Math.pow((double) x2, (double) n2)) + ((double) b));
            allPoints[j].y = ((axisHeight / 2) + axisY) - Math.round((((float) axisHeight) * y2) / K_MAX);
            allPoints[i].y = ((axisHeight / 2) + axisY) + Math.round((((float) axisHeight) * y1) / K_MAX);
            if (allPoints[i].y > axisY + axisHeight) {
                allPoints[i].y = axisY + axisHeight;
            }
            if (allPoints[j].y < axisY) {
                allPoints[j].y = axisY;
            }
            i++;
            j++;
        }
        return allPoints;
    }

    public static Integer[] get17PointsValueForFightControl(int maxValue, int minValue, float[] values) {
        Integer[] points = new Integer[17];
        for (int i = 0; i < values.length - 1; i++) {
            points[i * 2] = Integer.valueOf(((int) (values[i] * 10.0f)) - (minValue * 10));
            points[(i * 2) + 1] = Integer.valueOf(((int) (((values[i + 1] + values[i]) / K_MAX) * 10.0f)) - (minValue * 10));
        }
        points[16] = Integer.valueOf((int) ((values[8] * 10.0f) - ((float) (minValue * 10))));
        return points;
    }

    public static Integer[] get17PointsValueForFightControl(float maxValue, float minValue, int[] chValues) {
        Integer[] points = new Integer[17];
        int i = 0;
        int j = 0;
        points[0] = Integer.valueOf(((int) ((((float) (chValues[0] + 0)) * (maxValue - minValue)) / 4095.0f)) * 10);
        while (i < chValues.length && j < 16) {
            i += chValues.length / 16;
            j++;
            points[j] = Integer.valueOf((int) (((((float) (chValues[i] + 0)) * (maxValue - minValue)) / 4095.0f) * 10.0f));
        }
        return points;
    }

    public static float[] convertPositionToValue(int value_max, int value_min, int pos_max, int pos_min, int[] allPoints) {
        float[] values = new float[allPoints.length];
        for (int i = 0; i < allPoints.length; i++) {
            values[i] = ((float) value_min) + (((float) ((allPoints[i] - pos_min) * (value_max - value_min))) / ((float) (pos_max - pos_min)));
        }
        return values;
    }

    public static ReceiverInfomation getReceiverInfo(Context context, long model_id) {
        ReceiverInfomation rec_Info = new ReceiverInfomation();
        Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id), new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_RX, DBOpenHelper.KEY_RX_ANALOG_NUM, DBOpenHelper.KEY_RX_ANALOG_BIT, DBOpenHelper.KEY_RX_SWITCH_NUM, DBOpenHelper.KEY_RX_SWITCH_BIT, DBOpenHelper.KEY_ANALOG_MIN, DBOpenHelper.KEY_SWITCH_MIN}, null, null, null);
        if (DataProviderHelper.isCursorValid(cursor)) {
            rec_Info.receiver = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_RX));
            rec_Info.analogChNumber = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_RX_ANALOG_NUM));
            rec_Info.analogChBit = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_RX_ANALOG_BIT));
            rec_Info.switchChNumber = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_RX_SWITCH_NUM));
            rec_Info.switchChBit = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_RX_SWITCH_BIT));
            rec_Info.channelNumber = rec_Info.analogChNumber + rec_Info.switchChNumber;
            rec_Info.analogChNumber_min = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_ANALOG_MIN));
            rec_Info.switchChNumber_min = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_SWITCH_MIN));
            cursor.close();
            return rec_Info;
        }
        Log.e(TAG, "Read switch curve params, Cursor is invalid");
        return null;
    }

    public static String getCurrentRxAddrFromDB(Context context, long model_id) {
        Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id);
        Cursor c = context.getContentResolver().query(uri, new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_RX}, null, null, null);
        if (!DataProviderHelper.isCursorValid(c)) {
            return null;
        }
        String node_id = c.getString(c.getColumnIndex(DBOpenHelper.KEY_RX));
        c.close();
        return node_id;
    }

    public static int getCurrentRxTypeFromDB(Context context, long model_id) {
        Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id);
        Cursor c = context.getContentResolver().query(uri, new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_RX_TYPE}, null, null, null);
        if (!DataProviderHelper.isCursorValid(c)) {
            return -1;
        }
        int type = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_RX_TYPE));
        c.close();
        return type;
    }

    public static void setStatusBarLeftText(Context context, StatusbarView status) {
        if (status != null) {
            String model_name;
            long model_id = context.getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
            if (model_id == -2) {
                model_name = context.getString(R.string.no_model);
            } else if (getCurrentMode() != isFPVModel(context, model_id)) {
                model_name = context.getString(R.string.no_model);
            } else {
                Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id), new String[]{DBOpenHelper.KEY_NAME}, null, null, null);
                if (DataProviderHelper.isCursorValid(cursor)) {
                    String str = cursor.getString(cursor.getColumnIndex(DBOpenHelper.KEY_NAME));
                    cursor.close();
                    if (str != null) {
                        model_name = context.getString(R.string.model_name) + str;
                    } else {
                        model_name = context.getString(R.string.no_model);
                    }
                } else {
                    Log.e(TAG, "Get date from MODEL_URI, Cursor is invalid");
                    return;
                }
            }
            status.setLeftText(0, model_name);
        }
    }

    public static void showFmodeState(Context context, long model_id, UARTController controller, StatusbarView status) {
        if (controller != null) {
            int state = -1;
            if (model_id != -2) {
                if (getCurrentMode() != isFPVModel(context, model_id)) {
                    status.setLeftTextVisibility(1, false);
                } else {
                    int fModeKey = DataProviderHelper.getFmodeKeyFromDatabase(context, model_id);
                    if (fModeKey != -1) {
                        state = controller.querySwitchState(fModeKey + 1);
                    } else {
                        Log.e(TAG, "F-Mode key is -1");
                    }
                }
            }
            if (state != -1) {
                status.setLeftText(1, context.getString(R.string.fmode_state, new Object[]{Integer.valueOf(state)}));
                return;
            }
            status.setLeftTextVisibility(1, false);
        }
    }

    public static float normalizeDegree(float degree) {
        return (720.0f + degree) % 360.0f;
    }

    public static float getRelativeDegree(float degree1, float degree2) {
        return normalizeDegree(degree1 - degree2);
    }

    private static ArrayList<Integer> initThrCurve() {
        ArrayList<Integer> curveList = new ArrayList();
        curveList.add(0, Integer.valueOf(250));
        curveList.add(1, Integer.valueOf(313));
        curveList.add(2, Integer.valueOf(375));
        curveList.add(3, Integer.valueOf(438));
        curveList.add(4, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
        curveList.add(5, Integer.valueOf(563));
        curveList.add(6, Integer.valueOf(625));
        curveList.add(7, Integer.valueOf(688));
        curveList.add(8, Integer.valueOf(750));
        curveList.add(9, Integer.valueOf(913));
        curveList.add(10, Integer.valueOf(875));
        curveList.add(11, Integer.valueOf(938));
        curveList.add(12, Integer.valueOf(1000));
        curveList.add(13, Integer.valueOf(1063));
        curveList.add(14, Integer.valueOf(1125));
        curveList.add(15, Integer.valueOf(1187));
        curveList.add(16, Integer.valueOf(1250));
        return curveList;
    }

    private static ArrayList<Integer> initDRCurve(int expo) {
        ArrayList<Integer> curveList = new ArrayList();
        if (expo == 30) {
            curveList.add(0, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(1, Integer.valueOf(721));
            curveList.add(2, Integer.valueOf(923));
            curveList.add(3, Integer.valueOf(1086));
            curveList.add(4, Integer.valueOf(1230));
            curveList.add(5, Integer.valueOf(1346));
            curveList.add(6, Integer.valueOf(1432));
            curveList.add(7, Integer.valueOf(1480));
            curveList.add(8, Integer.valueOf(1500));
            curveList.add(9, Integer.valueOf(1519));
            curveList.add(10, Integer.valueOf(1567));
            curveList.add(11, Integer.valueOf(1653));
            curveList.add(12, Integer.valueOf(1769));
            curveList.add(13, Integer.valueOf(1913));
            curveList.add(14, Integer.valueOf(2076));
            curveList.add(15, Integer.valueOf(2278));
            curveList.add(16, Integer.valueOf(2500));
        } else if (expo == 1) {
            curveList.add(0, Integer.valueOf(2500));
            curveList.add(1, Integer.valueOf(2500));
            curveList.add(2, Integer.valueOf(2500));
            curveList.add(3, Integer.valueOf(2500));
            curveList.add(4, Integer.valueOf(2500));
            curveList.add(5, Integer.valueOf(2500));
            curveList.add(6, Integer.valueOf(2500));
            curveList.add(7, Integer.valueOf(2500));
            curveList.add(8, Integer.valueOf(1500));
            curveList.add(9, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(10, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(11, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(12, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(13, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(14, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(15, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(16, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
        } else {
            curveList.add(0, Integer.valueOf(DataProviderHelper.MODEL_TYPE_LAST));
            curveList.add(1, Integer.valueOf(625));
            curveList.add(2, Integer.valueOf(750));
            curveList.add(3, Integer.valueOf(875));
            curveList.add(4, Integer.valueOf(1000));
            curveList.add(5, Integer.valueOf(1125));
            curveList.add(6, Integer.valueOf(1250));
            curveList.add(7, Integer.valueOf(1375));
            curveList.add(8, Integer.valueOf(1500));
            curveList.add(9, Integer.valueOf(1625));
            curveList.add(10, Integer.valueOf(1750));
            curveList.add(11, Integer.valueOf(1875));
            curveList.add(12, Integer.valueOf(2000));
            curveList.add(13, Integer.valueOf(2125));
            curveList.add(14, Integer.valueOf(2250));
            curveList.add(15, Integer.valueOf(2375));
            curveList.add(16, Integer.valueOf(2500));
        }
        return curveList;
    }

    public static boolean getFmodeChannelValues(Context context) {
        String[] fmode_values;
        int fmode_opt = context.getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getInt("fmode_option", 1);
        if (fmode_opt == 0) {
            fmode_values = context.getResources().getStringArray(R.array.fmode_opt_0);
        } else if (fmode_opt == 1) {
            fmode_values = context.getResources().getStringArray(R.array.fmode_opt_1);
        } else {
            fmode_values = context.getResources().getStringArray(R.array.fmode_opt_2);
        }
        if (fmode_values.length != 3) {
            Log.e(TAG, "Invalid fmode option! length is:" + fmode_values.length);
            return false;
        }
        int i = 0;
        while (i < fmode_values.length) {
            String[] temp = fmode_values[i].split(",");
            Log.d(TAG, "getFmodeChannelValues---fmode_values:" + fmode_values[i]);
            Log.d(TAG, "getFmodeChannelValues---temp[0]:" + temp[0] + ";temp[1]:" + temp[1]);
            if (temp.length == 2) {
                value_ch5[i] = Integer.parseInt(temp[0]);
                value_ch6[i] = Integer.parseInt(temp[1]);
                i++;
            } else {
                Log.e(TAG, "fmode values format error");
                return false;
            }
        }
        return true;
    }

    static ArrayList<MixedData> V18CameraMixMode() {
        ArrayList<MixedData> datasListTmp = new ArrayList();
        MixedData data1 = new MixedData();
        data1.mFmode = 0;
        data1.mChannel = 1;
        data1.mhardware = HW_J_BASE + 3;
        data1.mHardwareType = 1;
        data1.mPriority = 1;
        data1.mCurvePoint = initDRCurve(0);
        data1.mSpeed = 10;
        data1.mReverse = false;
        datasListTmp.add(data1);
        MixedData data2 = new MixedData();
        data2.mFmode = 0;
        data2.mChannel = 2;
        data2.mhardware = HW_J_BASE + 4;
        data2.mHardwareType = 1;
        data2.mPriority = 1;
        data2.mCurvePoint = initDRCurve(0);
        data2.mSpeed = 10;
        data2.mReverse = false;
        datasListTmp.add(data2);
        MixedData data3 = new MixedData();
        data3.mFmode = 0;
        data3.mChannel = 5;
        data3.mhardware = HW_S_BASE + 1;
        data3.mHardwareType = 2;
        data3.mPriority = 1;
        data3.mMixedType = 3;
        data3.mSwitchStatus.add(0, Boolean.valueOf(true));
        data3.mSwitchStatus.add(1, Boolean.valueOf(true));
        data3.mSwitchValue.add(0, Integer.valueOf(100));
        data3.mSwitchValue.add(1, Integer.valueOf(100));
        data3.mSpeed = 10;
        data3.mReverse = false;
        datasListTmp.add(data3);
        MixedData data4 = new MixedData();
        data4.mFmode = 0;
        data4.mChannel = 6;
        data4.mhardware = HW_S_BASE + 2;
        data4.mHardwareType = 2;
        data4.mPriority = 1;
        data4.mMixedType = 3;
        data4.mSwitchStatus.add(0, Boolean.valueOf(true));
        data4.mSwitchStatus.add(1, Boolean.valueOf(true));
        data4.mSwitchStatus.add(2, Boolean.valueOf(true));
        data4.mSwitchValue.add(0, Integer.valueOf(-100));
        data4.mSwitchValue.add(1, Integer.valueOf(0));
        data4.mSwitchValue.add(2, Integer.valueOf(100));
        data4.mSpeed = 10;
        data4.mReverse = false;
        datasListTmp.add(data4);
        return datasListTmp;
    }

    static ArrayList<MixedData> GB603CameraMixMode() {
        ArrayList<MixedData> datasListTmp = new ArrayList();
        MixedData data1 = new MixedData();
        data1.mFmode = 0;
        data1.mChannel = 1;
        data1.mhardware = HW_J_BASE + 3;
        data1.mHardwareType = 1;
        data1.mPriority = 1;
        data1.mCurvePoint = initDRCurve(0);
        data1.mSpeed = 10;
        data1.mReverse = false;
        datasListTmp.add(data1);
        MixedData data2 = new MixedData();
        data2.mFmode = 0;
        data2.mChannel = 2;
        data2.mhardware = HW_J_BASE + 4;
        data2.mHardwareType = 1;
        data2.mPriority = 1;
        data2.mCurvePoint = initDRCurve(0);
        data2.mSpeed = 10;
        data2.mReverse = false;
        datasListTmp.add(data2);
        MixedData data3 = new MixedData();
        data3.mFmode = 0;
        data3.mChannel = 5;
        data3.mhardware = HW_S_BASE + 1;
        data3.mHardwareType = 2;
        data3.mPriority = 1;
        data3.mMixedType = 3;
        data3.mSwitchStatus.add(0, Boolean.valueOf(true));
        data3.mSwitchStatus.add(1, Boolean.valueOf(true));
        data3.mSwitchValue.add(0, Integer.valueOf(100));
        data3.mSwitchValue.add(1, Integer.valueOf(0));
        data3.mSpeed = 10;
        data3.mReverse = false;
        datasListTmp.add(data3);
        MixedData data4 = new MixedData();
        data4.mFmode = 0;
        data4.mChannel = 6;
        data4.mhardware = HW_S_BASE + 2;
        data4.mHardwareType = 2;
        data4.mPriority = 1;
        data4.mMixedType = 3;
        data4.mSwitchStatus.add(0, Boolean.valueOf(true));
        data4.mSwitchStatus.add(1, Boolean.valueOf(true));
        data4.mSwitchStatus.add(2, Boolean.valueOf(true));
        data4.mSwitchValue.add(0, Integer.valueOf(-100));
        data4.mSwitchValue.add(1, Integer.valueOf(0));
        data4.mSwitchValue.add(2, Integer.valueOf(100));
        data4.mSpeed = 10;
        data4.mReverse = false;
        datasListTmp.add(data4);
        MixedData data5 = new MixedData();
        data5.mFmode = 0;
        data5.mChannel = 7;
        data5.mhardware = HW_K_BASE + 1;
        data5.mHardwareType = 1;
        data5.mPriority = 1;
        data5.mCurvePoint = initDRCurve(0);
        data5.mSpeed = 10;
        data5.mReverse = false;
        datasListTmp.add(data5);
        return datasListTmp;
    }

    static ArrayList<MixedData> GimbalMixMode(int mode) {
        ArrayList<MixedData> datasListTmp = new ArrayList();
        MixedData data1 = new MixedData();
        data1.mFmode = 0;
        data1.mChannel = 6;
        data1.mhardware = HW_K_BASE + 1;
        data1.mHardwareType = 1;
        data1.mPriority = 1;
        data1.mCurvePoint = initDRCurve(0);
        data1.mSpeed = 10;
        data1.mReverse = false;
        datasListTmp.add(data1);
        MixedData data2 = new MixedData();
        data2.mFmode = 0;
        data2.mChannel = 7;
        switch (mode) {
            case 1:
            case 3:
                data2.mhardware = HW_J_BASE + 1;
                break;
            case 2:
            case 4:
                data2.mhardware = HW_J_BASE + 3;
                break;
        }
        data2.mHardwareType = 1;
        data2.mPriority = 1;
        data2.mCurvePoint = initDRCurve(0);
        data2.mSpeed = 10;
        data2.mReverse = true;
        datasListTmp.add(data2);
        MixedData data3 = new MixedData();
        data3.mFmode = 0;
        data3.mChannel = 8;
        switch (mode) {
            case 1:
            case 2:
                data3.mhardware = HW_J_BASE + 4;
                break;
            case 3:
            case 4:
                data3.mhardware = HW_J_BASE + 2;
                break;
        }
        data3.mHardwareType = 1;
        data3.mPriority = 1;
        data3.mCurvePoint = initDRCurve(0);
        data3.mSpeed = 10;
        data3.mReverse = false;
        datasListTmp.add(data3);
        MixedData data4 = new MixedData();
        data4.mFmode = 0;
        data4.mChannel = 9;
        data4.mhardware = HW_S_BASE + 1;
        data4.mHardwareType = 2;
        data4.mPriority = 1;
        data4.mMixedType = 3;
        data4.mSwitchStatus.add(0, Boolean.valueOf(true));
        data4.mSwitchStatus.add(1, Boolean.valueOf(true));
        data4.mSwitchStatus.add(2, Boolean.valueOf(true));
        data4.mSwitchValue.add(0, Integer.valueOf(99));
        data4.mSwitchValue.add(1, Integer.valueOf(11));
        data4.mSwitchValue.add(2, Integer.valueOf(11));
        data4.mSpeed = 10;
        data4.mReverse = false;
        datasListTmp.add(data4);
        MixedData data5 = new MixedData();
        data5.mFmode = 0;
        data5.mChannel = 10;
        data5.mhardware = HW_S_BASE + 2;
        data5.mHardwareType = 2;
        data5.mPriority = 1;
        data5.mMixedType = 3;
        data5.mSwitchStatus.add(0, Boolean.valueOf(true));
        data5.mSwitchStatus.add(1, Boolean.valueOf(true));
        data5.mSwitchStatus.add(2, Boolean.valueOf(true));
        data5.mSwitchValue.add(0, Integer.valueOf(-106));
        data5.mSwitchValue.add(1, Integer.valueOf(-47));
        data5.mSwitchValue.add(2, Integer.valueOf(99));
        data5.mSpeed = 10;
        data5.mReverse = false;
        datasListTmp.add(data5);
        return datasListTmp;
    }

    static ArrayList<MixedData> H920MixMode(int mode) {
        ArrayList<MixedData> datasListTmp = new ArrayList();
        MixedData data1 = new MixedData();
        data1.mFmode = 0;
        data1.mChannel = 1;
        switch (mode) {
            case 1:
            case 3:
                data1.mhardware = HW_J_BASE + 3;
                break;
            case 2:
            case 4:
                data1.mhardware = HW_J_BASE + 1;
                break;
        }
        data1.mHardwareType = 1;
        data1.mPriority = 1;
        data1.mCurvePoint = initThrCurve();
        data1.mSpeed = 10;
        data1.mReverse = false;
        datasListTmp.add(data1);
        MixedData data2 = new MixedData();
        data2.mFmode = 0;
        data2.mChannel = 1;
        data2.mhardware = HW_B_BASE + 3;
        data2.mHardwareType = 2;
        data2.mPriority = 2;
        data2.mMixedType = 3;
        data2.mSwitchStatus.add(0, Boolean.valueOf(true));
        data2.mSwitchStatus.add(1, Boolean.valueOf(false));
        data2.mSwitchStatus.add(2, Boolean.valueOf(true));
        data2.mSwitchValue.add(0, Integer.valueOf(-50));
        data2.mSwitchValue.add(1, Integer.valueOf(0));
        data2.mSwitchValue.add(2, Integer.valueOf(-50));
        data2.mSpeed = 10;
        data2.mReverse = false;
        datasListTmp.add(data2);
        MixedData data3 = new MixedData();
        data3.mFmode = 0;
        data3.mChannel = 2;
        switch (mode) {
            case 1:
            case 2:
                data3.mhardware = HW_J_BASE + 4;
                break;
            case 3:
            case 4:
                data3.mhardware = HW_J_BASE + 2;
                break;
        }
        data3.mHardwareType = 1;
        data3.mPriority = 1;
        data3.mCurvePoint = initDRCurve(0);
        data3.mSpeed = 10;
        data3.mReverse = false;
        datasListTmp.add(data3);
        MixedData data4 = new MixedData();
        data4.mFmode = 0;
        data4.mChannel = 3;
        switch (mode) {
            case 1:
            case 3:
                data4.mhardware = HW_J_BASE + 1;
                break;
            case 2:
            case 4:
                data4.mhardware = HW_J_BASE + 3;
                break;
        }
        data4.mHardwareType = 1;
        data4.mPriority = 1;
        data4.mCurvePoint = initDRCurve(0);
        data4.mSpeed = 10;
        data4.mReverse = false;
        datasListTmp.add(data4);
        MixedData data5 = new MixedData();
        data5.mFmode = 0;
        data5.mChannel = 4;
        switch (mode) {
            case 1:
            case 2:
                data5.mhardware = HW_J_BASE + 2;
                break;
            case 3:
            case 4:
                data5.mhardware = HW_J_BASE + 4;
                break;
        }
        data5.mHardwareType = 1;
        data5.mPriority = 1;
        data5.mCurvePoint = initDRCurve(30);
        data5.mSpeed = 10;
        data5.mReverse = false;
        datasListTmp.add(data5);
        MixedData data6 = new MixedData();
        data6.mFmode = 0;
        data6.mChannel = 5;
        data6.mhardware = HW_S_BASE + 2;
        data6.mHardwareType = 2;
        data6.mPriority = 1;
        data6.mMixedType = 3;
        data6.mSwitchStatus.add(0, Boolean.valueOf(true));
        data6.mSwitchStatus.add(1, Boolean.valueOf(true));
        data6.mSwitchStatus.add(2, Boolean.valueOf(false));
        data6.mSwitchValue.add(0, Integer.valueOf(100));
        data6.mSwitchValue.add(1, Integer.valueOf(0));
        data6.mSwitchValue.add(2, Integer.valueOf(0));
        data6.mSpeed = 10;
        data6.mReverse = false;
        datasListTmp.add(data6);
        MixedData data7 = new MixedData();
        data7.mFmode = 0;
        data7.mChannel = 6;
        data7.mhardware = HW_S_BASE + 2;
        data7.mHardwareType = 2;
        data7.mPriority = 2;
        data7.mMixedType = 3;
        data7.mSwitchStatus.add(0, Boolean.valueOf(false));
        data7.mSwitchStatus.add(1, Boolean.valueOf(false));
        data7.mSwitchStatus.add(2, Boolean.valueOf(true));
        data7.mSwitchValue.add(0, Integer.valueOf(0));
        data7.mSwitchValue.add(1, Integer.valueOf(0));
        data7.mSwitchValue.add(2, Integer.valueOf(OFFSET_SWITCH_MAX_2));
        data7.mSpeed = 10;
        data7.mReverse = false;
        datasListTmp.add(data7);
        MixedData data8 = new MixedData();
        data8.mFmode = 0;
        data8.mChannel = 6;
        data8.mhardware = HW_K_BASE + 3;
        data8.mHardwareType = 1;
        data8.mCurvePoint = initDRCurve(1);
        data8.mPriority = 3;
        data8.mSpeed = 10;
        data8.mReverse = false;
        datasListTmp.add(data8);
        MixedData data9 = new MixedData();
        data9.mFmode = 0;
        data9.mChannel = 7;
        data9.mhardware = HW_J_BASE;
        data9.mHardwareType = 1;
        data9.mPriority = 1;
        data9.mCurvePoint = initDRCurve(0);
        data9.mSpeed = 10;
        data9.mReverse = false;
        datasListTmp.add(data9);
        MixedData data10 = new MixedData();
        data10.mFmode = 0;
        data10.mChannel = 8;
        data10.mhardware = HW_J_BASE;
        data10.mHardwareType = 1;
        data10.mPriority = 1;
        data10.mCurvePoint = initDRCurve(0);
        data10.mSpeed = 10;
        data10.mReverse = false;
        datasListTmp.add(data10);
        MixedData data11 = new MixedData();
        data11.mFmode = 0;
        data11.mChannel = 9;
        data11.mhardware = HW_J_BASE;
        data11.mHardwareType = 2;
        data11.mPriority = 1;
        data11.mMixedType = 3;
        data11.mSwitchStatus.add(0, Boolean.valueOf(true));
        data11.mSwitchStatus.add(1, Boolean.valueOf(false));
        data11.mSwitchValue.add(0, Integer.valueOf(0));
        data11.mSwitchValue.add(1, Integer.valueOf(0));
        data11.mSpeed = 10;
        data11.mReverse = false;
        datasListTmp.add(data11);
        MixedData data12 = new MixedData();
        data12.mFmode = 0;
        data12.mChannel = 10;
        data12.mhardware = HW_J_BASE;
        data12.mHardwareType = 2;
        data12.mPriority = 1;
        data12.mMixedType = 3;
        data12.mSwitchStatus.add(0, Boolean.valueOf(true));
        data12.mSwitchStatus.add(1, Boolean.valueOf(false));
        data12.mSwitchValue.add(0, Integer.valueOf(0));
        data12.mSwitchValue.add(1, Integer.valueOf(0));
        data12.mSpeed = 10;
        data12.mReverse = false;
        datasListTmp.add(data12);
        MixedData data13 = new MixedData();
        data13.mFmode = 0;
        data13.mChannel = 11;
        data13.mhardware = HW_S_BASE + 1;
        data13.mHardwareType = 2;
        data13.mPriority = 1;
        data13.mMixedType = 3;
        data13.mSwitchStatus.add(0, Boolean.valueOf(true));
        data13.mSwitchStatus.add(1, Boolean.valueOf(true));
        data13.mSwitchValue.add(0, Integer.valueOf(-100));
        data13.mSwitchValue.add(1, Integer.valueOf(100));
        data13.mSpeed = 10;
        data13.mReverse = false;
        datasListTmp.add(data13);
        return datasListTmp;
    }

    static ArrayList<MixedData> mControlMixMode_ST10(int mode) {
        ArrayList<MixedData> datasListTmp = new ArrayList();
        MixedData data1 = new MixedData();
        data1.mFmode = 0;
        data1.mChannel = 1;
        switch (mode) {
            case 1:
            case 3:
                data1.mhardware = HW_J_BASE + 3;
                break;
            case 2:
            case 4:
                data1.mhardware = HW_J_BASE + 1;
                break;
        }
        data1.mHardwareType = 1;
        data1.mPriority = 1;
        data1.mCurvePoint = initThrCurve();
        data1.mSpeed = 10;
        data1.mReverse = false;
        datasListTmp.add(data1);
        MixedData data2 = new MixedData();
        data2.mFmode = 0;
        data2.mChannel = 1;
        data2.mhardware = HW_B_BASE + 3;
        data2.mHardwareType = 2;
        data2.mPriority = 2;
        data2.mMixedType = 3;
        data2.mSwitchStatus.add(0, Boolean.valueOf(true));
        data2.mSwitchStatus.add(1, Boolean.valueOf(false));
        data2.mSwitchStatus.add(2, Boolean.valueOf(true));
        data2.mSwitchValue.add(0, Integer.valueOf(-50));
        data2.mSwitchValue.add(1, Integer.valueOf(0));
        data2.mSwitchValue.add(2, Integer.valueOf(-50));
        data2.mSpeed = 10;
        data2.mReverse = false;
        datasListTmp.add(data2);
        MixedData data3 = new MixedData();
        data3.mFmode = 0;
        data3.mChannel = 2;
        switch (mode) {
            case 1:
            case 2:
                data3.mhardware = HW_J_BASE + 4;
                break;
            case 3:
            case 4:
                data3.mhardware = HW_J_BASE + 2;
                break;
        }
        data3.mHardwareType = 1;
        data3.mPriority = 1;
        data3.mCurvePoint = initDRCurve(0);
        data3.mSpeed = 10;
        data3.mReverse = false;
        datasListTmp.add(data3);
        MixedData data4 = new MixedData();
        data4.mFmode = 0;
        data4.mChannel = 3;
        switch (mode) {
            case 1:
            case 3:
                data4.mhardware = HW_J_BASE + 1;
                break;
            case 2:
            case 4:
                data4.mhardware = HW_J_BASE + 3;
                break;
        }
        data4.mHardwareType = 1;
        data4.mPriority = 1;
        data4.mCurvePoint = initDRCurve(0);
        data4.mSpeed = 10;
        data4.mReverse = false;
        datasListTmp.add(data4);
        MixedData data5 = new MixedData();
        data5.mFmode = 0;
        data5.mChannel = 4;
        switch (mode) {
            case 1:
            case 2:
                data5.mhardware = HW_J_BASE + 2;
                break;
            case 3:
            case 4:
                data5.mhardware = HW_J_BASE + 4;
                break;
        }
        data5.mHardwareType = 1;
        data5.mPriority = 1;
        data5.mCurvePoint = initDRCurve(30);
        data5.mSpeed = 10;
        data5.mReverse = false;
        datasListTmp.add(data5);
        MixedData data6 = new MixedData();
        data6.mFmode = 0;
        data6.mChannel = 5;
        data6.mhardware = HW_S_BASE + 1;
        data6.mHardwareType = 2;
        data6.mPriority = 1;
        data6.mMixedType = 3;
        data6.mSwitchStatus.add(0, Boolean.valueOf(true));
        data6.mSwitchStatus.add(1, Boolean.valueOf(true));
        data6.mSwitchStatus.add(2, Boolean.valueOf(true));
        data6.mSwitchValue.add(0, Integer.valueOf(value_ch5[0]));
        data6.mSwitchValue.add(1, Integer.valueOf(value_ch5[1]));
        data6.mSwitchValue.add(2, Integer.valueOf(value_ch5[2]));
        data6.mSpeed = 10;
        data6.mReverse = false;
        datasListTmp.add(data6);
        MixedData data7 = new MixedData();
        data7.mFmode = 0;
        data7.mChannel = 6;
        data7.mhardware = HW_S_BASE + 1;
        data7.mHardwareType = 2;
        data7.mPriority = 1;
        data7.mMixedType = 3;
        data7.mSwitchStatus.add(0, Boolean.valueOf(true));
        data7.mSwitchStatus.add(1, Boolean.valueOf(true));
        data7.mSwitchStatus.add(2, Boolean.valueOf(true));
        data7.mSwitchValue.add(0, Integer.valueOf(value_ch6[0]));
        data7.mSwitchValue.add(1, Integer.valueOf(value_ch6[1]));
        data7.mSwitchValue.add(2, Integer.valueOf(value_ch6[2]));
        data7.mSpeed = 10;
        data7.mReverse = false;
        datasListTmp.add(data7);
        MixedData data8 = new MixedData();
        data8.mFmode = 0;
        data8.mChannel = 7;
        data8.mhardware = HW_K_BASE + 1;
        data8.mHardwareType = 1;
        data8.mPriority = 1;
        data8.mCurvePoint = initDRCurve(0);
        data8.mSpeed = 10;
        data8.mReverse = false;
        datasListTmp.add(data8);
        MixedData data9 = new MixedData();
        data9.mFmode = 0;
        data9.mChannel = 8;
        data9.mhardware = HW_K_BASE + 2;
        data9.mHardwareType = 1;
        data9.mPriority = 1;
        data9.mCurvePoint = initDRCurve(0);
        data9.mSpeed = 10;
        data9.mReverse = false;
        datasListTmp.add(data9);
        MixedData data10 = new MixedData();
        data10.mFmode = 0;
        data10.mChannel = 9;
        data10.mhardware = HW_J_BASE + 0;
        data10.mHardwareType = 2;
        data10.mPriority = 1;
        data10.mMixedType = 3;
        data10.mSwitchStatus.add(0, Boolean.valueOf(true));
        data10.mSwitchStatus.add(1, Boolean.valueOf(false));
        data10.mSwitchValue.add(0, Integer.valueOf(0));
        data10.mSwitchValue.add(1, Integer.valueOf(0));
        data10.mSpeed = 10;
        data10.mReverse = false;
        datasListTmp.add(data10);
        MixedData data11 = new MixedData();
        data11.mFmode = 0;
        data11.mChannel = 10;
        data11.mhardware = HW_J_BASE + 0;
        data11.mHardwareType = 2;
        data11.mPriority = 1;
        data11.mMixedType = 3;
        data11.mSwitchStatus.add(0, Boolean.valueOf(true));
        data11.mSwitchStatus.add(1, Boolean.valueOf(false));
        data11.mSwitchValue.add(0, Integer.valueOf(0));
        data11.mSwitchValue.add(1, Integer.valueOf(0));
        data11.mSpeed = 10;
        data11.mReverse = false;
        datasListTmp.add(data11);
        return datasListTmp;
    }

    static ArrayList<MixedData> mControlMixMode_ST12(int mode) {
        ArrayList<MixedData> datasListTmp = new ArrayList();
        MixedData data1 = new MixedData();
        data1.mFmode = 0;
        data1.mChannel = 1;
        switch (mode) {
            case 1:
            case 3:
                data1.mhardware = HW_J_BASE + 3;
                break;
            case 2:
            case 4:
                data1.mhardware = HW_J_BASE + 1;
                break;
        }
        data1.mHardwareType = 1;
        data1.mPriority = 1;
        data1.mCurvePoint = initThrCurve();
        data1.mSpeed = 10;
        data1.mReverse = false;
        datasListTmp.add(data1);
        MixedData data2 = new MixedData();
        data2.mFmode = 0;
        data2.mChannel = 1;
        data2.mhardware = HW_B_BASE + 3;
        data2.mHardwareType = 2;
        data2.mPriority = 2;
        data2.mMixedType = 3;
        data2.mSwitchStatus.add(0, Boolean.valueOf(true));
        data2.mSwitchStatus.add(1, Boolean.valueOf(false));
        data2.mSwitchStatus.add(2, Boolean.valueOf(true));
        data2.mSwitchValue.add(0, Integer.valueOf(-50));
        data2.mSwitchValue.add(1, Integer.valueOf(0));
        data2.mSwitchValue.add(2, Integer.valueOf(-50));
        data2.mSpeed = 10;
        data2.mReverse = false;
        datasListTmp.add(data2);
        MixedData data3 = new MixedData();
        data3.mFmode = 0;
        data3.mChannel = 2;
        switch (mode) {
            case 1:
            case 2:
                data3.mhardware = HW_J_BASE + 4;
                break;
            case 3:
            case 4:
                data3.mhardware = HW_J_BASE + 2;
                break;
        }
        data3.mHardwareType = 1;
        data3.mPriority = 1;
        data3.mCurvePoint = initDRCurve(0);
        data3.mSpeed = 10;
        data3.mReverse = false;
        datasListTmp.add(data3);
        MixedData data4 = new MixedData();
        data4.mFmode = 0;
        data4.mChannel = 3;
        switch (mode) {
            case 1:
            case 3:
                data4.mhardware = HW_J_BASE + 1;
                break;
            case 2:
            case 4:
                data4.mhardware = HW_J_BASE + 3;
                break;
        }
        data4.mHardwareType = 1;
        data4.mPriority = 1;
        data4.mCurvePoint = initDRCurve(0);
        data4.mSpeed = 10;
        data4.mReverse = false;
        datasListTmp.add(data4);
        MixedData data5 = new MixedData();
        data5.mFmode = 0;
        data5.mChannel = 4;
        switch (mode) {
            case 1:
            case 2:
                data5.mhardware = HW_J_BASE + 2;
                break;
            case 3:
            case 4:
                data5.mhardware = HW_J_BASE + 4;
                break;
        }
        data5.mHardwareType = 1;
        data5.mPriority = 1;
        data5.mCurvePoint = initDRCurve(30);
        data5.mSpeed = 10;
        data5.mReverse = false;
        datasListTmp.add(data5);
        MixedData data6 = new MixedData();
        data6.mFmode = 0;
        data6.mChannel = 5;
        data6.mhardware = HW_S_BASE + 2;
        data6.mHardwareType = 2;
        data6.mPriority = 1;
        data6.mMixedType = 3;
        data6.mSwitchStatus.add(0, Boolean.valueOf(true));
        data6.mSwitchStatus.add(1, Boolean.valueOf(true));
        data6.mSwitchStatus.add(2, Boolean.valueOf(true));
        data6.mSwitchValue.add(0, Integer.valueOf(value_ch5[0]));
        data6.mSwitchValue.add(1, Integer.valueOf(value_ch5[1]));
        data6.mSwitchValue.add(2, Integer.valueOf(value_ch5[2]));
        data6.mSpeed = 10;
        data6.mReverse = false;
        datasListTmp.add(data6);
        MixedData data7 = new MixedData();
        data7.mFmode = 0;
        data7.mChannel = 6;
        data7.mhardware = HW_S_BASE + 2;
        data7.mHardwareType = 2;
        data7.mPriority = 1;
        data7.mMixedType = 3;
        data7.mSwitchStatus.add(0, Boolean.valueOf(true));
        data7.mSwitchStatus.add(1, Boolean.valueOf(true));
        data7.mSwitchStatus.add(2, Boolean.valueOf(true));
        data7.mSwitchValue.add(0, Integer.valueOf(value_ch6[0]));
        data7.mSwitchValue.add(1, Integer.valueOf(value_ch6[1]));
        data7.mSwitchValue.add(2, Integer.valueOf(value_ch6[2]));
        data7.mSpeed = 10;
        data7.mReverse = false;
        datasListTmp.add(data7);
        MixedData data8 = new MixedData();
        data8.mFmode = 0;
        data8.mChannel = 7;
        data8.mhardware = HW_K_BASE + 1;
        data8.mHardwareType = 1;
        data8.mPriority = 1;
        data8.mCurvePoint = initDRCurve(0);
        data8.mSpeed = 10;
        data8.mReverse = false;
        datasListTmp.add(data8);
        MixedData data9 = new MixedData();
        data9.mFmode = 0;
        data9.mChannel = 8;
        data9.mhardware = HW_K_BASE + 3;
        data9.mHardwareType = 1;
        data9.mPriority = 1;
        data9.mCurvePoint = initDRCurve(0);
        data9.mSpeed = 10;
        data9.mReverse = true;
        datasListTmp.add(data9);
        MixedData data10 = new MixedData();
        data10.mFmode = 0;
        data10.mChannel = 9;
        data10.mhardware = HW_VB_BASE + 9;
        data10.mHardwareType = 2;
        data10.mPriority = 1;
        data10.mMixedType = 3;
        data10.mSwitchStatus.add(0, Boolean.valueOf(true));
        data10.mSwitchStatus.add(1, Boolean.valueOf(true));
        data10.mSwitchValue.add(0, Integer.valueOf(10));
        data10.mSwitchValue.add(1, Integer.valueOf(10));
        data10.mSpeed = 10;
        data10.mReverse = false;
        datasListTmp.add(data10);
        MixedData data11 = new MixedData();
        data11.mFmode = 0;
        data11.mChannel = 10;
        data11.mhardware = HW_J_BASE;
        data11.mHardwareType = 2;
        data11.mPriority = 1;
        data11.mMixedType = 3;
        data11.mSwitchStatus.add(0, Boolean.valueOf(true));
        data11.mSwitchStatus.add(1, Boolean.valueOf(false));
        data11.mSwitchValue.add(0, Integer.valueOf(0));
        data11.mSwitchValue.add(1, Integer.valueOf(0));
        data11.mSpeed = 10;
        data11.mReverse = false;
        datasListTmp.add(data11);
        MixedData data12 = new MixedData();
        data12.mFmode = 0;
        data12.mChannel = 11;
        data12.mhardware = HW_S_BASE + 1;
        data12.mHardwareType = 2;
        data12.mPriority = 1;
        data12.mMixedType = 3;
        data12.mSwitchStatus.add(0, Boolean.valueOf(true));
        data12.mSwitchStatus.add(1, Boolean.valueOf(true));
        data12.mSwitchValue.add(0, Integer.valueOf(-100));
        data12.mSwitchValue.add(1, Integer.valueOf(100));
        data12.mSpeed = 10;
        data12.mReverse = false;
        datasListTmp.add(data12);
        return datasListTmp;
    }

    public static boolean sendDataEachChannelToFlightControl(Context context, UARTController controller) {
        boolean result = true;
        ArrayList<MixedData> datasList = new ArrayList();
        mPrefsUtil = context.getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        int mode = mPrefsUtil.getInt(FlightSettings.FLIGHT_SETTINGS_MODE, 2);
        long model_type = mPrefsUtil.getLong("current_model_type", -2);
        Log.e(TAG, "####model_type = " + model_type + "####");
        getFmodeChannelValues(context);
        if (model_type == 404) {
            datasList = H920MixMode(mode);
        } else if (model_type == 406) {
            datasList = GimbalMixMode(mode);
        } else if (PROJECT_TAG.equals("ST12")) {
            datasList = mControlMixMode_ST12(mode);
        } else if (PROJECT_TAG.equals(PROJECT_TAG)) {
            datasList = mControlMixMode_ST10(mode);
        }
        for (int i = 0; i < datasList.size(); i++) {
            if (!controller.syncMixingData(true, (MixedData) datasList.get(i), 0)) {
                result = false;
            }
        }
        return result;
    }

    public static boolean sendDataEachChannelToFlightControl(Context context, long model_id, UARTController controller, int fmode) {
        int i;
        boolean result = true;
        ChannelMap[] cm = DataProviderHelper.readChannelMapFromDatabase(context, model_id);
        ThrottleData thrData = DataProviderHelper.readThrDataFromDatabase(context, model_id, fmode);
        DRData[] drData = DataProviderHelper.readDRDataFromDatabase(context, model_id, fmode);
        ServoData[] servoDatas = DataProviderHelper.readServoDataFromDatabase(context, model_id, fmode);
        ArrayList<MixedData> datasList = new ArrayList();
        for (i = 0; i < 12; i++) {
            MixedData data = new MixedData();
            data.mFmode = fmode;
            data.mChannel = i + 1;
            data.mhardware = getHardwareIndexT(cm[i].hardware);
            data.mHardwareType = getHardwareType(cm[i].hardware);
            data.mPriority = 1;
            ServoData sd = ServoSetupFragment.getServoSetup(servoDatas, cm[i].function);
            if (sd != null) {
                data.mSpeed = sd.speed;
                data.mReverse = sd.reverse;
            }
            datasList.add(i, data);
        }
        datasList.remove(0);
        datasList.add(0, ThrottleCurveFragment.getThrottleData(context, model_id, thrData));
        if (!thrData.cut_sw.equals("INH")) {
            datasList.add(ThrottleCurveFragment.getThrCutData(thrData));
        }
        MixedData[] drDatas = DR_Fragment.getDRDatas(context, model_id, drData);
        for (i = 0; i < drDatas.length; i++) {
            datasList.remove(drDatas[i].mChannel - 1);
            datasList.add(drDatas[i].mChannel - 1, drDatas[i]);
        }
        for (i = 0; i < datasList.size(); i++) {
            if (!controller.syncMixingData(true, (MixedData) datasList.get(i), 0)) {
                result = false;
            }
        }
        return result;
    }

    public static float getChannelValue(Channel cmsg, int hardwareIndex) {
        if (hardwareIndex == HW_J_BASE || hardwareIndex == HW_K_BASE || hardwareIndex == HW_T_BASE || hardwareIndex == HW_S_BASE || hardwareIndex == HW_B_BASE || hardwareIndex == HW_VS_BASE || hardwareIndex == HW_VB_BASE) {
            return 0.0f;
        }
        return ((Float) cmsg.channels.get(HW_CHANNEL_INDEX.get(hardwareIndex))).floatValue();
    }

    private static void initHardwareChannelIndex() {
        HW_CHANNEL_INDEX.clear();
        HW_CHANNEL_INDEX.put(HW_J_BASE + 1, 0);
        HW_CHANNEL_INDEX.put(HW_J_BASE + 2, 1);
        HW_CHANNEL_INDEX.put(HW_J_BASE + 3, 2);
        HW_CHANNEL_INDEX.put(HW_J_BASE + 4, 3);
        HW_CHANNEL_INDEX.put(HW_K_BASE + 1, 4);
        HW_CHANNEL_INDEX.put(HW_K_BASE + 2, 5);
        if (PROJECT_TAG.equals("ST12")) {
            HW_CHANNEL_INDEX.put(HW_K_BASE + 3, 6);
            HW_CHANNEL_INDEX.put(HW_S_BASE + 1, 7);
            HW_CHANNEL_INDEX.put(HW_S_BASE + 2, 8);
            HW_CHANNEL_INDEX.put(HW_B_BASE + 1, 9);
            HW_CHANNEL_INDEX.put(HW_B_BASE + 2, 10);
            HW_CHANNEL_INDEX.put(HW_B_BASE + 3, 11);
        } else if (PROJECT_TAG.equals(PROJECT_TAG)) {
            HW_CHANNEL_INDEX.put(HW_S_BASE + 1, 6);
            HW_CHANNEL_INDEX.put(HW_B_BASE + 1, 7);
            HW_CHANNEL_INDEX.put(HW_B_BASE + 2, 8);
            HW_CHANNEL_INDEX.put(HW_B_BASE + 3, 9);
        }
    }

    private static void setIndexNumFunc() {
        if (PROJECT_TAG.equals("ST12")) {
            HW_J_BASE = 0;
            HW_K_BASE = 10;
            HW_T_BASE = 20;
            HW_S_BASE = 30;
            HW_B_BASE = 50;
            HW_VS_BASE = 70;
            HW_VB_BASE = 90;
        } else if (PROJECT_TAG.equals(PROJECT_TAG)) {
            HW_J_BASE = 0;
            HW_K_BASE = 10;
            HW_S_BASE = 20;
            HW_B_BASE = 30;
            HW_VB_BASE = 40;
            HW_VS_BASE = 70;
        }
        FMODE_KEY_INDEX = HW_B_BASE;
        BIND_KEY_INDEX = HW_B_BASE + 3;
        CAMERA_KEY_INDEX = HW_B_BASE + 1;
        VIDEO_KEY_INDEX = HW_B_BASE + 2;
        ZOOM_KEY_INDEX = HW_B_BASE;
        initHardwareChannelIndex();
    }

    public static boolean sendAllDataToFlightControl(Context context, long model_id, UARTController controller) {
        if (controller == null) {
            Log.e(TAG, "UartController is null");
            return false;
        }
        boolean success;
        ReceiverInfomation rec_info = getReceiverInfo(context, model_id);
        if (rec_info.channelNumber != 0) {
            controller.setChannelConfig(false, rec_info.analogChNumber, rec_info.switchChNumber);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }
        syncMixingDataDeleteAll(controller);
        if (PROJECT_TAG.equals(PROJECT_TAG) || PROJECT_TAG.equals("ST12")) {
            setIndexNumFunc();
            success = sendDataEachChannelToFlightControl(context, controller);
        } else {
            success = sendDataEachChannelToFlightControl(context, model_id, controller, 0);
        }
        success = sendFmodeKey(context, model_id, controller);
        if (!success) {
            Log.e(TAG, "Failed to send F-Mode key to transmitter");
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e2) {
        }
        mSendDataCompleted = true;
        return success;
    }

    public static void syncMixingDataDeleteAll(UARTController controller) {
        if (controller == null) {
            return;
        }
        if (controller.syncMixingDataDeleteAll(true, 3)) {
            mSendDataCompleted = false;
        } else {
            Log.e(TAG, "Failed to connect transmitter");
        }
    }

    public static int getFmodeState() {
        return 0;
    }

    public static void readSDcard(Context context) {
        boolean hasSDCard;
        SharedPreferences prefs = context.getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        if (new File(SDCARD2_PATH).canWrite()) {
            hasSDCard = true;
        } else {
            hasSDCard = false;
        }
        Log.v(TAG, "-----has SDcard----" + hasSDCard);
        prefs.edit().putBoolean("has_sdcard_value", hasSDCard).commit();
    }

    public static boolean hasSDCard(Context context) {
        return context.getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getBoolean("has_sdcard_value", false);
    }

    public static String getExterPath() {
        return SDCARD2_PATH;
    }

    public static void saveWifiInfoToDatabase(Context context, long model_id, String wifi_info) {
        if (wifi_info != null && model_id != -2) {
            Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id);
            ContentValues cv = new ContentValues();
            cv.put(DBOpenHelper.KEY_CONNECT_WIFI_INFO, wifi_info);
            context.getContentResolver().update(uri, cv, null, null);
        }
    }

    public static String getModelWifiInfoToDatabase(Context context, long model_id) {
        if (model_id == -2) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id);
        Cursor c = context.getContentResolver().query(uri, new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_CONNECT_WIFI_INFO}, null, null, null);
        if (DataProviderHelper.isCursorValid(c)) {
            String last_connect = c.getString(c.getColumnIndex(DBOpenHelper.KEY_CONNECT_WIFI_INFO));
            c.close();
            return last_connect;
        }
        Log.e(TAG, "Read curve params of analog, Cursor is invalid");
        return null;
    }

    public static void setWIFIConnectFlag(boolean isconnected) {
        isWIFIConnected = isconnected;
    }

    public static void connectModelWifi(Context context, long model_id) {
        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        BindWifiManage bwm = new BindWifiManage(wifiManager);
        WifiConnect wc = new WifiConnect(wifiManager);
        if (!bwm.getWifiStatus()) {
            bwm.openWifi();
        }
        String wifi_info = getModelWifiInfoToDatabase(context, model_id);
        if (wifi_info != null) {
            String[] str = wifi_info.split(",");
            if (str.length != 3) {
                return;
            }
            if (!str[0].equals(bwm.getSSID())) {
                wifiManager.disableNetwork(bwm.getCurrentNetId());
                wc.Connect(str[0], str[1], Integer.parseInt(str[2]));
            } else if (str[0].equals(bwm.getSSID())) {
                isWIFIConnected = true;
            }
        }
    }

    public static boolean isValidWifi(Context context, long model_id) {
        BindWifiManage bwm = new BindWifiManage((WifiManager) context.getSystemService("wifi"));
        String wifi_info = getModelWifiInfoToDatabase(context, model_id);
        if (wifi_info == null) {
            return false;
        }
        String[] str = wifi_info.split(",");
        if (str.length == 3) {
            return str[0].equals(bwm.getSSID());
        }
        return false;
    }
}
