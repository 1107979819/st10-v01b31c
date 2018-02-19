package com.yuneec.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {
    public static final String CHANNEL_MAP_TABLE_NAME = "channel_map";
    private static final String CREATE_CHANNEL_MAP_TABLE = "create table channel_map (_id integer primary key autoincrement,_pid integer, channel integer, function integer, hardware text, alias text, foreign key(_pid) references models(_id) on delete cascade )";
    private static final String CREATE_DR_CURVE_TABLE = "create table dr_curve ( _id integer primary key autoincrement, _pid integer, sw_state integer, rate1 float default -100, rate2 float default 100, expo1 float default 0, expo2 float default 0, offset float default 0, foreign key(_pid) references dr_data(_id) on delete cascade)";
    private static final String CREATE_DR_DATA_TABLE = "create table dr_data ( _id integer primary key autoincrement, _pid integer, function text not null, switch text default INH, foreign key(_pid) references f_mode(_id) on delete cascade)";
    private static final String CREATE_FLIGHTMODE_TABLE = "create table f_mode (_id integer primary key autoincrement,_pid integer, fmode_state integer, foreign key(_pid) references models(_id) on delete cascade )";
    private static final String CREATE_MODELS_TABLE = "create table models (_id integer primary key autoincrement, name text not null, icon text, type integer default 1 check (type>=100 AND type<=500), f_mode_key integer default -1, current_fmode integer check (current_fmode in (0,1,2)), fpv integer not null check (fpv in (0,1)), rx text, rx_analog_num integer, rx_analog_bit integer, rx_switch_num integer, rx_switch_bit integer, analog_min integer check (rx_analog_num >= analog_min), switch_min integer check (rx_switch_num >= switch_min), last_connected_camera text, last_connected_da58_wifi text, rx_type integer default -1, pan_id text, tx_addr text, rx_res_info_blob blob )";
    private static final String CREATE_ON_INSERT_DR_TRIGGER = "create trigger on_insert_dr after insert on dr_data when new._id!=0 begin  insert into dr_curve(_pid,sw_state)  values(new._id,0); insert into dr_curve(_pid,sw_state)  values(new._id,1); insert into dr_curve(_pid,sw_state)  values(new._id,2);end";
    private static final String CREATE_ON_INSERT_FMODE_TRIGGER = ("create trigger on_insert_fmode after insert on f_mode when new._id!=0 begin  insert into thr_data(_pid)  values(new._id); insert into dr_data(_pid,function) values(new._id,'ail'); insert into dr_data(_pid,function) values(new._id,'ele'); insert into dr_data(_pid,function) values(new._id,'rud'); insert into servo_data(_pid,function)  values(new._id,'" + KEY_FUNC_ARRAY[0] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[1] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[2] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[3] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[4] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[5] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[6] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[7] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[8] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[9] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[10] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[11] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[12] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[13] + "'" + ");" + " insert into " + SERVO_DATA_TABLE_NAME + "(" + KEY_PID + "," + "function" + ") " + " values(new." + KEY_ID + "," + "'" + KEY_FUNC_ARRAY[14] + "'" + ");" + "end");
    private static final String CREATE_ON_INSERT_MODEL_TRIGGER = "create trigger on_insert_model after insert on models when new._id!=0 begin insert into f_mode (_pid,fmode_state) values(new._id,0); insert into f_mode (_pid,fmode_state) values(new._id,1); insert into f_mode (_pid,fmode_state) values(new._id,2);end";
    private static final String CREATE_ON_INSERT_THR_TRIGGER = "create trigger on_insert_throttle after insert on thr_data when new._id!=0 begin  insert into thr_curve(_pid,sw_state)  values(new._id,0); insert into thr_curve(_pid,sw_state)  values(new._id,1); insert into thr_curve(_pid,sw_state)  values(new._id,2);end";
    private static final String CREATE_SERVO_DATA_TABLE = "create table servo_data ( _id integer primary key autoincrement, _pid integer, function text not null, sub_trim integer default 0, reverse integer default 0, speed integer default 10, travel_l integer default -100, travel_r integer default 100, foreign key(_pid) references f_mode(_id) on delete cascade)";
    private static final String CREATE_THR_CURVE_TABLE = "create table thr_curve ( _id integer primary key autoincrement, _pid integer, sw_state integer, pot0 text default 0, pot1 text default 25, pot2 text default 50, pot3 text default 75, pot4 text default 100, foreign key(_pid) references thr_data(_id) on delete cascade)";
    private static final String CREATE_THR_DATA_TABLE = "create table thr_data (_id integer primary key autoincrement, _pid integer, switch text default INH, expo integer default 0, cut_switch text default B2, cut_value_1 integer default -50,cut_value_2 integer default 0,foreign key(_pid) references f_mode(_id) on delete cascade)";
    public static final String DB_NAME = "models.db";
    public static final String DR_CURVE_TABLE_NAME = "dr_curve";
    public static final String DR_DATA_TABLE_NAME = "dr_data";
    public static final String F_MODE_TABLE_NAME = "f_mode";
    public static final String KEY_ALIAS = "alias";
    public static final String KEY_ANALOG_MIN = "analog_min";
    public static final String KEY_CHANNEL = "channel";
    public static final String KEY_CONNECT_WIFI_INFO = "last_connected_da58_wifi";
    public static final String KEY_CURRENT_FMODE = "current_fmode";
    public static final String KEY_CURVE_TYPE = "type";
    public static final String KEY_CUT_SW = "cut_switch";
    public static final String KEY_CUT_VALUE_1 = "cut_value_1";
    public static final String KEY_CUT_VALUE_2 = "cut_value_2";
    public static final String KEY_DR_SWITCH = "dr_switch";
    public static final String KEY_EXPO = "expo";
    public static final String KEY_EXPO1 = "expo1";
    public static final String KEY_EXPO2 = "expo2";
    public static final String KEY_FMODE_STATE = "fmode_state";
    public static final String KEY_FPV = "fpv";
    public static final String KEY_FUNCTION = "function";
    public static final String[] KEY_FUNC_ARRAY = new String[]{"Thr", "Ail", "Ele", "Rud", "A01", "A02", "A03", "A04", "A05", "A06", "A07", "A08", "A09", "A10", "A11"};
    public static final String KEY_F_MODE_KEY = "f_mode_key";
    public static final String KEY_HARDWARE = "hardware";
    public static final String KEY_ICON = "icon";
    public static final String KEY_ID = "_id";
    public static final String KEY_LAST_CONNECT_CAMERA = "last_connected_camera";
    public static final String KEY_MIX_COEFFIENT = "coeffient";
    public static final String KEY_MIX_MASTER = "master_ch";
    public static final String KEY_MIX_SLAVER1 = "slaver_ch1";
    public static final String KEY_MIX_SLAVER2 = "slaver_ch2";
    public static final String KEY_MIX_SLAVER3 = "slaver_ch3";
    public static final String KEY_MIX_SLAVER4 = "slaver_ch4";
    public static final String KEY_MIX_SLAVER5 = "slaver_ch5";
    public static final String KEY_MIX_SLAVER6 = "slaver_ch6";
    public static final String KEY_MIX_SLAVER7 = "slaver_ch7";
    public static final String KEY_MIX_SWITCH1 = "switch1";
    public static final String KEY_MIX_SWITCH2 = "switch2";
    public static final String KEY_MIX_SWITCH3 = "switch3";
    public static final String KEY_MIX_SWITCH4 = "switch4";
    public static final String KEY_MIX_SWITCH5 = "switch5";
    public static final String KEY_MIX_SWITCH6 = "switch6";
    public static final String KEY_MIX_SWITCH7 = "switch7";
    public static final String KEY_MIX_SWITCH8 = "switch8";
    public static final String KEY_MIX_TYPE = "mix_type";
    public static final String KEY_NAME = "name";
    public static final String KEY_OFFSET = "offset";
    public static final String KEY_PAN_ID = "pan_id";
    public static final String KEY_PID = "_pid";
    public static final String KEY_POT0 = "pot0";
    public static final String KEY_POT1 = "pot1";
    public static final String KEY_POT2 = "pot2";
    public static final String KEY_POT3 = "pot3";
    public static final String KEY_POT4 = "pot4";
    public static final String KEY_RATE1 = "rate1";
    public static final String KEY_RATE2 = "rate2";
    public static final String KEY_REVERSE = "reverse";
    public static final String KEY_RX = "rx";
    public static final String KEY_RX_ANALOG_BIT = "rx_analog_bit";
    public static final String KEY_RX_ANALOG_NUM = "rx_analog_num";
    public static final String KEY_RX_RES_INFO_BLOB = "rx_res_info_blob";
    public static final String KEY_RX_SWITCH_BIT = "rx_switch_bit";
    public static final String KEY_RX_SWITCH_NUM = "rx_switch_num";
    public static final String KEY_RX_TYPE = "rx_type";
    public static final String KEY_SPEED = "speed";
    public static final String KEY_SUB_TRIM = "sub_trim";
    public static final String KEY_SW = "switch";
    public static final String KEY_SWITCH_HIGH = "high";
    public static final String KEY_SWITCH_LOW = "low";
    public static final String KEY_SWITCH_MIDDLE = "middle";
    public static final String KEY_SWITCH_MIN = "switch_min";
    public static final String KEY_SWITCH_STATUS_HIGH = "status_high";
    public static final String KEY_SWITCH_STATUS_LOW = "status_low";
    public static final String KEY_SWITCH_STATUS_MID = "status_mid";
    public static final String KEY_SW_STATE = "sw_state";
    public static final String KEY_TRAVEL_L = "travel_l";
    public static final String KEY_TRAVEL_R = "travel_r";
    public static final String KEY_TX_ADDR = "tx_addr";
    public static final String KEY_TYPE = "type";
    public static final String MODEL_TABLE_NAME = "models";
    public static final String SERVO_DATA_TABLE_NAME = "servo_data";
    public static final String THR_CURVE_TABLE_NAME = "thr_curve";
    public static final String THR_DATA_TABLE_NAME = "thr_data";
    public static final String VERION = "version";
    public static final int VERSION = 1;

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MODELS_TABLE);
        db.execSQL(CREATE_CHANNEL_MAP_TABLE);
        db.execSQL(CREATE_FLIGHTMODE_TABLE);
        db.execSQL(CREATE_THR_DATA_TABLE);
        db.execSQL(CREATE_THR_CURVE_TABLE);
        db.execSQL(CREATE_DR_DATA_TABLE);
        db.execSQL(CREATE_DR_CURVE_TABLE);
        db.execSQL(CREATE_SERVO_DATA_TABLE);
        db.execSQL(CREATE_ON_INSERT_MODEL_TRIGGER);
        db.execSQL(CREATE_ON_INSERT_FMODE_TRIGGER);
        db.execSQL(CREATE_ON_INSERT_THR_TRIGGER);
        db.execSQL(CREATE_ON_INSERT_DR_TRIGGER);
        init(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    private void init(SQLiteDatabase db) {
        db.execSQL("insert into f_mode values(0,0,-1)");
    }

    public void initChannelMap(long id, SQLiteDatabase db, int mode) {
        generateStickChannelMap(db, mode, id);
        db.execSQL("insert into channel_map values(null," + id + ",5,'A01','INH','A01')");
        db.execSQL("insert into channel_map values(null," + id + ",6,'A02','INH','A02')");
        db.execSQL("insert into channel_map values(null," + id + ",7,'A03','INH','A03')");
        db.execSQL("insert into channel_map values(null," + id + ",8,'A04','INH','A04')");
        db.execSQL("insert into channel_map values(null," + id + ",9,'A05','INH','A05')");
        db.execSQL("insert into channel_map values(null," + id + ",10,'A06','INH','A06')");
        db.execSQL("insert into channel_map values(null," + id + ",11,'A07','INH','A07')");
        db.execSQL("insert into channel_map values(null," + id + ",12,'A08','INH','A08')");
        db.execSQL("insert into channel_map values(null," + id + ",13,'A09','INH','A09')");
        db.execSQL("insert into channel_map values(null," + id + ",14,'A10','INH','A10')");
        db.execSQL("insert into channel_map values(null," + id + ",15,'A11','INH','A11')");
    }

    private void generateStickChannelMap(SQLiteDatabase db, int mode, long id) {
        if (mode == 1) {
            db.execSQL("insert into channel_map values(null," + id + ",1,'Thr','J3','Thr')");
            db.execSQL("insert into channel_map values(null," + id + ",2,'Ail','J4','Ail')");
            db.execSQL("insert into channel_map values(null," + id + ",3,'Ele','J1','Ele')");
            db.execSQL("insert into channel_map values(null," + id + ",4,'Rud','J2','Rud')");
        } else if (mode == 2) {
            db.execSQL("insert into channel_map values(null," + id + ",1,'Thr','J1','Thr')");
            db.execSQL("insert into channel_map values(null," + id + ",2,'Ail','J4','Ail')");
            db.execSQL("insert into channel_map values(null," + id + ",3,'Ele','J3','Ele')");
            db.execSQL("insert into channel_map values(null," + id + ",4,'Rud','J2','Rud')");
        } else if (mode == 3) {
            db.execSQL("insert into channel_map values(null," + id + ",1,'Thr','J3','Thr')");
            db.execSQL("insert into channel_map values(null," + id + ",2,'Ail','J2','Ail')");
            db.execSQL("insert into channel_map values(null," + id + ",3,'Ele','J1','Ele')");
            db.execSQL("insert into channel_map values(null," + id + ",4,'Rud','J4','Rud')");
        } else if (mode == 4) {
            db.execSQL("insert into channel_map values(null," + id + ",1,'Thr','J1','Thr')");
            db.execSQL("insert into channel_map values(null," + id + ",2,'Ail','J2','Ail')");
            db.execSQL("insert into channel_map values(null," + id + ",3,'Ele','J3','Ele')");
            db.execSQL("insert into channel_map values(null," + id + ",4,'Rud','J4','Rud')");
        }
    }

    void onUpdateFmodeKey(long id, String old_fkey, SQLiteDatabase db) {
    }

    private static String generateFindSwitchByModel(long id) {
        return null;
    }

    private static String generateFindSwitchByModelInternal(long id, String fmode) {
        return null;
    }

    static String generateMixingBrief(String table, String model, String mode, String master) {
        return null;
    }

    static String generateMasterId(String model, String mode, String master) {
        return null;
    }

    static void updateParamOfAnalogTable(SQLiteDatabase db, String model, String mode, String param, String old_value, String new_value) {
    }

    static String querySwitchesMap(String model, String mode) {
        return null;
    }

    private static String changeHardware(String alias, String hardware) {
        return "update channel_map set hardware = '" + hardware + "'" + " where " + KEY_ALIAS + " like " + "'" + alias + "'";
    }

    static boolean updateChannelMap(SQLiteDatabase db, int mode) {
        String sql_1;
        String sql_2;
        String sql_3;
        String sql_4;
        switch (mode) {
            case 1:
                sql_1 = changeHardware("Thr", "J3");
                sql_2 = changeHardware("Ail", "J4");
                sql_3 = changeHardware("Ele", "J1");
                sql_4 = changeHardware("Rud", "J2");
                break;
            case 2:
                sql_1 = changeHardware("Thr", "J1");
                sql_2 = changeHardware("Ail", "J4");
                sql_3 = changeHardware("Ele", "J3");
                sql_4 = changeHardware("Rud", "J2");
                break;
            case 3:
                sql_1 = changeHardware("Thr", "J3");
                sql_2 = changeHardware("Ail", "J2");
                sql_3 = changeHardware("Ele", "J1");
                sql_4 = changeHardware("Rud", "J4");
                break;
            case 4:
                sql_1 = changeHardware("Thr", "J1");
                sql_2 = changeHardware("Ail", "J2");
                sql_3 = changeHardware("Ele", "J3");
                sql_4 = changeHardware("Rud", "J4");
                break;
            default:
                Log.e("DBOpenHelper", "error! mode = " + mode);
                return false;
        }
        db.execSQL(sql_1);
        db.execSQL(sql_2);
        db.execSQL(sql_3);
        db.execSQL(sql_4);
        return true;
    }
}
