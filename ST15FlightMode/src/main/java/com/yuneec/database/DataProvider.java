package com.yuneec.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DataProvider extends ContentProvider {
    public static final int ALL_MODELS = 1;
    public static final String AUTHORITY = "com.yuneec.databaseprovider";
    public static final int CHANNEL_MAP = 3;
    public static final Uri CHMAP_URI = Uri.parse("content://com.yuneec.databaseprovider/channel_map/#");
    public static final int DR = 7;
    public static final int DR_CURVE = 8;
    public static final Uri DR_CURVE_URI = Uri.parse("content://com.yuneec.databaseprovider/dr_curve/#");
    public static final Uri DR_URI = Uri.parse("content://com.yuneec.databaseprovider/dr_data/#");
    public static final String EXTRA_PARAMS = "params";
    public static final Uri FMODE_URI = Uri.parse("content://com.yuneec.databaseprovider/f_mode/#");
    public static final int F_MODE = 4;
    public static final String MIME_ITEM = "vnd.yuneec.model";
    public static final String MIME_TYPE_ALL = "vnd.android.cursor.dir/vnd.yuneec.model";
    public static final String MIME_TYPE_SINGLE = "vnd.android.cursor.item/vnd.yuneec.model";
    public static final Uri MODEL_URI = Uri.parse("content://com.yuneec.databaseprovider/model");
    public static final int PARAMS = 10;
    public static final String PARAMS_CHANGE_MODE = "change_mode";
    public static final String PARAMS_FUNCTION = "function";
    public static final String PARAMS_F_MODE = "fmode";
    public static final String PARAMS_F_MODE_1 = "f_mode1";
    public static final String PARAMS_F_MODE_2 = "f_mode2";
    public static final String PARAMS_F_MODE_3 = "f_mode3";
    public static final String PARAMS_HARDWARE = "hardware";
    public static final String PARAMS_MASTER_CHANNEL = "master_channel";
    public static final String PARAMS_MASTER_ID = "master_id";
    public static final String PARAMS_MIXING_BRIEF = "mixing_brief";
    public static final String PARAMS_MODE = "mode";
    public static final String PARAMS_MODEL = "model";
    public static final String PARAMS_PARENT_ID = "parent_id";
    public static final String PARAMS_RESET_MIXING = "reset_mixing";
    public static final String PARAMS_SWITCHES_MAP = "switches_map";
    public static final String PARAMS_SW_STATE = "sw_state";
    public static final Uri PARAMS_URI = Uri.parse("content://com.yuneec.databaseprovider/params/");
    public static final String PATH_ALL_MODEL = "model";
    public static final String PATH_CHMAP = "channel_map/#";
    public static final String PATH_DR = "dr_data/#";
    public static final String PATH_DR_CURVE = "dr_curve/#";
    public static final String PATH_FMODE = "f_mode/#";
    public static final String PATH_PARAMS = "params/";
    public static final String PATH_SERVO = "servo/#";
    public static final String PATH_SINGLE_MODEL = "model/#";
    public static final String PATH_THR = "thr_data/#";
    public static final String PATH_THR_CURVE = "thr_curve/#";
    public static final int SERVO = 9;
    public static final Uri SERVO_URI = Uri.parse("content://com.yuneec.databaseprovider/servo/#");
    public static final int SINGLE_MODEL = 2;
    private static final String TAG = "DataProvider";
    public static final int THR = 5;
    public static final int THR_CURVE = 6;
    public static final Uri THR_CURVE_URI = Uri.parse("content://com.yuneec.databaseprovider/thr_curve/#");
    public static final Uri THR_URI = Uri.parse("content://com.yuneec.databaseprovider/thr_data/#");
    private static final UriMatcher uriMatcher = new UriMatcher(-1);
    private DBOpenHelper mOpenHelper;

    public android.net.Uri insert(android.net.Uri r13, android.content.ContentValues r14) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:31:? in {3, 4, 11, 16, 17, 18, 19, 24, 26, 27, 29, 30, 32, 33} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r12 = this;
        r11 = 1;
        r9 = 0;
        r8 = r12.mOpenHelper;
        r0 = r8.getWritableDatabase();
        r6 = 0;
        r8 = "DEFAULT";
        r7 = android.net.Uri.parse(r8);
        r8 = uriMatcher;
        r8 = r8.match(r13);
        switch(r8) {
            case 1: goto L_0x002d;
            case 2: goto L_0x0018;
            case 3: goto L_0x007c;
            case 4: goto L_0x0018;
            case 5: goto L_0x0081;
            case 6: goto L_0x0018;
            case 7: goto L_0x0093;
            case 8: goto L_0x0018;
            case 9: goto L_0x00a5;
            default: goto L_0x0018;
        };
    L_0x0018:
        r8 = new java.lang.IllegalArgumentException;
        r9 = new java.lang.StringBuilder;
        r10 = "Disallowed uri:";
        r9.<init>(r10);
        r9 = r9.append(r13);
        r9 = r9.toString();
        r8.<init>(r9);
        throw r8;
    L_0x002d:
        r7 = MODEL_URI;
        r6 = "models";
    L_0x0031:
        r4 = -1;
        r8 = MODEL_URI;
        r8 = r7.equals(r8);
        if (r8 == 0) goto L_0x00cd;
    L_0x003b:
        r0.beginTransaction();
        r8 = 0;
        r4 = r0.insertOrThrow(r6, r8, r14);	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r8 = r12.getContext();	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r10 = "flight_setting_value";	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r11 = 0;	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r8 = r8.getSharedPreferences(r10, r11);	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r10 = "mode_select_value";	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r11 = 2;	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r2 = r8.getInt(r10, r11);	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r8 = r12.needInitChannelMap(r14);	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        if (r8 == 0) goto L_0x0060;	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
    L_0x005b:
        r8 = r12.mOpenHelper;	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r8.initChannelMap(r4, r0, r2);	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
    L_0x0060:
        r0.setTransactionSuccessful();	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r0.endTransaction();
    L_0x0066:
        r10 = 0;
        r8 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r8 <= 0) goto L_0x00d2;
    L_0x006c:
        r3 = android.content.ContentUris.withAppendedId(r7, r4);
        r8 = r12.getContext();
        r8 = r8.getContentResolver();
        r8.notifyChange(r3, r9);
    L_0x007b:
        return r3;
    L_0x007c:
        r7 = CHMAP_URI;
        r6 = "channel_map";
        goto L_0x0031;
    L_0x0081:
        r6 = "thr_data";
        r10 = "_pid";
        r8 = r13.getPathSegments();
        r8 = r8.get(r11);
        r8 = (java.lang.String) r8;
        r14.put(r10, r8);
        goto L_0x0031;
    L_0x0093:
        r6 = "dr_data";
        r10 = "_pid";
        r8 = r13.getPathSegments();
        r8 = r8.get(r11);
        r8 = (java.lang.String) r8;
        r14.put(r10, r8);
        goto L_0x0031;
    L_0x00a5:
        r6 = "servo_data";
        r10 = "_pid";
        r8 = r13.getPathSegments();
        r8 = r8.get(r11);
        r8 = (java.lang.String) r8;
        r14.put(r10, r8);
        goto L_0x0031;
    L_0x00b8:
        r1 = move-exception;
        r8 = "DataProvider";	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r10 = r1.getMessage();	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        android.util.Log.e(r8, r10);	 Catch:{ Throwable -> 0x00b8, all -> 0x00c8 }
        r4 = -1;
        r0.endTransaction();
        goto L_0x0066;
    L_0x00c8:
        r8 = move-exception;
        r0.endTransaction();
        throw r8;
    L_0x00cd:
        r4 = r0.insert(r6, r9, r14);
        goto L_0x0066;
    L_0x00d2:
        r8 = "DataProvider";
        r10 = new java.lang.StringBuilder;
        r11 = "Fail to insert a row into :";
        r10.<init>(r11);
        r10 = r10.append(r13);
        r10 = r10.toString();
        android.util.Log.e(r8, r10);
        r3 = r9;
        goto L_0x007b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.database.DataProvider.insert(android.net.Uri, android.content.ContentValues):android.net.Uri");
    }

    static {
        uriMatcher.addURI(AUTHORITY, "model", 1);
        uriMatcher.addURI(AUTHORITY, PATH_SINGLE_MODEL, 2);
        uriMatcher.addURI(AUTHORITY, PATH_PARAMS, 10);
        uriMatcher.addURI(AUTHORITY, PATH_CHMAP, 3);
        uriMatcher.addURI(AUTHORITY, PATH_FMODE, 4);
        uriMatcher.addURI(AUTHORITY, PATH_THR, 5);
        uriMatcher.addURI(AUTHORITY, PATH_THR_CURVE, 6);
        uriMatcher.addURI(AUTHORITY, PATH_DR, 7);
        uriMatcher.addURI(AUTHORITY, PATH_DR_CURVE, 8);
        uriMatcher.addURI(AUTHORITY, PATH_SERVO, 9);
    }

    public boolean onCreate() {
        this.mOpenHelper = new DBOpenHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String parameter;
        switch (uriMatcher.match(uri)) {
            case 1:
                break;
            case 2:
                qb.appendWhere("_id=" + ((String) uri.getPathSegments().get(1)));
                break;
            case 3:
                qb.setTables(DBOpenHelper.CHANNEL_MAP_TABLE_NAME);
                if (!"true".equals(uri.getQueryParameter(PARAMS_PARENT_ID))) {
                    qb.appendWhere("_id=" + ((String) uri.getPathSegments().get(1)));
                    break;
                }
                qb.appendWhere("_pid=" + ((String) uri.getPathSegments().get(1)));
                break;
            case 4:
                qb.setTables(DBOpenHelper.F_MODE_TABLE_NAME);
                if (!"true".equals(uri.getQueryParameter(PARAMS_PARENT_ID))) {
                    qb.appendWhere("_id=" + ((String) uri.getPathSegments().get(1)));
                    break;
                }
                qb.appendWhere("_pid=" + ((String) uri.getPathSegments().get(1)));
                break;
            case 5:
                qb.setTables(DBOpenHelper.THR_DATA_TABLE_NAME);
                if (!"true".equals(uri.getQueryParameter(PARAMS_PARENT_ID))) {
                    qb.appendWhere("_id=" + ((String) uri.getPathSegments().get(1)));
                    break;
                }
                qb.appendWhere("_pid=" + ((String) uri.getPathSegments().get(1)));
                break;
            case 6:
                qb.setTables(DBOpenHelper.THR_CURVE_TABLE_NAME);
                qb.appendWhere("_pid=" + ((String) uri.getPathSegments().get(1)));
                break;
            case 7:
                qb.setTables(DBOpenHelper.DR_DATA_TABLE_NAME);
                parameter = uri.getQueryParameter("function");
                if (!"true".equals(uri.getQueryParameter(PARAMS_PARENT_ID))) {
                    qb.appendWhere("_id=" + ((String) uri.getPathSegments().get(1)));
                    break;
                }
                qb.appendWhere("_pid=" + ((String) uri.getPathSegments().get(1)) + " and " + "function" + "='" + parameter + "'");
                break;
            case 8:
                qb.setTables(DBOpenHelper.DR_CURVE_TABLE_NAME);
                qb.appendWhere("_pid=" + ((String) uri.getPathSegments().get(1)));
                break;
            case 9:
                qb.setTables(DBOpenHelper.SERVO_DATA_TABLE_NAME);
                parameter = uri.getQueryParameter("function");
                if (!"true".equals(uri.getQueryParameter(PARAMS_PARENT_ID))) {
                    qb.appendWhere("_id=" + ((String) uri.getPathSegments().get(1)));
                    break;
                }
                qb.appendWhere("_pid=" + ((String) uri.getPathSegments().get(1)) + " and " + "function" + "='" + parameter + "'");
                break;
            case 10:
                return null;
            default:
                Log.e(TAG, "Unkown uri:" + uri);
                return null;
        }
        qb.setTables(DBOpenHelper.MODEL_TABLE_NAME);
        Cursor c = qb.query(this.mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case 1:
            case 10:
                return MIME_TYPE_ALL;
            case 2:
            case 3:
                return MIME_TYPE_SINGLE;
            default:
                throw new IllegalArgumentException("Unkown uri:" + uri);
        }
    }

    private boolean needInitChannelMap(ContentValues value) {
        if (100 != 0) {
            return true;
        }
        return false;
    }

    private String checkChannelMapInput(Uri uri, ContentValues cv, boolean isInsert) {
        String master = uri.getQueryParameter(PARAMS_MASTER_CHANNEL);
        if (master == null) {
            return "you must assgin a channel to insert/modify";
        }
        String ch = cv.getAsString(DBOpenHelper.KEY_CHANNEL);
        String hw = cv.getAsString("hardware");
        String func = cv.getAsString("function");
        if (isInsert && (ch == null || hw == null || func == null)) {
            return "insert action must contain channel, function and hardware";
        }
        if (ch != null && !ch.equals(master)) {
            return "channel in ContentValues is not match with the values in Uri";
        }
        if (ch == null || isInsert) {
            return "OK";
        }
        return "channel can not be modified once inserted";
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;
        String finalWhere;
        switch (uriMatcher.match(uri)) {
            case 2:
                table = DBOpenHelper.MODEL_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 3:
                table = DBOpenHelper.CHANNEL_MAP_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 5:
                table = DBOpenHelper.THR_DATA_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 7:
                table = DBOpenHelper.DR_DATA_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 9:
                table = DBOpenHelper.SERVO_DATA_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 10:
                return -1;
            default:
                throw new IllegalArgumentException("Disallowed uri:" + uri);
        }
        int count = this.mOpenHelper.getWritableDatabase().delete(table, finalWhere, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String finalWhere;
        String table;
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        Uri which = Uri.parse("DEFAULT");
        switch (uriMatcher.match(uri)) {
            case 1:
                finalWhere = selection;
                table = DBOpenHelper.MODEL_TABLE_NAME;
                which = MODEL_URI;
                break;
            case 2:
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                table = DBOpenHelper.MODEL_TABLE_NAME;
                which = MODEL_URI;
                break;
            case 3:
                table = DBOpenHelper.CHANNEL_MAP_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                if ("true".equals(uri.getQueryParameter(PARAMS_CHANGE_MODE))) {
                    DBOpenHelper.updateChannelMap(db, Integer.parseInt(uri.getQueryParameter(PARAMS_MODE)));
                    return 0;
                }
                break;
            case 5:
                table = DBOpenHelper.THR_DATA_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 6:
                table = DBOpenHelper.THR_CURVE_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 7:
                table = DBOpenHelper.DR_DATA_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 8:
                table = DBOpenHelper.DR_CURVE_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 9:
                table = DBOpenHelper.SERVO_DATA_TABLE_NAME;
                finalWhere = "_id=" + ((String) uri.getPathSegments().get(1));
                break;
            case 10:
                table = uri.getQueryParameter(EXTRA_PARAMS);
                String mode = uri.getQueryParameter(PARAMS_MODE);
                finalWhere = "_pid=" + mode;
                values.put(DBOpenHelper.KEY_PID, Long.valueOf(Long.parseLong(mode)));
                which = PARAMS_URI;
                break;
            default:
                throw new IllegalArgumentException("Disallowed uri:" + uri);
        }
        if (which.equals(MODEL_URI)) {
            if (values.containsKey(DBOpenHelper.KEY_F_MODE_KEY) && !onUpdateFModeKey(db, values.getAsString(DBOpenHelper.KEY_F_MODE_KEY), Long.parseLong((String) uri.getPathSegments().get(1)))) {
                Log.e(TAG, "onUpdateFModeKey failed, remove field 'f_mode_key'");
                values.remove(DBOpenHelper.KEY_F_MODE_KEY);
            }
            count = db.update(table, values, finalWhere, selectionArgs);
        } else {
            count = db.update(table, values, finalWhere, selectionArgs);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private boolean onUpdateFModeKey(SQLiteDatabase db, String new_fkey, long model_id) {
        Uri uri = ContentUris.withAppendedId(MODEL_URI, model_id);
        Cursor c = getContext().getContentResolver().query(uri, new String[]{DBOpenHelper.KEY_F_MODE_KEY}, null, null, null);
        if (!DataProviderHelper.isCursorValid(c)) {
            return false;
        }
        String f_key = c.getString(c.getColumnIndex(DBOpenHelper.KEY_F_MODE_KEY));
        if (!(new_fkey == null || new_fkey.equals(f_key))) {
            this.mOpenHelper.onUpdateFmodeKey(model_id, new_fkey, db);
        }
        c.close();
        return true;
    }
}
