package com.spreadtrum.android.eng;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.IMountService.Stub;
import android.util.Log;
import android.widget.CheckBox;
import com.android.internal.app.IMediaContainerService;
import com.spreadtrum.android.eng.SlogProvider.Contract;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.util.EncodingUtils;

public class SlogAction {
    private static final File EXTERNAL_STORAGE_DIRECTORY = getDirectory(getMainStoragePath(), "/mnt/sdcard/");
    private static boolean MMC_SUPPORT = "1".equals(SystemProperties.get("ro.device.support.mmc"));
    public static Context contextMainActivity;
    private static Context mContext;
    private static BroadcastReceiver mInstallReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.PACKAGE_ADDED".equals(intent.getAction()) && "com.android.synchronism".equals(intent.getData().getSchemeSpecificPart())) {
                if (SlogAction.mContext != null) {
                    SlogAction.mContext.unregisterReceiver(this);
                }
                SlogAction.mContext = null;
                new Thread() {
                    public void run() {
                        SystemProperties.set("persist.sys.synchronism.exist", "1");
                        SlogAction.runSlogCommand("am startservice -n com.android.synchronism/com.android.synchronism.service.CoreService");
                    }
                }.start();
            }
        }
    };
    private static Object mLock = new Object();
    private static BroadcastReceiver mNetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || !"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction()) || intent.getBooleanExtra("noConnectivity", false))) {
                Log.d("SlogAction", "receive net broadcast, start fetch thread");
                new Thread(null, new FetchRunnable(this), "FetchThread").start();
            }
            if (SlogAction.mTryTimes > 20) {
                Log.d("SlogAction", "unregisterRecier netReceiver");
                SlogAction.mContext.unregisterReceiver(this);
                SlogAction.registBroadcast(SlogAction.mContext, "synchronism download");
            }
        }
    };
    private static Object mResetLock = new Object();
    private static int mTryTimes = 0;

    class AnonymousClass1SnapThread extends Thread {
        Context mContext;

        AnonymousClass1SnapThread(Context context) {
            this.mContext = context.getApplicationContext();
        }

        public void run() {
            screenShot();
        }

        synchronized void screenShot() {
            File screenpath;
            Message msg = new Message();
            try {
                Looper.prepare();
            } catch (Exception e) {
                Log.e("SlogAction", "Failed prepare Looper");
            }
            try {
                Thread.sleep(500);
                Process proc = Runtime.getRuntime().exec("slogctl screen");
                try {
                    if (proc.waitFor() != 0) {
                        Log.d("Snap", "Exit value=" + proc.exitValue() + ".Maybe not correct");
                    }
                    msg.what = 21;
                    LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
                } catch (InterruptedException e2) {
                    msg.what = 22;
                    try {
                        LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
                    } catch (ExceptionInInitializerError e3) {
                        Log.e("SlogAction", "Can't send message");
                    }
                    Log.e("Snap", e2.toString());
                }
            } catch (Exception e4) {
                System.err.println("slogctl reload has Exception, log followed");
                msg.what = 22;
                try {
                    LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
                } catch (ExceptionInInitializerError e5) {
                    Log.e("SlogAction", "Can't send message");
                }
            }
            if (SlogAction.GetState("logpath\t") && SlogAction.IsHaveSDCard()) {
                screenpath = SlogAction.getExternalStorage();
            } else {
                screenpath = Environment.getDataDirectory();
            }
            SlogAction.scanScreenShotFile(new File(screenpath.getAbsolutePath() + File.separator + "slog"), this.mContext);
            try {
                Looper.loop();
            } catch (Exception e6) {
                Log.e("SlogAction", "Failed loop the Looper");
            }
        }
    }

    private static class ClearThread implements Runnable {
        private ClearThread() {
        }

        public void run() {
            SlogAction.runClearLog();
        }
    }

    private static class DumpThread implements Runnable {
        String filename;

        public DumpThread(String fname) {
            this.filename = fname;
        }

        public void run() {
            if (this.filename == null) {
                new Message().what = 18;
                Log.d("SlogUIDumpThreadRun()", "filename==null");
                return;
            }
            SlogAction.runDump(this.filename);
        }
    }

    private static class FetchRunnable implements Runnable {
        private BroadcastReceiver mBroadcastReceiver;

        FetchRunnable(BroadcastReceiver br) {
            this.mBroadcastReceiver = br;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r5 = this;
            r3 = com.spreadtrum.android.eng.SlogAction.mLock;
            monitor-enter(r3);
            r2 = "synchronism check";
            r0 = com.spreadtrum.android.eng.SlogAction.runSlogCommand(r2);	 Catch:{ all -> 0x0032 }
            if (r0 != 0) goto L_0x0016;
        L_0x000d:
            r2 = "SlogAction";
            r4 = "check == OK return";
            android.util.Log.d(r2, r4);	 Catch:{ all -> 0x0032 }
            monitor-exit(r3);	 Catch:{ all -> 0x0032 }
        L_0x0015:
            return;
        L_0x0016:
            r2 = "synchronism download";
            r1 = com.spreadtrum.android.eng.SlogAction.runSlogCommand(r2);	 Catch:{ all -> 0x0032 }
            if (r1 != 0) goto L_0x0035;
        L_0x001e:
            r2 = com.spreadtrum.android.eng.SlogAction.mContext;	 Catch:{ all -> 0x0032 }
            r4 = r5.mBroadcastReceiver;	 Catch:{ all -> 0x0032 }
            r2.unregisterReceiver(r4);	 Catch:{ all -> 0x0032 }
            r2 = com.spreadtrum.android.eng.SlogAction.mContext;	 Catch:{ all -> 0x0032 }
            r4 = "synchronism download";
            com.spreadtrum.android.eng.SlogAction.registBroadcast(r2, r4);	 Catch:{ all -> 0x0032 }
        L_0x0030:
            monitor-exit(r3);	 Catch:{ all -> 0x0032 }
            goto L_0x0015;
        L_0x0032:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x0032 }
            throw r2;
        L_0x0035:
            r2 = "SlogAction";
            r4 = "Failed fetch";
            android.util.Log.e(r2, r4);	 Catch:{ all -> 0x0032 }
            com.spreadtrum.android.eng.SlogAction.access$808();	 Catch:{ all -> 0x0032 }
            goto L_0x0030;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.spreadtrum.android.eng.SlogAction.FetchRunnable.run():void");
        }
    }

    private static class RunThread implements Runnable {
        private RunThread() {
        }

        public void run() {
            SlogAction.runCommand();
        }
    }

    static /* synthetic */ int access$808() {
        int i = mTryTimes;
        mTryTimes = i + 1;
        return i;
    }

    public static boolean GetState(String keyName) {
        if (keyName == null) {
            Log.e("slog", "You have give a null to GetState():boolean,please check");
            return false;
        }
        try {
            if (keyName.equals("\n") && GetState(keyName, true).equals("enable")) {
                return true;
            }
            if (keyName.equals("logpath\t") && GetState(keyName, true).equals("external")) {
                return true;
            }
            if (keyName.equals("var\tslogsaveall\t") && GetState(keyName, true).equals("on")) {
                return true;
            }
            if (GetState(keyName, false).equals("on")) {
                return true;
            }
            return false;
        } catch (NullPointerException nullPointer) {
            Log.e("GetState", "Maybe you change GetState(),but don't return null.Log:\n" + nullPointer);
            return false;
        }
    }

    public static boolean GetState(int otherCase) {
        switch (otherCase) {
            case 101:
                if (GetState("stream\tkernel\t", false).equals("on") || GetState("stream\tsystem\t", false).equals("on") || GetState("stream\tradio\t", false).equals("on")) {
                    return true;
                }
                if (GetState("stream\tmain\t", false).equals("on")) {
                    return true;
                }
                break;
            default:
                try {
                    Log.e("GetState(int)", "You have given a invalid case");
                    break;
                } catch (NullPointerException nullPointer) {
                    Log.e("GetState(int)", "Maybe you change GetState(),but don't return null.Log:\n" + nullPointer);
                    return false;
                }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized java.lang.String GetState(java.lang.String r13, boolean r14) {
        /*
        r9 = com.spreadtrum.android.eng.SlogAction.class;
        monitor-enter(r9);
        r1 = 0;
        r5 = 0;
        r7 = 0;
        r6 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x007f, Exception -> 0x008d }
        r8 = "/data/local/tmp/slog/slog.conf";
        r6.<init>(r8);	 Catch:{ FileNotFoundException -> 0x007f, Exception -> 0x008d }
        r8 = r6.available();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r0 = new byte[r8];	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r8 = r6.available();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r7 = new char[r8];	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r8 = r6.available();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r10 = 10;
        if (r8 >= r10) goto L_0x002c;
    L_0x0021:
        r6.close();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        resetSlogConf();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r8 = "decode error";
        r5 = r6;
    L_0x002a:
        monitor-exit(r9);
        return r8;
    L_0x002c:
        r6.read(r0);	 Catch:{ Exception -> 0x0052, FileNotFoundException -> 0x00e5, all -> 0x00ec }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0052, FileNotFoundException -> 0x00e5, all -> 0x00ec }
        r8 = "UTF-8";
        r8 = org.apache.http.util.EncodingUtils.getString(r0, r8);	 Catch:{ Exception -> 0x0052, FileNotFoundException -> 0x00e5, all -> 0x00ec }
        r2.<init>(r8);	 Catch:{ Exception -> 0x0052, FileNotFoundException -> 0x00e5, all -> 0x00ec }
        r6.close();	 Catch:{ FileNotFoundException -> 0x00e8, Exception -> 0x00e1 }
        if (r2 == 0) goto L_0x0046;
    L_0x003f:
        r8 = r2.length();	 Catch:{ all -> 0x00ef }
        r10 = 1;
        if (r8 >= r10) goto L_0x009b;
    L_0x0046:
        r8 = "SlogAction";
        r10 = "conf.lenght < 1, return decode_error";
        android.util.Log.d(r8, r10);	 Catch:{ all -> 0x00ef }
        r8 = "decode error";
        r5 = r6;
        r1 = r2;
        goto L_0x002a;
    L_0x0052:
        r3 = move-exception;
        r6.close();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r8 = "SlogAction";
        r10 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r10.<init>();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r11 = "Read buffer failed, because ";
        r10 = r10.append(r11);	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r11 = r3.getMessage();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r10 = r10.append(r11);	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r11 = "Now print stack";
        r10 = r10.append(r11);	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r10 = r10.toString();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        android.util.Log.e(r8, r10);	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r3.printStackTrace();	 Catch:{ FileNotFoundException -> 0x00e5, Exception -> 0x00de, all -> 0x00ec }
        r8 = "decode error";
        r5 = r6;
        goto L_0x002a;
    L_0x007f:
        r4 = move-exception;
    L_0x0080:
        r8 = "SlogAction";
        r10 = "File not found, reset slog.conf";
        android.util.Log.e(r8, r10);	 Catch:{ all -> 0x00db }
        resetSlogConf();	 Catch:{ all -> 0x00db }
        r8 = "decode error";
        goto L_0x002a;
    L_0x008d:
        r3 = move-exception;
    L_0x008e:
        r8 = "SlogAction";
        r10 = "Failed reading file. Now dump stack";
        android.util.Log.e(r8, r10);	 Catch:{ all -> 0x00db }
        r3.printStackTrace();	 Catch:{ all -> 0x00db }
        r8 = "decode error";
        goto L_0x002a;
    L_0x009b:
        r8 = r2.indexOf(r13);	 Catch:{ Exception -> 0x00ca }
        r10 = r13.length();	 Catch:{ Exception -> 0x00ca }
        r10 = r10 + r8;
        if (r14 == 0) goto L_0x00c7;
    L_0x00a6:
        r8 = "\n";
    L_0x00a8:
        r11 = r2.indexOf(r13);	 Catch:{ Exception -> 0x00ca }
        r12 = r13.length();	 Catch:{ Exception -> 0x00ca }
        r11 = r11 + r12;
        r11 = r11 + 1;
        r8 = r2.indexOf(r8, r11);	 Catch:{ Exception -> 0x00ca }
        r11 = 0;
        r2.getChars(r10, r8, r7, r11);	 Catch:{ Exception -> 0x00ca }
        r8 = java.lang.String.valueOf(r7);	 Catch:{ all -> 0x00ef }
        r8 = r8.trim();	 Catch:{ all -> 0x00ef }
        r5 = r6;
        r1 = r2;
        goto L_0x002a;
    L_0x00c7:
        r8 = "\t";
        goto L_0x00a8;
    L_0x00ca:
        r3 = move-exception;
        r8 = "SlogAction";
        r10 = "Catch exception";
        android.util.Log.d(r8, r10);	 Catch:{ all -> 0x00ef }
        r3.printStackTrace();	 Catch:{ all -> 0x00ef }
        r8 = "decode error";
        r5 = r6;
        r1 = r2;
        goto L_0x002a;
    L_0x00db:
        r8 = move-exception;
    L_0x00dc:
        monitor-exit(r9);
        throw r8;
    L_0x00de:
        r3 = move-exception;
        r5 = r6;
        goto L_0x008e;
    L_0x00e1:
        r3 = move-exception;
        r5 = r6;
        r1 = r2;
        goto L_0x008e;
    L_0x00e5:
        r4 = move-exception;
        r5 = r6;
        goto L_0x0080;
    L_0x00e8:
        r4 = move-exception;
        r5 = r6;
        r1 = r2;
        goto L_0x0080;
    L_0x00ec:
        r8 = move-exception;
        r5 = r6;
        goto L_0x00dc;
    L_0x00ef:
        r8 = move-exception;
        r5 = r6;
        r1 = r2;
        goto L_0x00dc;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.spreadtrum.android.eng.SlogAction.GetState(java.lang.String, boolean):java.lang.String");
    }

    private static synchronized void resetSlogConf() {
        synchronized (SlogAction.class) {
            new Thread() {
                public void run() {
                    synchronized (SlogAction.mResetLock) {
                        SlogAction.runSlogCommand("rm /data/local/tmp/slog/slog.conf");
                        SlogAction.runCommand();
                    }
                }
            }.start();
        }
    }

    public static void SetState(String keyName, boolean status, boolean isLastOption) {
        if (keyName == null) {
            Log.e("SetState(String,boolean,boolean):void", "Do NOT give me null");
        } else if (keyName.equals("\n")) {
            if (status) {
                SetState(keyName, "enable", true);
            } else {
                SetState(keyName, "disable", true);
            }
        } else if (!keyName.equals("logpath\t")) {
            SetState(keyName, status ? "on" : "off", isLastOption);
        } else if (status) {
            SetState(keyName, "external", true);
        } else {
            SetState(keyName, "internal", true);
        }
    }

    public static void SetState(int otherCase, boolean status) {
        switch (otherCase) {
            case 101:
                Message msg = new Message();
                msg.what = 101;
                LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
                SetState("stream\tsystem\t", status, false);
                SetState("stream\tkernel\t", status, false);
                SetState("stream\tradio\t", status, false);
                SetState("stream\tmain\t", status, false);
                return;
            default:
                Log.w("SetState(int,boolean)", "You have given a invalid case");
                return;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void SetState(java.lang.String r19, java.lang.String r20, boolean r21) {
        /*
        r16 = com.spreadtrum.android.eng.SlogAction.class;
        monitor-enter(r16);
        r2 = "SetState(String,String,boolean):void";
        if (r19 != 0) goto L_0x0012;
    L_0x0007:
        r15 = "SetState(String,String,boolean):void";
        r17 = "Do NOT give keyName null";
        r0 = r17;
        android.util.Log.e(r15, r0);	 Catch:{ all -> 0x001e }
    L_0x0010:
        monitor-exit(r16);
        return;
    L_0x0012:
        if (r20 != 0) goto L_0x0021;
    L_0x0014:
        r15 = "SetState(String,String,boolean):void";
        r17 = "Do NOT give status null";
        r0 = r17;
        android.util.Log.e(r15, r0);	 Catch:{ all -> 0x001e }
        goto L_0x0010;
    L_0x001e:
        r15 = move-exception;
        monitor-exit(r16);
        throw r15;
    L_0x0021:
        r15 = com.spreadtrum.android.eng.LogSettingSlogUITabHostActivity.mTabHostHandler;	 Catch:{ all -> 0x001e }
        if (r15 == 0) goto L_0x0033;
    L_0x0025:
        r11 = new android.os.Message;	 Catch:{ all -> 0x001e }
        r11.<init>();	 Catch:{ all -> 0x001e }
        r15 = 15;
        r11.what = r15;	 Catch:{ all -> 0x001e }
        r15 = com.spreadtrum.android.eng.LogSettingSlogUITabHostActivity.mTabHostHandler;	 Catch:{ all -> 0x001e }
        r15.sendMessage(r11);	 Catch:{ all -> 0x001e }
    L_0x0033:
        r4 = 0;
        r8 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r15 = "/data/local/tmp/slog/slog.conf";
        r8.<init>(r15);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r15 = r8.available();	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r3 = new byte[r15];	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r8.read(r3);	 Catch:{ Exception -> 0x0089, FileNotFoundException -> 0x00ab }
        r8.close();	 Catch:{ Exception -> 0x0089, FileNotFoundException -> 0x00ab }
        r5 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r15 = "UTF-8";
        r15 = org.apache.http.util.EncodingUtils.getString(r3, r15);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r5.<init>(r15);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r0 = r19;
        r14 = r5.indexOf(r0);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        if (r14 >= 0) goto L_0x00ad;
    L_0x005a:
        r15 = "SlogAction";
        r17 = "start index < 0, reset slog.conf";
        r0 = r17;
        android.util.Log.e(r15, r0);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        resetSlogConf();	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        goto L_0x0010;
    L_0x0067:
        r7 = move-exception;
        r4 = r5;
    L_0x0069:
        r15 = "SlogAction";
        r17 = "Init FileInputStream failed, reset slog.conf";
        r0 = r17;
        android.util.Log.e(r15, r0);	 Catch:{ all -> 0x001e }
        resetSlogConf();	 Catch:{ all -> 0x001e }
    L_0x0075:
        r12 = new com.spreadtrum.android.eng.SlogAction$RunThread;	 Catch:{ all -> 0x001e }
        r15 = 0;
        r12.<init>();	 Catch:{ all -> 0x001e }
        r13 = new java.lang.Thread;	 Catch:{ all -> 0x001e }
        r15 = 0;
        r17 = "RunThread";
        r0 = r17;
        r13.<init>(r15, r12, r0);	 Catch:{ all -> 0x001e }
        r13.start();	 Catch:{ all -> 0x001e }
        goto L_0x0010;
    L_0x0089:
        r6 = move-exception;
        r8.close();	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r15 = "SetState(String,String,boolean):void";
        r17 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r17.<init>();	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r18 = "Reading file failed,now close,log:\n";
        r17 = r17.append(r18);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r0 = r17;
        r17 = r0.append(r6);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r17 = r17.toString();	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        r0 = r17;
        android.util.Log.e(r15, r0);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x012b }
        goto L_0x0010;
    L_0x00ab:
        r7 = move-exception;
        goto L_0x0069;
    L_0x00ad:
        r10 = r19.length();	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r17 = r14 + r10;
        if (r21 == 0) goto L_0x0128;
    L_0x00b5:
        r15 = "\n";
    L_0x00b7:
        r18 = r14 + r10;
        r18 = r18 + 1;
        r0 = r18;
        r15 = r5.indexOf(r15, r0);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r0 = r17;
        r1 = r20;
        r5.replace(r0, r15, r1);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r3 = 0;
        r9 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r15 = "/data/local/tmp/slog/slog.conf";
        r9.<init>(r15);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r15 = r5.toString();	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r17 = "UTF-8";
        r0 = r17;
        r3 = r15.getBytes(r0);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r9.write(r3);	 Catch:{ Exception -> 0x00e3, FileNotFoundException -> 0x0067 }
        r9.close();	 Catch:{ Exception -> 0x00e3, FileNotFoundException -> 0x0067 }
        goto L_0x0075;
    L_0x00e3:
        r6 = move-exception;
        r9.close();	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r15 = "SetState(String,String,boolean):void";
        r17 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r17.<init>();	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r18 = "Writing file failed,now close,log:\n";
        r17 = r17.append(r18);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r0 = r17;
        r17 = r0.append(r6);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r17 = r17.toString();	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        r0 = r17;
        android.util.Log.e(r15, r0);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x0105 }
        goto L_0x0010;
    L_0x0105:
        r6 = move-exception;
        r4 = r5;
    L_0x0107:
        r6.printStackTrace();	 Catch:{ all -> 0x001e }
        r15 = "SetState(String,String,boolean):void";
        r17 = new java.lang.StringBuilder;	 Catch:{ all -> 0x001e }
        r17.<init>();	 Catch:{ all -> 0x001e }
        r18 = "Catch Excepton,log:\n";
        r17 = r17.append(r18);	 Catch:{ all -> 0x001e }
        r0 = r17;
        r17 = r0.append(r6);	 Catch:{ all -> 0x001e }
        r17 = r17.toString();	 Catch:{ all -> 0x001e }
        r0 = r17;
        android.util.Log.e(r15, r0);	 Catch:{ all -> 0x001e }
        goto L_0x0010;
    L_0x0128:
        r15 = "\t";
        goto L_0x00b7;
    L_0x012b:
        r6 = move-exception;
        goto L_0x0107;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.spreadtrum.android.eng.SlogAction.SetState(java.lang.String, java.lang.String, boolean):void");
    }

    public static void setAllStates(Context context, int id) {
        Cursor c = context.getContentResolver().query(ContentUris.withAppendedId(SlogProvider.URI_ID_MODES, (long) id), null, null, null, null);
        if (c.moveToNext()) {
            setAllStatesWithCursor(c);
        }
        c.close();
    }

    public static void setAllStatesWithCursor(Cursor cursor) {
        boolean z;
        boolean z2 = false;
        SetState("\n", cursor.getString(cursor.getColumnIndex("general")), true);
        SetState("stream\tkernel\t", cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_KERNEL)) == 1, false);
        String str = "stream\tmain\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_MAIN)) == 1) {
            z = true;
        } else {
            z = false;
        }
        SetState(str, z, false);
        str = "stream\tradio\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_RADIO)) == 1) {
            z = true;
        } else {
            z = false;
        }
        SetState(str, z, false);
        str = "stream\tsystem\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_SYSTEM)) == 1) {
            z = true;
        } else {
            z = false;
        }
        SetState(str, z, false);
        str = "stream\tmodem\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_MODEM)) == 1) {
            z = true;
        } else {
            z = false;
        }
        SetState(str, z, false);
        str = "stream\ttcp\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_TCP)) == 1) {
            z = true;
        } else {
            z = false;
        }
        SetState(str, z, false);
        str = "stream\tbt\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_BLUETOOTH)) == 1) {
            z = true;
        } else {
            z = false;
        }
        SetState(str, z, false);
        str = "misc\tmisc\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_MISC)) == 1) {
            z = true;
        } else {
            z = false;
        }
        SetState(str, z, false);
        str = "var\tslogsaveall\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_CLEAR_AUTO)) == 1) {
            z = true;
        } else {
            z = false;
        }
        SetState(str, z, true);
        String str2 = "logpath\t";
        if (cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_STORAGE)) == 1) {
            z2 = true;
        }
        SetState(str2, z2, true);
    }

    public static void saveAsNewMode(String name, Context context) {
        ContentValues values = getAllStatesInContentValues();
        values.put("name", name);
        context.getContentResolver().insert(SlogProvider.URI_MODES, values);
    }

    public static void updateMode(Context context, int id) {
        context.getContentResolver().update(ContentUris.withAppendedId(SlogProvider.URI_ID_MODES, (long) id), getAllStatesInContentValues(), null, null);
    }

    public static void deleteMode(Context context, int id) {
        context.getContentResolver().delete(ContentUris.withAppendedId(SlogProvider.URI_ID_MODES, (long) id), null, null);
    }

    private static ContentValues getAllStatesInContentValues() {
        int i;
        int i2 = 1;
        ContentValues cv = new ContentValues();
        cv.put("general", GetState("\n", true));
        cv.put(Contract.COLUMN_KERNEL, Integer.valueOf(GetState("stream\tkernel\t") ? 1 : 0));
        String str = Contract.COLUMN_MAIN;
        if (GetState("stream\tmain\t")) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        str = Contract.COLUMN_RADIO;
        if (GetState("stream\tradio\t")) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        str = Contract.COLUMN_SYSTEM;
        if (GetState("stream\tsystem\t")) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        str = Contract.COLUMN_MODEM;
        if (GetState("stream\tmodem\t")) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        str = Contract.COLUMN_TCP;
        if (GetState("stream\ttcp\t")) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        str = Contract.COLUMN_BLUETOOTH;
        if (GetState("stream\tbt\t")) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        str = Contract.COLUMN_CLEAR_AUTO;
        if (GetState("var\tslogsaveall\t")) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        str = Contract.COLUMN_MISC;
        if (GetState("misc\tmisc\t")) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        String str2 = Contract.COLUMN_STORAGE;
        if (!GetState("logpath\t")) {
            i2 = 0;
        }
        cv.put(str2, Integer.valueOf(i2));
        return cv;
    }

    private static String getExternalStorageState() {
        try {
            return Stub.asInterface(ServiceManager.getService("mount")).getVolumeState(EXTERNAL_STORAGE_DIRECTORY.toString());
        } catch (Exception e) {
            return "removed";
        }
    }

    private static String getMainStoragePath() {
        try {
            switch (Integer.parseInt(System.getenv("SECOND_STORAGE_TYPE"))) {
                case 0:
                    return "EXTERNAL_STORAGE";
                case 1:
                    return "EXTERNAL_STORAGE";
                case 2:
                    return "SECONDARY_STORAGE";
                default:
                    Log.e("SlogUI", "Please check \"SECOND_STORAGE_TYPE\" 'S value after parse to int in System.getenv for framework");
                    if (MMC_SUPPORT) {
                        return "SECONDARY_SOTRAGE";
                    }
                    return "EXTERNAL_STORAGE";
            }
        } catch (Exception parseError) {
            Log.e("SlogUI", "Parsing SECOND_STORAGE_TYPE crashed.\n" + parseError);
            if (MMC_SUPPORT) {
                return "SECONDARY_SOTRAGE";
            }
            return "EXTERNAL_STORAGE";
        }
    }

    private static File getDirectory(String variableName, String defaultPath) {
        String path = System.getenv(variableName);
        return path == null ? new File(defaultPath) : new File(path);
    }

    private static File getExternalStorage() {
        return EXTERNAL_STORAGE_DIRECTORY;
    }

    public static boolean IsHaveSDCard() {
        if (Environment.getExternalStorageState() != null && getExternalStorageState() != null) {
            return getExternalStorageState().equals("mounted");
        }
        Log.e("SlogUI.isHaveSDCard():boolean", "Your enviroment has something wrong,please check.\nReason:android.os.Environment.getExternalStorageState()==null");
        return false;
    }

    public static long GetFreeSpace(IMediaContainerService imcs, String storageLocation) {
        String MethodName = "GetFreeSpace(String):long";
        if (Environment.getExternalStorageDirectory() == null || Environment.getDataDirectory() == null || EXTERNAL_STORAGE_DIRECTORY == null) {
            Log.e("GetFreeSpace(String):long", "Your environment has problem,please check");
            return 0;
        } else if (storageLocation == null) {
            Log.e("GetFreeSpace(String):long", "Do NOT give storageLocation null");
            return 0;
        } else {
            long size = 0;
            File path;
            StatFs statFs;
            if (storageLocation.equals("external")) {
                path = EXTERNAL_STORAGE_DIRECTORY;
                if (path != null) {
                    statFs = new StatFs(path.getPath());
                    size = ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
                }
            } else {
                path = Environment.getDataDirectory();
                if (path != null) {
                    statFs = new StatFs(path.getPath());
                    size = ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
                }
            }
            return (size / 1024) / 1024;
        }
    }

    public static void SetCheckBoxBranchState(CheckBox tempCheckBox, boolean tempHost, boolean tempBranch) {
        String MethodName = "SetCheckBoxBranchState(CheckBox,boolean,boolean):void";
        if (tempCheckBox == null) {
            Log.e("SetCheckBoxBranchState(CheckBox,boolean,boolean):void", "Do NOT give checkbox null");
        } else if (tempHost) {
            tempCheckBox.setEnabled(tempHost);
            tempCheckBox.setChecked(tempBranch);
        } else {
            tempCheckBox.setEnabled(tempHost);
        }
    }

    public static boolean isAlwaysRun(String keyName) {
        Exception e;
        FileOutputStream fwriter;
        if (keyName == null) {
            return false;
        }
        if (!keyName.equals("slogsvc.conf") && !keyName.equals("snapsvc.conf")) {
            return false;
        }
        FileInputStream freader = null;
        try {
            FileInputStream freader2 = new FileInputStream("/data/data/com.spreadtrum.android.eng/files/" + keyName);
            try {
                byte[] buffer = new byte[freader2.available()];
                freader2.read(buffer);
                freader2.close();
                if (new String(EncodingUtils.getString(buffer, "UTF-8")).trim().equals(String.valueOf(true))) {
                    return true;
                }
                return false;
            } catch (Exception e2) {
                e = e2;
                freader = freader2;
                if (freader != null) {
                    try {
                        freader.close();
                    } catch (IOException ioException) {
                        Log.e("isAlwaysRun(String):boolean", "Freader close error" + ioException);
                        return false;
                    }
                }
                System.err.println("Maybe it is first Run,now try to create one.\n" + e);
                fwriter = null;
                try {
                    fwriter = contextMainActivity.openFileOutput(keyName, 0);
                    fwriter.write(String.valueOf(false).toString().getBytes("UTF-8"));
                    fwriter.close();
                    System.err.println("--->SetAlways Run failed,logs are followed:<---");
                    System.err.println(e);
                    return false;
                } catch (Exception e1) {
                    System.err.println("No!! Create file failed, logs followed\n" + e1);
                    if (fwriter == null) {
                        return false;
                    }
                    try {
                        fwriter.close();
                        return false;
                    } catch (IOException e3) {
                        return false;
                    }
                }
            }
        } catch (Exception e4) {
            e = e4;
            if (freader != null) {
                freader.close();
            }
            System.err.println("Maybe it is first Run,now try to create one.\n" + e);
            fwriter = null;
            fwriter = contextMainActivity.openFileOutput(keyName, 0);
            fwriter.write(String.valueOf(false).toString().getBytes("UTF-8"));
            fwriter.close();
            System.err.println("--->SetAlways Run failed,logs are followed:<---");
            System.err.println(e);
            return false;
        }
    }

    public static void setAlwaysRun(String keyService, boolean isChecked) {
        FileOutputStream fwriter = null;
        try {
            fwriter = contextMainActivity.openFileOutput(keyService, 0);
            fwriter.write(String.valueOf(isChecked).toString().getBytes("UTF-8"));
            fwriter.close();
            if (fwriter != null) {
                try {
                    fwriter.close();
                } catch (IOException e1) {
                    Log.e("SetAlwaysRun(String,boolean):void", "try to close fwriter failed" + e1);
                }
            }
        } catch (Exception e12) {
            Log.e("SetAlwaysRun", "Write file failed, see logs\n" + e12);
            if (fwriter != null) {
                try {
                    fwriter.close();
                } catch (IOException e13) {
                    Log.e("SetAlwaysRun(String,boolean):void", "try to close fwriter failed" + e13);
                }
            }
        } catch (Throwable th) {
            if (fwriter != null) {
                try {
                    fwriter.close();
                } catch (IOException e132) {
                    Log.e("SetAlwaysRun(String,boolean):void", "try to close fwriter failed" + e132);
                }
            }
        }
    }

    public static void ClearLog() {
        Message msg = new Message();
        msg.what = 19;
        LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
        new Thread(null, new ClearThread(), "clearThread").start();
    }

    private static void runClearLog() {
        Message msg = new Message();
        msg.what = 20;
        if (runSlogCommand("slogctl clear") != 0) {
            msg.what = 25;
        }
        try {
            LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
        } catch (Exception e) {
            Log.e("SlogAction", "Clear log failed, sendMessage failed");
        }
    }

    private static void registBroadcast(Context context, String command) {
        if (command == null) {
            Log.e("SlogAction", "Failed registBroadcast, command == null");
        } else if ("synchronism check".equals(command)) {
            try {
                mContext = context.createPackageContext("com.spreadtrum.android.eng", 2);
                IntentFilter filterConn = new IntentFilter();
                filterConn.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                mContext.registerReceiver(mNetReceiver, filterConn);
            } catch (NameNotFoundException e) {
                Log.e("SlogAction", "NameNotFoundException " + e);
            }
        } else if ("synchronism download".equals(command)) {
            try {
                if (mContext == null) {
                    mContext = context.createPackageContext("com.spreadtrum.android.eng", 2);
                }
                IntentFilter filterInstall = new IntentFilter();
                filterInstall.addAction("android.intent.action.PACKAGE_ADDED");
                filterInstall.addDataScheme("package");
                mContext.registerReceiver(mInstallReceiver, filterInstall);
            } catch (NameNotFoundException e2) {
                Log.e("SlogAction", "NameNotFoundException " + e2);
            }
        } else {
            Log.e("SlogAction", "Failed registBroadcast, unknown command " + command);
        }
    }

    private static void runDump(String filename) {
        String NowMethodName = "SlogUIDump";
        Message msg = new Message();
        msg.what = 18;
        if (filename == null) {
            Log.e("SlogUIDump", "Do NOT give me null");
            msg.what = 24;
            LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
            return;
        }
        if (runSlogCommand("slogctl dump  " + filename) != 0) {
            msg.what = 24;
        }
        try {
            LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
        } catch (Exception e) {
            Log.e("SlogAction", "Failed to send message!");
        }
    }

    public static void dump(String filename) {
        if (filename == null) {
            Log.e("SlogUIDump()", "Do not give nulll");
            return;
        }
        Message msg = new Message();
        msg.what = 17;
        LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(msg);
        new Thread(null, new DumpThread(filename), "DumpThread").start();
    }

    public static String decodeInputStream(InputStream input) {
        if (input == null) {
            Log.e("SlogAction", "decode error, InputStream is null.");
            return "decode error";
        }
        try {
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();
            String result = EncodingUtils.getString(buffer, "UTF-8");
            if (result != null) {
                return result;
            }
            Log.e("SlogAction", "decode error, result == null.");
            return "decode error";
        } catch (IOException ioe) {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    Log.e("SlogAction", "Close file failed, \n" + e.toString());
                }
            }
            Log.e("SlogAction", "decode error, see log:" + ioe.toString());
            return "decode error";
        }
    }

    public static boolean sendATCommand(int i, boolean z) {
        synchronized (mLock) {
            if (z) {
                OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                try {
                    String str = "%d,%d,%d";
                    Object[] objArr = new Object[3];
                    objArr[0] = Integer.valueOf(i);
                    objArr[1] = Integer.valueOf(1);
                    objArr[2] = Integer.valueOf(z ? 1 : 1);
                    dataOutputStream.writeBytes(String.format(str, objArr));
                    engfetch com_spreadtrum_android_eng_engfetch = new engfetch();
                    int engopen = com_spreadtrum_android_eng_engfetch.engopen();
                    com_spreadtrum_android_eng_engfetch.engwrite(engopen, byteArrayOutputStream.toByteArray(), byteArrayOutputStream.toByteArray().length);
                    byte[] bArr = new byte[512];
                    String str2 = new String(bArr, 0, com_spreadtrum_android_eng_engfetch.engread(engopen, bArr, 512));
                    if ("OK".equals(str2)) {
                        return true;
                    } else if ("Unknown".equals(str2)) {
                        Log.w("SlogUI", "ATCommand has catch a \"Unknow\" command!");
                        return false;
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    Log.e("SlogAction", "IOException has catched, see logs " + e);
                    return false;
                }
            }
            return false;
        }
    }

    private static int runSlogCommand(String str) {
        int i = -126;
        if (str == null) {
            Log.e("SlogAction", "runSlogCommand catch null command.");
        } else {
            Log.i("SlogAction", "runSlogCommand command=" + str);
            try {
                Process exec = Runtime.getRuntime().exec(str);
                exec.waitFor();
                Log.d("SlogAction", decodeInputStream(exec.getInputStream()));
                i = exec.exitValue();
            } catch (IOException e) {
                Log.e("SlogAction", "Catch IOException.\n" + e.toString());
            } catch (InterruptedException e2) {
                Log.e("SlogAction", "Catch InterruptedException.\n" + e2.toString());
            } catch (Exception e3) {
                Log.e("SlogAction", "Catch InterruptedException.\n" + e3.toString());
            }
        }
        return i;
    }

    public static void SlogStart(Context context) {
        Object obj = null;
        int runSlogCommand = runSlogCommand("synchronism check");
        runSlogCommand("slog");
        try {
            context.getPackageManager().getPackageInfo("com.android.synchronism", 0);
        } catch (NameNotFoundException e) {
            Log.e("SlogAction", "The package was not installed.Set is dirty");
            obj = 1;
        }
        if (obj == null && runSlogCommand == 0) {
            SystemProperties.set("persist.sys.synchronism.exist", "1");
            Log.i("SlogAction", "Check ok, set persist.sys.synchronism.exist = true");
        } else if (255 == runSlogCommand) {
            Log.i("SlogAction", "Check failed, set persist.sys.synchronism.exist = false");
            SystemProperties.set("persist.sys.synchronism.exist", "0");
            registBroadcast(context, "synchronism check");
        } else {
            Log.i("SlogAction", "Check catch exception, set persist.sys.synchronism = false");
            SystemProperties.set("persist.sys.synchronism", "0");
            Log.e("SlogAction", "Failed checking. Stop");
        }
    }

    public static void SlogStart() {
    }

    private static void runCommand() {
        Message message = new Message();
        message.what = 16;
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Process exec = Runtime.getRuntime().exec("slogctl reload");
            try {
                if (exec.waitFor() != 0) {
                    System.err.println("Exit value=" + exec.exitValue());
                }
            } catch (InterruptedException e2) {
                System.err.println(e2);
            }
            if (LogSettingSlogUITabHostActivity.mTabHostHandler != null) {
                LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(message);
            }
        } catch (Exception e3) {
            Log.e("run", "slogctl reload has Exception, log followed\n" + e3);
            if (LogSettingSlogUITabHostActivity.mTabHostHandler != null) {
                LogSettingSlogUITabHostActivity.mTabHostHandler.sendMessage(message);
            }
        }
    }

    public static void snap(Context context) {
        new AnonymousClass1SnapThread(context).start();
    }

    private static void scanScreenShotFile(File file, Context context) {
        if (file == null) {
            Log.i("SlogAction", "scanFailed!");
        } else if (!"last_log".equals(file.getName())) {
            if (file.isDirectory()) {
                for (File scanScreenShotFile : file.listFiles()) {
                    scanScreenShotFile(scanScreenShotFile, context);
                }
            }
            if (file.getName().endsWith("jpg")) {
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
            }
        }
    }
}
