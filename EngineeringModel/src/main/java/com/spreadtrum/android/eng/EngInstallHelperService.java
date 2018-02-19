package com.spreadtrum.android.eng;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageInstallObserver;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class EngInstallHelperService extends Service {
    private static final boolean AOTA_ALLOW = SystemProperties.getBoolean("persist.sys.synchronism.support", true);
    private static final boolean AOTA_ENABLE = SystemProperties.getBoolean("persist.sys.synchronism.enable", true);
    private static final boolean AOTA_SILENT = SystemProperties.getBoolean("persist.sys.silent.install", true);
    private static Object mLock = new Object();
    private static int mOperationCode = 255;
    private Context mContext = this;
    private LocalServerSocket mSocketServer = null;

    private class AotaSocketServerRunnable implements Runnable {

        class LocalSocketRunnable implements Runnable {
            LocalSocket client = null;

            class PackageDeleteObserver extends Stub {
                BufferedReader mBuf;
                PrintStream mOut;

                PackageDeleteObserver(PrintStream out, BufferedReader buf) {
                    this.mOut = out;
                    this.mBuf = buf;
                }

                public void packageDeleted(String packageName, int returnCode) {
                    LocalSocketRunnable.this.writeMessage(this.mOut, this.mBuf, String.valueOf(returnCode));
                }
            }

            class PackageInstallObserver extends IPackageInstallObserver.Stub {
                BufferedReader mBuf;
                PrintStream mOut;

                PackageInstallObserver(PrintStream out, BufferedReader buf) {
                    this.mOut = out;
                    this.mBuf = buf;
                }

                public void packageInstalled(String packageName, int returnCode) {
                    LocalSocketRunnable.this.writeMessage(this.mOut, this.mBuf, String.valueOf(returnCode));
                }
            }

            LocalSocketRunnable(LocalSocket ls) {
                this.client = ls;
            }

            boolean writeMessage(PrintStream out, BufferedReader buf, String message) {
                if (!(out == null || buf == null || message == null)) {
                    Log.d("EngInstallHelperService", "message=" + message);
                    try {
                        out.println(message);
                        out.close();
                        buf.close();
                        this.client.close();
                        EngInstallHelperService.mOperationCode = 255;
                    } catch (Exception ioe) {
                        out.close();
                        Log.e("EngInstallHelperService", "writeMessage catch IOException\n" + ioe);
                        try {
                            out.close();
                            buf.close();
                            this.client.close();
                        } catch (Exception e) {
                            Log.e("EngInstallHelperService", "close socket failed" + e);
                        }
                        return false;
                    }
                }
                return true;
            }

            String readMessage(BufferedReader buf) {
                if (buf != null) {
                    try {
                        return buf.readLine();
                    } catch (IOException e) {
                        try {
                            buf.close();
                        } catch (Exception e2) {
                            Log.e("EngInstallHelperService", "close failed:120");
                        }
                        return "e";
                    }
                }
                Log.e("EngInstallHelperService", "buf == null");
                return "e";
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                /*
                r9 = this;
                r2 = com.spreadtrum.android.eng.EngInstallHelperService.mLock;
                monitor-enter(r2);
                r0 = r9.client;	 Catch:{ all -> 0x0041 }
                if (r0 != 0) goto L_0x000b;
            L_0x0009:
                monitor-exit(r2);	 Catch:{ all -> 0x0041 }
            L_0x000a:
                return;
            L_0x000b:
                r3 = new java.io.PrintStream;	 Catch:{ Exception -> 0x0044 }
                r0 = r9.client;	 Catch:{ Exception -> 0x0044 }
                r0 = r0.getOutputStream();	 Catch:{ Exception -> 0x0044 }
                r3.<init>(r0);	 Catch:{ Exception -> 0x0044 }
                r4 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x0044 }
                r0 = new java.io.InputStreamReader;	 Catch:{ Exception -> 0x0044 }
                r1 = r9.client;	 Catch:{ Exception -> 0x0044 }
                r1 = r1.getInputStream();	 Catch:{ Exception -> 0x0044 }
                r0.<init>(r1);	 Catch:{ Exception -> 0x0044 }
                r4.<init>(r0);	 Catch:{ Exception -> 0x0044 }
                r0 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
                r1 = r9.readMessage(r4);	 Catch:{ all -> 0x0041 }
                r0.<init>(r1);	 Catch:{ all -> 0x0041 }
                if (r0 == 0) goto L_0x0038;
            L_0x0031:
                r1 = r0.length();	 Catch:{ all -> 0x0041 }
                r5 = 3;
                if (r1 >= r5) goto L_0x0071;
            L_0x0038:
                r0 = r0.toString();	 Catch:{ all -> 0x0041 }
                r9.writeMessage(r3, r4, r0);	 Catch:{ all -> 0x0041 }
                monitor-exit(r2);	 Catch:{ all -> 0x0041 }
                goto L_0x000a;
            L_0x0041:
                r0 = move-exception;
                monitor-exit(r2);	 Catch:{ all -> 0x0041 }
                throw r0;
            L_0x0044:
                r0 = move-exception;
                r1 = "EngInstallHelperService";
                r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
                r3.<init>();	 Catch:{ all -> 0x0041 }
                r4 = "PrintStream or BufferedRead init failed\n";
                r3 = r3.append(r4);	 Catch:{ all -> 0x0041 }
                r0 = r0.toString();	 Catch:{ all -> 0x0041 }
                r0 = r3.append(r0);	 Catch:{ all -> 0x0041 }
                r0 = r0.toString();	 Catch:{ all -> 0x0041 }
                android.util.Log.e(r1, r0);	 Catch:{ all -> 0x0041 }
                r0 = r9.client;	 Catch:{ Exception -> 0x0068 }
                r0.close();	 Catch:{ Exception -> 0x0068 }
            L_0x0066:
                monitor-exit(r2);	 Catch:{ all -> 0x0041 }
                goto L_0x000a;
            L_0x0068:
                r0 = move-exception;
                r0 = "EngInstallHelperService";
                r1 = "close failed";
                android.util.Log.e(r0, r1);	 Catch:{ all -> 0x0041 }
                goto L_0x0066;
            L_0x0071:
                r1 = 0;
                r5 = "EngInstallHelperService";
                r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
                r6.<init>();	 Catch:{ all -> 0x0041 }
                r7 = "get message";
                r6 = r6.append(r7);	 Catch:{ all -> 0x0041 }
                r7 = r0.toString();	 Catch:{ all -> 0x0041 }
                r6 = r6.append(r7);	 Catch:{ all -> 0x0041 }
                r6 = r6.toString();	 Catch:{ all -> 0x0041 }
                android.util.Log.e(r5, r6);	 Catch:{ all -> 0x0041 }
                r5 = r0.toString();	 Catch:{ all -> 0x0041 }
                r5 = r5.length();	 Catch:{ all -> 0x0041 }
                r5 = new char[r5];	 Catch:{ all -> 0x0041 }
                r6 = 0;
                r7 = 2;
                r8 = 0;
                r0.getChars(r6, r7, r5, r8);	 Catch:{ all -> 0x0041 }
                r6 = "1";
                r7 = 0;
                r7 = r5[r7];	 Catch:{ all -> 0x0041 }
                r7 = java.lang.String.valueOf(r7);	 Catch:{ all -> 0x0041 }
                r6 = r6.equals(r7);	 Catch:{ all -> 0x0041 }
                if (r6 == 0) goto L_0x01ea;
            L_0x00ad:
                r1 = 0;
                r6 = 2;
                r0 = r0.delete(r1, r6);	 Catch:{ all -> 0x0041 }
                r0 = r0.toString();	 Catch:{ all -> 0x0041 }
                r0 = r0.trim();	 Catch:{ all -> 0x0041 }
                r1 = "EngInstallHelperService";
                r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
                r6.<init>();	 Catch:{ all -> 0x0041 }
                r7 = "message trimed =";
                r6 = r6.append(r7);	 Catch:{ all -> 0x0041 }
                r6 = r6.append(r0);	 Catch:{ all -> 0x0041 }
                r6 = r6.toString();	 Catch:{ all -> 0x0041 }
                android.util.Log.i(r1, r6);	 Catch:{ all -> 0x0041 }
                r1 = "persist.sys.silent.install";
                r6 = 1;
                r1 = android.os.SystemProperties.getBoolean(r1, r6);	 Catch:{ all -> 0x0041 }
                r6 = "0";
                r7 = 1;
                r7 = r5[r7];	 Catch:{ all -> 0x0041 }
                r7 = java.lang.String.valueOf(r7);	 Catch:{ all -> 0x0041 }
                r6 = r6.equals(r7);	 Catch:{ all -> 0x0041 }
                if (r6 == 0) goto L_0x0165;
            L_0x00e9:
                if (r1 == 0) goto L_0x0109;
            L_0x00eb:
                r1 = com.spreadtrum.android.eng.EngInstallHelperService.AotaSocketServerRunnable.this;	 Catch:{ all -> 0x0041 }
                r1 = com.spreadtrum.android.eng.EngInstallHelperService.this;	 Catch:{ all -> 0x0041 }
                r1 = r1.mContext;	 Catch:{ all -> 0x0041 }
                r1 = r1.getPackageManager();	 Catch:{ all -> 0x0041 }
                r0 = android.net.Uri.parse(r0);	 Catch:{ all -> 0x0041 }
                r5 = new com.spreadtrum.android.eng.EngInstallHelperService$AotaSocketServerRunnable$LocalSocketRunnable$PackageInstallObserver;	 Catch:{ all -> 0x0041 }
                r5.<init>(r3, r4);	 Catch:{ all -> 0x0041 }
                r3 = 2;
                r4 = "com.spreadtrum.android.eng";
                r1.installPackage(r0, r5, r3, r4);	 Catch:{ all -> 0x0041 }
            L_0x0106:
                monitor-exit(r2);	 Catch:{ all -> 0x0041 }
                goto L_0x000a;
            L_0x0109:
                r1 = new android.content.Intent;	 Catch:{ all -> 0x0041 }
                r5 = com.spreadtrum.android.eng.EngInstallHelperService.AotaSocketServerRunnable.this;	 Catch:{ all -> 0x0041 }
                r5 = com.spreadtrum.android.eng.EngInstallHelperService.this;	 Catch:{ all -> 0x0041 }
                r5 = r5.mContext;	 Catch:{ all -> 0x0041 }
                r6 = com.spreadtrum.android.eng.EngInstallActivity.class;
                r1.<init>(r5, r6);	 Catch:{ all -> 0x0041 }
                r5 = "name";
                r1.putExtra(r5, r0);	 Catch:{ all -> 0x0041 }
                r0 = "action";
                r5 = 10;
                r1.putExtra(r0, r5);	 Catch:{ all -> 0x0041 }
                r0 = 276824064; // 0x10800000 float:5.0487098E-29 double:1.3676926E-315;
                r1.addFlags(r0);	 Catch:{ all -> 0x0041 }
                r0 = com.spreadtrum.android.eng.EngInstallHelperService.AotaSocketServerRunnable.this;	 Catch:{ all -> 0x0041 }
                r0 = com.spreadtrum.android.eng.EngInstallHelperService.this;	 Catch:{ all -> 0x0041 }
                r0 = r0.mContext;	 Catch:{ all -> 0x0041 }
                r0 = r0.getApplicationContext();	 Catch:{ all -> 0x0041 }
                r0.startActivity(r1);	 Catch:{ all -> 0x0041 }
                r0 = com.spreadtrum.android.eng.EngInstallHelperService.mLock;	 Catch:{ Exception -> 0x014b }
                r0.wait();	 Catch:{ Exception -> 0x014b }
            L_0x013f:
                r0 = com.spreadtrum.android.eng.EngInstallHelperService.mOperationCode;	 Catch:{ all -> 0x0041 }
                r0 = java.lang.String.valueOf(r0);	 Catch:{ all -> 0x0041 }
                r9.writeMessage(r3, r4, r0);	 Catch:{ all -> 0x0041 }
                goto L_0x0106;
            L_0x014b:
                r0 = move-exception;
                r1 = "EngInstallHelperService";
                r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
                r5.<init>();	 Catch:{ all -> 0x0041 }
                r6 = "mLock failed wait ";
                r5 = r5.append(r6);	 Catch:{ all -> 0x0041 }
                r0 = r5.append(r0);	 Catch:{ all -> 0x0041 }
                r0 = r0.toString();	 Catch:{ all -> 0x0041 }
                android.util.Log.e(r1, r0);	 Catch:{ all -> 0x0041 }
                goto L_0x013f;
            L_0x0165:
                r6 = "1";
                r7 = 1;
                r5 = r5[r7];	 Catch:{ all -> 0x0041 }
                r5 = java.lang.String.valueOf(r5);	 Catch:{ all -> 0x0041 }
                r5 = r6.equals(r5);	 Catch:{ all -> 0x0041 }
                if (r5 == 0) goto L_0x0106;
            L_0x0174:
                if (r1 == 0) goto L_0x018d;
            L_0x0176:
                r1 = com.spreadtrum.android.eng.EngInstallHelperService.AotaSocketServerRunnable.this;	 Catch:{ all -> 0x0041 }
                r1 = com.spreadtrum.android.eng.EngInstallHelperService.this;	 Catch:{ all -> 0x0041 }
                r1 = r1.mContext;	 Catch:{ all -> 0x0041 }
                r1 = r1.getPackageManager();	 Catch:{ all -> 0x0041 }
                r5 = new com.spreadtrum.android.eng.EngInstallHelperService$AotaSocketServerRunnable$LocalSocketRunnable$PackageDeleteObserver;	 Catch:{ all -> 0x0041 }
                r5.<init>(r3, r4);	 Catch:{ all -> 0x0041 }
                r3 = 0;
                r1.deletePackage(r0, r5, r3);	 Catch:{ all -> 0x0041 }
                goto L_0x0106;
            L_0x018d:
                r1 = new android.content.Intent;	 Catch:{ all -> 0x0041 }
                r5 = com.spreadtrum.android.eng.EngInstallHelperService.AotaSocketServerRunnable.this;	 Catch:{ all -> 0x0041 }
                r5 = com.spreadtrum.android.eng.EngInstallHelperService.this;	 Catch:{ all -> 0x0041 }
                r5 = r5.mContext;	 Catch:{ all -> 0x0041 }
                r6 = com.spreadtrum.android.eng.EngInstallActivity.class;
                r1.<init>(r5, r6);	 Catch:{ all -> 0x0041 }
                r5 = "name";
                r1.putExtra(r5, r0);	 Catch:{ all -> 0x0041 }
                r0 = "action";
                r5 = 12;
                r1.putExtra(r0, r5);	 Catch:{ all -> 0x0041 }
                r0 = 276824064; // 0x10800000 float:5.0487098E-29 double:1.3676926E-315;
                r1.addFlags(r0);	 Catch:{ all -> 0x0041 }
                r0 = com.spreadtrum.android.eng.EngInstallHelperService.AotaSocketServerRunnable.this;	 Catch:{ all -> 0x0041 }
                r0 = com.spreadtrum.android.eng.EngInstallHelperService.this;	 Catch:{ all -> 0x0041 }
                r0 = r0.mContext;	 Catch:{ all -> 0x0041 }
                r0 = r0.getApplicationContext();	 Catch:{ all -> 0x0041 }
                r0.startActivity(r1);	 Catch:{ all -> 0x0041 }
                r0 = com.spreadtrum.android.eng.EngInstallHelperService.mLock;	 Catch:{ Exception -> 0x01d0 }
                r0.wait();	 Catch:{ Exception -> 0x01d0 }
            L_0x01c3:
                r0 = com.spreadtrum.android.eng.EngInstallHelperService.mOperationCode;	 Catch:{ all -> 0x0041 }
                r0 = java.lang.String.valueOf(r0);	 Catch:{ all -> 0x0041 }
                r9.writeMessage(r3, r4, r0);	 Catch:{ all -> 0x0041 }
                goto L_0x0106;
            L_0x01d0:
                r0 = move-exception;
                r1 = "EngInstallHelperService";
                r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
                r5.<init>();	 Catch:{ all -> 0x0041 }
                r6 = "mLock failed wait ";
                r5 = r5.append(r6);	 Catch:{ all -> 0x0041 }
                r0 = r5.append(r0);	 Catch:{ all -> 0x0041 }
                r0 = r0.toString();	 Catch:{ all -> 0x0041 }
                android.util.Log.e(r1, r0);	 Catch:{ all -> 0x0041 }
                goto L_0x01c3;
            L_0x01ea:
                r0 = "2";
                r6 = 0;
                r5 = r5[r6];	 Catch:{ all -> 0x0041 }
                r5 = java.lang.String.valueOf(r5);	 Catch:{ all -> 0x0041 }
                r0 = r0.equals(r5);	 Catch:{ all -> 0x0041 }
                if (r0 == 0) goto L_0x0232;
            L_0x01f9:
                r0 = java.lang.Runtime.getRuntime();	 Catch:{ Exception -> 0x0217 }
                r5 = "synchronism download";
                r0 = r0.exec(r5);	 Catch:{ Exception -> 0x0217 }
                r0.waitFor();	 Catch:{ Exception -> 0x0217 }
                r0 = r0.getInputStream();	 Catch:{ Exception -> 0x0217 }
                r0 = com.spreadtrum.android.eng.SlogAction.decodeInputStream(r0);	 Catch:{ Exception -> 0x0217 }
            L_0x020e:
                if (r0 != 0) goto L_0x0212;
            L_0x0210:
                r0 = "e";
            L_0x0212:
                r9.writeMessage(r3, r4, r0);	 Catch:{ all -> 0x0041 }
                goto L_0x0106;
            L_0x0217:
                r0 = move-exception;
                r5 = "EngInstallHelperService";
                r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
                r6.<init>();	 Catch:{ all -> 0x0041 }
                r7 = "exec catch excpetion";
                r6 = r6.append(r7);	 Catch:{ all -> 0x0041 }
                r0 = r6.append(r0);	 Catch:{ all -> 0x0041 }
                r0 = r0.toString();	 Catch:{ all -> 0x0041 }
                android.util.Log.e(r5, r0);	 Catch:{ all -> 0x0041 }
                r0 = r1;
                goto L_0x020e;
            L_0x0232:
                r0 = "b";
                r9.writeMessage(r3, r4, r0);	 Catch:{ all -> 0x0041 }
                goto L_0x0106;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.spreadtrum.android.eng.EngInstallHelperService.AotaSocketServerRunnable.LocalSocketRunnable.run():void");
            }
        }

        private AotaSocketServerRunnable() {
        }

        public void run() {
            while (true) {
                try {
                    new Thread(null, new LocalSocketRunnable(EngInstallHelperService.this.mSocketServer.accept()), "AirpushWorkingThread").start();
                } catch (IOException ioe) {
                    Log.e("EngInstallHelperService", "mSocketServer has catch a IOException\n" + ioe);
                }
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        if (AOTA_ENABLE) {
            try {
                this.mSocketServer = new LocalServerSocket("aotad");
                Log.d("EngInstallHelperService", "start local socket successful");
            } catch (IOException ioException) {
                this.mSocketServer = null;
                Log.e("EngInstallHelperService", "failed start socket server\n " + ioException);
            }
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (AOTA_ENABLE) {
            startAotaSocketServer();
        } else {
            Log.i("EngInstallHelperService", "AOTA is not enable, exit");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startAotaSocketServer() {
        if (this.mSocketServer == null) {
            Log.e("EngInstallHelperService", "socket server haven't init, exit");
            stopSelf();
            return;
        }
        Log.i("EngInstallHelperService", "now starting aotaserver");
        new Thread(null, new AotaSocketServerRunnable(), "AirpushDeamon").start();
    }

    public static void onResult(int returnCode) {
        synchronized (mLock) {
            mOperationCode = returnCode;
            mLock.notify();
        }
    }
}
