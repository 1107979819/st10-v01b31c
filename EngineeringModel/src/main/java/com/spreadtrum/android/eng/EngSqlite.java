package com.spreadtrum.android.eng;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class EngSqlite {
    private static EngSqlite mEngSqlite;
    private Context mContext;
    private SQLiteDatabase mSqLiteDatabase = null;

    private static class EngineeringModeDatabaseHelper extends SQLiteOpenHelper {
        public EngineeringModeDatabaseHelper(Context context) {
            super(context, "/productinfo/engtest.db", null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS str2int (_id INTEGER PRIMARY KEY AUTOINCREMENT,groupid INTEGER NOT NULL DEFAULT 0,name TEXT,value INTEGER NOT NULL DEFAULT 0);");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion) {
                db.execSQL("DROP TABLE IF EXISTS str2int;");
                onCreate(db);
            }
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion < oldVersion) {
                db.execSQL("DROP TABLE IF EXISTS str2int;");
                onCreate(db);
            }
        }
    }

    public static synchronized EngSqlite getInstance(Context context) {
        EngSqlite engSqlite;
        synchronized (EngSqlite.class) {
            if (mEngSqlite == null) {
                mEngSqlite = new EngSqlite(context);
            }
            engSqlite = mEngSqlite;
        }
        return engSqlite;
    }

    private EngSqlite(Context context) {
        IOException e;
        Throwable th;
        InterruptedException e2;
        this.mContext = context;
        File file = new File("/productinfo/engtest.db");
        DataOutputStream os = null;
        try {
            Process p = Runtime.getRuntime().exec("chmod 777 productinfo");
            DataOutputStream os2 = new DataOutputStream(p.getOutputStream());
            try {
                Log.v("Vtools", "os= " + new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getErrorStream()))).readLine());
                Runtime.getRuntime().exec("chmod 777 " + file.getAbsolutePath());
                p.waitFor();
                if (os2 != null) {
                    try {
                        os2.close();
                        os = os2;
                    } catch (IOException e3) {
                        e3.printStackTrace();
                        os = os2;
                    }
                }
            } catch (IOException e4) {
                e3 = e4;
                os = os2;
                try {
                    e3.printStackTrace();
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    this.mSqLiteDatabase = new EngineeringModeDatabaseHelper(this.mContext).getWritableDatabase();
                } catch (Throwable th2) {
                    th = th2;
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (InterruptedException e5) {
                e2 = e5;
                os = os2;
                e2.printStackTrace();
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
                this.mSqLiteDatabase = new EngineeringModeDatabaseHelper(this.mContext).getWritableDatabase();
            } catch (Throwable th3) {
                th = th3;
                os = os2;
                if (os != null) {
                    os.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e3222 = e6;
            e3222.printStackTrace();
            if (os != null) {
                os.close();
            }
            this.mSqLiteDatabase = new EngineeringModeDatabaseHelper(this.mContext).getWritableDatabase();
        } catch (InterruptedException e7) {
            e2 = e7;
            e2.printStackTrace();
            if (os != null) {
                os.close();
            }
            this.mSqLiteDatabase = new EngineeringModeDatabaseHelper(this.mContext).getWritableDatabase();
        }
        this.mSqLiteDatabase = new EngineeringModeDatabaseHelper(this.mContext).getWritableDatabase();
    }

    public int queryFactoryModeDate(String name) {
        int ret = 0;
        try {
            Cursor c = this.mSqLiteDatabase.query("str2int", new String[]{"name", "value"}, "name= '" + name + "'", null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                ret = c.getInt(1);
                c.close();
            }
            return ret;
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean queryData(String name) {
        try {
            Cursor c = this.mSqLiteDatabase.query("str2int", new String[]{"name", "value"}, "name= '" + name + "'", null, null, null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                }
                c.close();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void insertFactoryModeData(String name, int value) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("value", Integer.valueOf(value));
        long returnValue = this.mSqLiteDatabase.insert("str2int", null, cv);
        Log.e("EngSqlite", "returnValue" + returnValue);
        if (returnValue == -1) {
            Log.e("EngSqlite", "insert DB error!");
        }
    }

    private void updateFactoryModeData(String name, int value) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("value", Integer.valueOf(value));
        this.mSqLiteDatabase.update("str2int", cv, "name= '" + name + "'", null);
    }

    public void updataFactoryModeDB(String name, int value) {
        if (queryData(name)) {
            updateFactoryModeData(name, value);
        } else {
            insertFactoryModeData(name, value);
        }
    }

    public void release() {
        if (this.mSqLiteDatabase != null) {
            this.mSqLiteDatabase.close();
            this.mSqLiteDatabase = null;
        }
        if (mEngSqlite != null) {
            mEngSqlite = null;
        }
    }
}
