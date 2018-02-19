package com.yuneec.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import com.yuneec.channelsettings.DRData;
import com.yuneec.channelsettings.ServoData;
import com.yuneec.channelsettings.ThrottleData;
import com.yuneec.flight_settings.ChannelMap;
import com.yuneec.flightmode15.R;
import java.util.ArrayList;

public class DataProviderHelper {
    public static final int MODEL_AIRPLANE = 1;
    public static final int MODEL_GLIDER = 3;
    public static final int MODEL_HELICOPTER = 2;
    public static final int MODEL_MULTICOPTER = 4;
    public static final int MODEL_TYPE_AIRPLANE_BASE = 100;
    public static final int MODEL_TYPE_GLIDER_BASE = 300;
    public static final int MODEL_TYPE_HELICOPTER_BASE = 200;
    public static final int MODEL_TYPE_LAST = 500;
    public static final int MODEL_TYPE_MULITCOPTER_BASE = 400;
    private static final String TAG = "DataProviderHelper";

    public static String getFuncNameByHw(Context context, long model_id, String hardware) {
        Cursor c = context.getContentResolver().query(ContentUris.withAppendedId(DataProvider.CHMAP_URI, model_id), new String[]{"function"}, "hardware='" + hardware + "'", null, null);
        if (!isCursorValid(c)) {
            return null;
        }
        String func = c.getString(c.getColumnIndex("function"));
        c.close();
        return func;
    }

