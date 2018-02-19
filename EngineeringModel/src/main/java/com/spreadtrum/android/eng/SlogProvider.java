package com.spreadtrum.android.eng;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class SlogProvider extends ContentProvider {
    public static final Uri URI_ID_MODES = Uri.parse("content://com.spreadtrum.android.eng/modes/");
    public static final Uri URI_MODES = Uri.parse("content://com.spreadtrum.android.eng/modes");
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private DatabaseHelper mDb;

    public static class Contract implements BaseColumns {
        public static final String COLUMN_BLUETOOTH = "stream\tbt\t".replace("\t", "_");
        public static final String COLUMN_CLEAR_AUTO = "var\tslogsaveall\t".replace("\t", "_");
        public static final String COLUMN_KERNEL = "stream\tkernel\t".replace("\t", "_");
        public static final String COLUMN_MAIN = "stream\tmain\t".replace("\t", "_");
        public static final String COLUMN_MISC = "misc\tmisc\t".replace("\t", "_");
        public static final String COLUMN_MODEM = "stream\tmodem\t".replace("\t", "_");
        public static final String COLUMN_RADIO = "stream\tradio\t".replace("\t", "_");
        public static final String COLUMN_STORAGE = "logpath\t".replace("\t", "_");
        public static final String COLUMN_SYSTEM = "stream\tsystem\t".replace("\t", "_");
        public static final String COLUMN_TCP = "stream\ttcp\t".replace("\t", "_");
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, "slog.db", null, 3);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE mode (name TEXT NOT NULL,general TEXT NOT NULL," + Contract.COLUMN_MAIN + " INTEGER NOT NULL," + Contract.COLUMN_RADIO + " INTEGER NOT NULL," + Contract.COLUMN_KERNEL + " INTEGER NOT NULL," + Contract.COLUMN_SYSTEM + " INTEGER NOT NULL," + Contract.COLUMN_MODEM + " INTEGER NOT NULL," + Contract.COLUMN_TCP + " INTEGER NOT NULL," + Contract.COLUMN_BLUETOOTH + " INTEGER NOT NULL," + Contract.COLUMN_CLEAR_AUTO + " INTEGER NOT NULL," + Contract.COLUMN_MISC + " INTEGER NOT NULL," + Contract.COLUMN_STORAGE + " INTEGER NOT NULL," + "_id" + " INTEGER PRIMARY KEY);");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS mode");
            onCreate(db);
        }
    }

    static {
        sUriMatcher.addURI("com.spreadtrum.android.eng", "modes", 0);
        sUriMatcher.addURI("com.spreadtrum.android.eng", "modes/#", 1);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase db = this.mDb.getWritableDatabase();
        String where = selection;
        switch (sUriMatcher.match(uri)) {
            case 0:
                count = db.delete("mode", selection, selectionArgs);
                break;
            case 1:
                where = "_id = " + ((String) uri.getPathSegments().get(1));
                if (selection != null) {
                    where = where + " AND " + selection;
                }
                count = db.delete("mode", where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case 0:
                return "vnd.spreadtrum.cursor.dir/vnd.spreadtrum.slog";
            case 1:
                return "vnd.spreadtrum.cursor.item/vnd.spreadtrum.slog";
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = this.mDb.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case 0:
                long rowId = db.insert("mode", "_id", values);
                Uri iuri = ContentUris.withAppendedId(URI_ID_MODES, rowId);
                if (rowId > 0) {
                    getContext().getContentResolver().notifyChange(iuri, null);
                    return iuri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public boolean onCreate() {
        Log.d("Provider", "=====> onCreate, " + getContext());
        this.mDb = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String where = selection;
        switch (sUriMatcher.match(uri)) {
            case 0:
                qb.setTables("mode");
                break;
            case 1:
                if (uri.getPathSegments().size() > 1) {
                    where = "_id = " + ((String) uri.getPathSegments().get(1));
                    if (selection != null) {
                        where = where + " AND " + selection;
                    }
                }
                qb.setTables("mode");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Cursor c = qb.query(this.mDb.getReadableDatabase(), projection, where, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase db = this.mDb.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case 0:
                count = db.update("mode", values, selection, selectionArgs);
                break;
            case 1:
                String where = "_id = " + ((String) uri.getPathSegments().get(1));
                if (selection != null) {
                    where = where + " AND " + selection;
                }
                count = db.update("mode", values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