    public static ArrayList<ModelBrief> getModelsByType(Context context, int type, int isFPV) {
        int start;
        int end;
        ContentResolver cr = context.getContentResolver();
        switch (type) {
            case 1:
                start = 100;
                end = 200;
                break;
            case 2:
                start = 200;
                end = MODEL_TYPE_GLIDER_BASE;
                break;
            case 3:
                start = MODEL_TYPE_GLIDER_BASE;
                end = MODEL_TYPE_MULITCOPTER_BASE;
                break;
            case 4:
                start = MODEL_TYPE_MULITCOPTER_BASE;
                end = MODEL_TYPE_LAST;
                break;
            default:
                Log.e(TAG, "Unknown Type");
                return null;
        }
        Cursor c = cr.query(DataProvider.MODEL_URI, new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_NAME, DBOpenHelper.KEY_FPV, DBOpenHelper.KEY_ICON, "type", DBOpenHelper.KEY_F_MODE_KEY, DBOpenHelper.KEY_ANALOG_MIN, DBOpenHelper.KEY_SWITCH_MIN}, "type>" + start + " AND " + "type" + "<" + end + " AND " + DBOpenHelper.KEY_FPV + "=" + isFPV, null, null);
        if (c != null && c.moveToFirst()) {
            ArrayList<ModelBrief> list = new ArrayList();
            do {
                ModelBrief m = new ModelBrief();
                m._id = c.getLong(c.getColumnIndex(DBOpenHelper.KEY_ID));
                m.name = c.getString(c.getColumnIndex(DBOpenHelper.KEY_NAME));
                m.iconResourceId = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_ICON));
                m.type = c.getInt(c.getColumnIndex("type"));
                m.fpv = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_FPV));
                m.f_mode_key = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_F_MODE_KEY));
                m.analog_min = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_ANALOG_MIN));
                m.switch_min = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_SWITCH_MIN));
                list.add(m);
            } while (c.moveToNext());
            c.close();
            return list;
        } else if (c == null) {
            return null;
        } else {
            c.close();
            return null;
        }
    }

    public static void resetMixingChannel(Context context, long model_id) {
        Uri uri = DataProvider.PARAMS_URI.buildUpon().appendQueryParameter(DataProvider.EXTRA_PARAMS, DataProvider.PARAMS_RESET_MIXING).appendQueryParameter("model", String.valueOf(model_id)).build();
        long now = SystemClock.uptimeMillis();
        context.getContentResolver().delete(uri, null, null);
        Log.d(TAG, "reset mixing channel use:" + (SystemClock.uptimeMillis() - now));
    }

    public static void setChannelAlias(Context context, long model_id, int channel, String alias) {
        Uri uri = ContentUris.withAppendedId(DataProvider.CHMAP_URI, model_id).buildUpon().appendQueryParameter(DataProvider.PARAMS_MASTER_CHANNEL, String.valueOf(channel)).build();
        ContentValues cv = new ContentValues();
        cv.put(DBOpenHelper.KEY_ALIAS, alias);
        context.getContentResolver().update(uri, cv, null, null);
    }

    public static String getChannelAlias(Context context, long model_id, int channel) {
        Uri uri = ContentUris.withAppendedId(DataProvider.CHMAP_URI, model_id);
        Cursor c = context.getContentResolver().query(uri, new String[]{DBOpenHelper.KEY_ALIAS}, "channel=" + channel, null, null);
        if (!isCursorValid(c)) {
            return null;
        }
        String alias = c.getString(c.getColumnIndex(DBOpenHelper.KEY_ALIAS));
        c.close();
        return alias;
    }

    public static String[] getAllChannelsAlias(Context context, long model_id) {
        Uri uri = ContentUris.withAppendedId(DataProvider.CHMAP_URI, model_id);
        Cursor c = context.getContentResolver().query(uri, new String[]{DBOpenHelper.KEY_ALIAS}, null, null, null);
        String[] alias = null;
        if (isCursorValid(c)) {
            int i = 0;
            alias = new String[12];
            do {
                alias[i] = c.getString(c.getColumnIndex(DBOpenHelper.KEY_ALIAS));
                i++;
            } while (c.moveToNext());
            c.close();
        }
        return alias;
    }

    public static int getFmodeKeyFromDatabase(Context context, long model_id) {
        ContentResolver cr = context.getContentResolver();
        if (model_id == -2) {
            return -1;
        }
        Cursor cursor = cr.query(ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id), new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_F_MODE_KEY}, null, null, null);
        if (!isCursorValid(cursor)) {
            return -1;
        }
        int fmodeKey = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_F_MODE_KEY));
        cursor.close();
        return fmodeKey;
    }

    public static long getFmodeID(Context context, long model_id, int fmode) {
        Cursor c = context.getContentResolver().query(ContentUris.withAppendedId(DataProvider.FMODE_URI, model_id).buildUpon().appendQueryParameter(DataProvider.PARAMS_PARENT_ID, "true").appendQueryParameter(DataProvider.PARAMS_F_MODE, String.valueOf(fmode)).build(), new String[]{DBOpenHelper.KEY_ID}, null, null, null);
        if (isCursorValid(c)) {
            long id = c.getLong(c.getColumnIndex(DBOpenHelper.KEY_ID));
            c.close();
            return id;
        }
        Log.e(TAG, "getFmodeID, Cursor is invalid");
        return -1;
    }

    public static ChannelMap[] readChannelMapFromDatabase(Context context, long model_id) {
        ChannelMap[] cm = new ChannelMap[15];
        Cursor c = context.getContentResolver().query(ContentUris.withAppendedId(DataProvider.CHMAP_URI, model_id).buildUpon().appendQueryParameter(DataProvider.PARAMS_PARENT_ID, "true").build(), new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_CHANNEL, "function", "hardware", DBOpenHelper.KEY_ALIAS}, null, null, null);
        if (!isCursorValid(c)) {
            Log.e(TAG, "readChannelMapFromDatabase----Cursor is invalid");
            return null;
        } else if (c.getCount() != 12) {
            Log.e(TAG, "readChannelMapFromDatabase----Get unordered data");
            return null;
        } else {
            int index = 0;
            while (!c.isAfterLast()) {
                cm[index] = new ChannelMap();
                cm[index].id = c.getLong(c.getColumnIndex(DBOpenHelper.KEY_ID));
                cm[index].channel = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_CHANNEL));
                cm[index].function = c.getString(c.getColumnIndex("function"));
                cm[index].hardware = c.getString(c.getColumnIndex("hardware"));
                cm[index].alias = c.getString(c.getColumnIndex(DBOpenHelper.KEY_ALIAS));
                index++;
                c.moveToNext();
            }
            c.close();
            return cm;
        }
    }

    public static void writeChannelMapFromDatabase(Context context, ChannelMap[] cm) {
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        if (cm.length != 12) {
            Log.e(TAG, "writeChannelMapFromDatabase-----Get unordered data");
            return;
        }
        for (int i = 0; i < cm.length; i++) {
            if (cv.size() != 0) {
                cv.clear();
            }
            cv.put(DBOpenHelper.KEY_CHANNEL, Integer.valueOf(cm[i].channel));
            cv.put("function", cm[i].function);
            cv.put("hardware", cm[i].hardware);
            cv.put(DBOpenHelper.KEY_ALIAS, cm[i].alias);
            cr.update(ContentUris.withAppendedId(DataProvider.CHMAP_URI, cm[i].id), cv, null, null);
        }
    }

    public static ThrottleData readThrDataFromDatabase(Context context, long model_id, int fmode) {
        ThrottleData td = new ThrottleData();
        long pid = getFmodeID(context, model_id, fmode);
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContentUris.withAppendedId(DataProvider.THR_URI, pid).buildUpon().appendQueryParameter(DataProvider.PARAMS_PARENT_ID, "true").build(), new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_SW, DBOpenHelper.KEY_EXPO, DBOpenHelper.KEY_CUT_SW, DBOpenHelper.KEY_CUT_VALUE_1, DBOpenHelper.KEY_CUT_VALUE_2}, null, null, null);
        if (isCursorValid(c)) {
            td.id = c.getLong(c.getColumnIndex(DBOpenHelper.KEY_ID));
            td.sw = c.getString(c.getColumnIndex(DBOpenHelper.KEY_SW));
            td.expo = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_EXPO)) != 0;
            td.cut_sw = c.getString(c.getColumnIndex(DBOpenHelper.KEY_CUT_SW));
            td.cut_value1 = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_CUT_VALUE_1));
            td.cut_value2 = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_CUT_VALUE_2));
            c.close();
            getThrCurvePoints(cr, td);
            return td;
        }
        Log.e(TAG, "ReadThrDataFromDatabase, Cursor is invalid");
        return null;
    }

    private static void getThrCurvePoints(ContentResolver cr, ThrottleData td) {
        Cursor c = cr.query(ContentUris.withAppendedId(DataProvider.THR_CURVE_URI, td.id).buildUpon().appendQueryParameter(DataProvider.PARAMS_PARENT_ID, "true").build(), new String[]{DBOpenHelper.KEY_ID, "sw_state", DBOpenHelper.KEY_POT0, DBOpenHelper.KEY_POT1, DBOpenHelper.KEY_POT2, DBOpenHelper.KEY_POT3, DBOpenHelper.KEY_POT4}, null, null, null);
        if (isCursorValid(c)) {
            int index = 0;
            while (!c.isAfterLast() && index < c.getCount()) {
                index = c.getInt(c.getColumnIndex("sw_state"));
                if (index >= 0 && index < 3) {
                    td.thrCurve[index].id = c.getLong(c.getColumnIndex(DBOpenHelper.KEY_ID));
                    td.thrCurve[index].sw_state = index;
                    td.thrCurve[index].curvePoints[0] = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_POT0));
                    td.thrCurve[index].curvePoints[1] = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_POT1));
                    td.thrCurve[index].curvePoints[2] = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_POT2));
                    td.thrCurve[index].curvePoints[3] = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_POT3));
                    td.thrCurve[index].curvePoints[4] = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_POT4));
                    c.moveToNext();
                }
            }
            c.close();
            return;
        }
        Log.e(TAG, "getThrCurvePoints(), Cursor is invalid");
    }

    public static void writeThrDataToDatabase(Context context, ThrottleData td) {
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(DBOpenHelper.KEY_SW, td.sw);
        cv.put(DBOpenHelper.KEY_EXPO, Boolean.valueOf(td.expo));
        cv.put(DBOpenHelper.KEY_CUT_SW, td.cut_sw);
        cv.put(DBOpenHelper.KEY_CUT_VALUE_1, Integer.valueOf(td.cut_value1));
        cv.put(DBOpenHelper.KEY_CUT_VALUE_2, Integer.valueOf(td.cut_value2));
        cr.update(ContentUris.withAppendedId(DataProvider.THR_URI, td.id), cv, null, null);
        for (int state = 0; state < 3; state++) {
            cv.clear();
            cv.put("sw_state", Integer.valueOf(td.thrCurve[state].sw_state));
            cv.put(DBOpenHelper.KEY_POT0, Float.valueOf(td.thrCurve[state].curvePoints[0]));
            cv.put(DBOpenHelper.KEY_POT1, Float.valueOf(td.thrCurve[state].curvePoints[1]));
            cv.put(DBOpenHelper.KEY_POT2, Float.valueOf(td.thrCurve[state].curvePoints[2]));
            cv.put(DBOpenHelper.KEY_POT3, Float.valueOf(td.thrCurve[state].curvePoints[3]));
            cv.put(DBOpenHelper.KEY_POT4, Float.valueOf(td.thrCurve[state].curvePoints[4]));
            cr.update(ContentUris.withAppendedId(DataProvider.THR_CURVE_URI, td.thrCurve[state].id), cv, null, null);
        }
    }

    private static DRData readSingleInputDR(Context context, long pid, String input) {
        DRData data = new DRData();
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContentUris.withAppendedId(DataProvider.DR_URI, pid).buildUpon().appendQueryParameter(DataProvider.PARAMS_PARENT_ID, "true").appendQueryParameter("function", input).build(), new String[]{DBOpenHelper.KEY_ID, "function", DBOpenHelper.KEY_SW}, null, null, null);
        if (isCursorValid(c)) {
            data.id = c.getLong(c.getColumnIndex(DBOpenHelper.KEY_ID));
            data.func = c.getString(c.getColumnIndex("function"));
            data.sw = c.getString(c.getColumnIndex(DBOpenHelper.KEY_SW));
            c.close();
            c = cr.query(ContentUris.withAppendedId(DataProvider.DR_CURVE_URI, data.id).buildUpon().appendQueryParameter(DataProvider.PARAMS_PARENT_ID, "true").build(), new String[]{DBOpenHelper.KEY_ID, "sw_state", DBOpenHelper.KEY_RATE1, DBOpenHelper.KEY_RATE2, DBOpenHelper.KEY_EXPO1, DBOpenHelper.KEY_EXPO2, DBOpenHelper.KEY_OFFSET}, null, null, null);
            if (isCursorValid(c)) {
                int index = 0;
                while (!c.isAfterLast() && index < c.getCount()) {
                    data.curveparams[index].id = c.getLong(c.getColumnIndex(DBOpenHelper.KEY_ID));
                    data.curveparams[index].sw_state = c.getInt(c.getColumnIndex("sw_state"));
                    data.curveparams[index].rate1 = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_RATE1));
                    data.curveparams[index].rate2 = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_RATE2));
                    data.curveparams[index].expo1 = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_EXPO1));
                    data.curveparams[index].expo2 = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_EXPO2));
                    data.curveparams[index].offset = c.getFloat(c.getColumnIndex(DBOpenHelper.KEY_OFFSET));
                    index++;
                    c.moveToNext();
                }
                c.close();
                return data;
            }
            Log.e(TAG, "ReadThrCurveFromDatabase, Cursor is invalid");
            return null;
        }
        Log.e(TAG, "ReadThrDataFromDatabase, Cursor is invalid");
        return null;
    }

    public static DRData[] readDRDataFromDatabase(Context context, long model_id, int fmode) {
        dd = new DRData[3];
        long pid = getFmodeID(context, model_id, fmode);
        dd[0] = readSingleInputDR(context, pid, "ail");
        dd[1] = readSingleInputDR(context, pid, "ele");
        dd[2] = readSingleInputDR(context, pid, "rud");
        return dd;
    }

    public static void writeDRDataToDatabase(Context context, DRData[] dds) {
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < dds.length; i++) {
            cv.clear();
            cv.put("function", dds[i].func);
            cv.put(DBOpenHelper.KEY_SW, dds[i].sw);
            cr.update(ContentUris.withAppendedId(DataProvider.DR_URI, dds[i].id), cv, null, null);
            for (int state = 0; state < 3; state++) {
                cv.clear();
                cv.put("sw_state", Integer.valueOf(dds[i].curveparams[state].sw_state));
                cv.put(DBOpenHelper.KEY_RATE1, Float.valueOf(dds[i].curveparams[state].rate1));
                cv.put(DBOpenHelper.KEY_RATE2, Float.valueOf(dds[i].curveparams[state].rate2));
                cv.put(DBOpenHelper.KEY_EXPO1, Float.valueOf(dds[i].curveparams[state].expo1));
                cv.put(DBOpenHelper.KEY_EXPO2, Float.valueOf(dds[i].curveparams[state].expo2));
                cv.put(DBOpenHelper.KEY_OFFSET, Float.valueOf(dds[i].curveparams[state].offset));
                cr.update(ContentUris.withAppendedId(DataProvider.DR_CURVE_URI, dds[i].curveparams[state].id), cv, null, null);
            }
        }
    }

    private static ServoData readSingleServo(Context context, long pid, String func) {
        ServoData data = new ServoData();
        Cursor c = context.getContentResolver().query(ContentUris.withAppendedId(DataProvider.SERVO_URI, pid).buildUpon().appendQueryParameter(DataProvider.PARAMS_PARENT_ID, "true").appendQueryParameter("function", func).build(), new String[]{DBOpenHelper.KEY_ID, "function", DBOpenHelper.KEY_SUB_TRIM, DBOpenHelper.KEY_REVERSE, DBOpenHelper.KEY_SPEED, DBOpenHelper.KEY_TRAVEL_L, DBOpenHelper.KEY_TRAVEL_R}, null, null, null);
        if (isCursorValid(c)) {
            data.id = c.getLong(c.getColumnIndex(DBOpenHelper.KEY_ID));
            data.func = c.getString(c.getColumnIndex("function"));
            data.subTrim = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_SUB_TRIM));
            data.reverse = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_REVERSE)) != 0;
            data.speed = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_SPEED));
            data.travelL = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_TRAVEL_L));
            data.travelR = c.getInt(c.getColumnIndex(DBOpenHelper.KEY_TRAVEL_R));
            c.close();
            return data;
        }
        Log.e(TAG, "ReadThrDataFromDatabase, Cursor is invalid");
        return null;
    }

    public static ServoData[] readServoDataFromDatabase(Context context, long model_id, int fmode) {
        String[] func = context.getResources().getStringArray(R.array.servo_label_1);
        ServoData[] sd = new ServoData[func.length];
        long pid = getFmodeID(context, model_id, fmode);
        for (int index = 0; index < sd.length; index++) {
            sd[index] = readSingleServo(context, pid, func[index]);
        }
        return sd;
    }

    public static void writeServoDataToDatabase(Context context, ServoData[] sds) {
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        String[] funcs = context.getResources().getStringArray(R.array.servo_label_1);
        for (int i = 0; i < funcs.length; i++) {
            cv.clear();
            cv.put("function", sds[i].func);
            cv.put(DBOpenHelper.KEY_SUB_TRIM, Integer.valueOf(sds[i].subTrim));
            cv.put(DBOpenHelper.KEY_REVERSE, Integer.valueOf(sds[i].reverse ? 1 : 0));
            cv.put(DBOpenHelper.KEY_SPEED, Integer.valueOf(sds[i].speed));
            cv.put(DBOpenHelper.KEY_TRAVEL_L, Integer.valueOf(sds[i].travelL));
            cv.put(DBOpenHelper.KEY_TRAVEL_R, Integer.valueOf(sds[i].travelR));
            cr.update(ContentUris.withAppendedId(DataProvider.SERVO_URI, sds[i].id), cv, null, null);
        }
    }

    public static boolean isCursorValid(Cursor c) {
        if (c != null && c.moveToFirst()) {
            return true;
        }
        if (c != null) {
            c.close();
        }
        return false;
    }

    public static void changeMode(Context context, int mode) {
        context.getContentResolver().update(ContentUris.withAppendedId(DataProvider.CHMAP_URI, 0).buildUpon().appendQueryParameter(DataProvider.PARAMS_CHANGE_MODE, "true").appendQueryParameter(DataProvider.PARAMS_MODE, String.valueOf(mode)).build(), null, null, null);
    }
}
