package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MmcInfo extends Activity {
    static BufferedWriter bw;
    static BufferedWriter bw2;
    static ArrayList<Integer> crcErrorList = new ArrayList();
    static String errorFilePath = "sys/kernel/debug/sdhci_debug/statistics";
    public static ArrayList<Integer> lastTimeout = new ArrayList();
    public static ArrayList<Integer> lastcrc = new ArrayList();
    static BufferedReader mBufferedReader;
    static FileReader mFileReader;
    static ArrayList<Integer> timeOutErrorList = new ArrayList();
    private TextView mmc0crc;
    private TextView mmc0timeout;
    private TextView mmc1crc;
    private TextView mmc1timeout;
    private TextView mmc2crc;
    private TextView mmc2timeout;
    private TextView mmc3crc;
    private TextView mmc3timeout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mmcinfo);
        Log.i("mmcinfo", "进入12130903065");
        this.mmc0crc = (TextView) findViewById(R.id.mmc0crc);
        this.mmc0timeout = (TextView) findViewById(R.id.mmc0timeout);
        this.mmc1crc = (TextView) findViewById(R.id.mmc1crc);
        this.mmc1timeout = (TextView) findViewById(R.id.mmc1timeout);
        this.mmc2crc = (TextView) findViewById(R.id.mmc2crc);
        this.mmc2timeout = (TextView) findViewById(R.id.mmc2timeout);
        this.mmc3crc = (TextView) findViewById(R.id.mmc3crc);
        this.mmc3timeout = (TextView) findViewById(R.id.mmc3timeout);
        Log.i("mmcinfo", "进入2");
        if (checkMMC()) {
            getLastValue();
            updateValue();
            saveValue();
        }
    }

    private boolean checkMMC() {
        if (new File(errorFilePath).exists()) {
            return true;
        }
        this.mmc0crc.setText("MMC0 CRC:0");
        this.mmc1crc.setText("MMC1 CRC:0");
        this.mmc2crc.setText("MMC2 CRC:0");
        this.mmc3crc.setText("MMC3 CRC:0");
        this.mmc0timeout.setText("MMC0 timeout:0");
        this.mmc1timeout.setText("MMC1 timeout:0");
        this.mmc2timeout.setText("MMC2 timeout:0");
        this.mmc3timeout.setText("MMC3 timeout:0");
        return false;
    }

    private void updateValue() {
        getErrorCount();
        this.mmc0crc.setText("MMC0 CRC:" + (((Integer) crcErrorList.get(0)).intValue() + ((Integer) lastcrc.get(0)).intValue()));
        this.mmc1crc.setText("MMC1 CRC:" + (((Integer) crcErrorList.get(1)).intValue() + ((Integer) lastcrc.get(1)).intValue()));
        this.mmc2crc.setText("MMC2 CRC:" + (((Integer) crcErrorList.get(2)).intValue() + ((Integer) lastcrc.get(2)).intValue()));
        this.mmc3crc.setText("MMC3 CRC:" + (((Integer) crcErrorList.get(3)).intValue() + ((Integer) lastcrc.get(3)).intValue()));
        this.mmc0timeout.setText("MMC0 timeout:" + (((Integer) timeOutErrorList.get(0)).intValue() + ((Integer) lastTimeout.get(0)).intValue()));
        this.mmc1timeout.setText("MMC1 timeout:" + (((Integer) timeOutErrorList.get(1)).intValue() + ((Integer) lastTimeout.get(1)).intValue()));
        this.mmc2timeout.setText("MMC2 timeout:" + (((Integer) timeOutErrorList.get(2)).intValue() + ((Integer) lastTimeout.get(2)).intValue()));
        this.mmc3timeout.setText("MMC3 timeout:" + (((Integer) timeOutErrorList.get(3)).intValue() + ((Integer) lastTimeout.get(3)).intValue()));
    }

    public static void setLastValueWhenBootComplete() {
        Exception e;
        Throwable th;
        while (lastcrc.size() < 4) {
            lastcrc.add(Integer.valueOf(0));
            lastTimeout.add(Integer.valueOf(0));
        }
        String[] value = new String[8];
        BufferedReader br = null;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader("/productinfo/mmcinfo"));
            int i = 0;
            do {
                try {
                    String s = br2.readLine();
                    if (s == null) {
                        break;
                    }
                    value[i] = s;
                    i++;
                } catch (Exception e2) {
                    e = e2;
                    br = br2;
                } catch (Throwable th2) {
                    th = th2;
                    br = br2;
                }
            } while (i <= 9);
            for (i = 0; i < 8; i += 2) {
                lastcrc.set(i / 2, Integer.valueOf(value[i]));
            }
            for (i = 1; i < 8; i += 2) {
                lastTimeout.set(i / 2, Integer.valueOf(value[i]));
            }
            bw2 = new BufferedWriter(new FileWriter("/productinfo/mmcinfo_last"));
            for (i = 0; i < 8; i++) {
                bw2.write(value[i]);
                bw2.newLine();
            }
            bw2.flush();
            if (br2 != null) {
                try {
                    br2.close();
                    br = br2;
                } catch (Exception e3) {
                }
            }
            if (bw2 != null) {
                try {
                    bw2.close();
                } catch (Exception e4) {
                    bw2 = null;
                }
            }
        } catch (Exception e5) {
            e = e5;
            try {
                e.printStackTrace();
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e6) {
                    }
                }
                if (bw2 != null) {
                    try {
                        bw2.close();
                    } catch (Exception e7) {
                        bw2 = null;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e8) {
                    }
                }
                if (bw2 != null) {
                    try {
                        bw2.close();
                    } catch (Exception e9) {
                        bw2 = null;
                    }
                }
                throw th;
            }
        }
    }

    public static void saveValue() {
        getErrorCount();
        try {
            bw = new BufferedWriter(new FileWriter("/productinfo/mmcinfo"));
            for (int i = 0; i < 4; i++) {
                if (crcErrorList.size() > i) {
                    bw.write(String.valueOf(((Integer) lastcrc.get(i)).intValue() + ((Integer) crcErrorList.get(i)).intValue()));
                    bw.newLine();
                }
                if (timeOutErrorList.size() > i) {
                    bw.write(String.valueOf(((Integer) lastTimeout.get(i)).intValue() + ((Integer) timeOutErrorList.get(i)).intValue()));
                    bw.newLine();
                }
            }
            bw.flush();
            bw.close();
            try {
                bw.close();
            } catch (Exception e) {
            }
        } catch (IOException e2) {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (Throwable th) {
                try {
                    bw.close();
                } catch (Exception e3) {
                }
            }
            try {
                bw.close();
            } catch (Exception e4) {
            }
        }
    }

    public static void getErrorCount() {
        crcErrorList.clear();
        timeOutErrorList.clear();
        try {
            mFileReader = new FileReader(errorFilePath);
            mBufferedReader = new BufferedReader(mFileReader, 1024);
            while (true) {
                String line = mBufferedReader.readLine();
                if (line != null) {
                    String[] arrayOfString = line.split("\\,");
                    String[] crcError = arrayOfString[0].split("\\s+");
                    String[] timeOutError = arrayOfString[1].split("\\s+");
                    crcErrorList.add(Integer.valueOf(Integer.parseInt(crcError[4].substring(2), 16)));
                    timeOutErrorList.add(Integer.valueOf(Integer.parseInt(timeOutError[3].substring(2), 16)));
                } else {
                    try {
                        mFileReader.close();
                        mBufferedReader.close();
                        return;
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        } catch (IOException e2) {
            try {
                if (mFileReader != null) {
                    mFileReader.close();
                }
                if (mBufferedReader != null) {
                    mBufferedReader.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (Throwable th) {
                try {
                    mFileReader.close();
                    mBufferedReader.close();
                } catch (Exception e3) {
                }
            }
            try {
                mFileReader.close();
                mBufferedReader.close();
            } catch (Exception e4) {
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void getLastValue() {
        /*
        r10 = 4;
        r8 = 0;
        r9 = 8;
        r6 = lastcrc;
        r6 = r6.isEmpty();
        if (r6 != 0) goto L_0x0014;
    L_0x000c:
        r6 = lastTimeout;
        r6 = r6.isEmpty();
        if (r6 == 0) goto L_0x00a7;
    L_0x0014:
        r6 = lastcrc;
        r6 = r6.size();
        if (r6 >= r10) goto L_0x0026;
    L_0x001c:
        r6 = lastcrc;
        r7 = java.lang.Integer.valueOf(r8);
        r6.add(r7);
        goto L_0x0014;
    L_0x0026:
        r6 = lastTimeout;
        r6 = r6.size();
        if (r6 >= r10) goto L_0x0038;
    L_0x002e:
        r6 = lastTimeout;
        r7 = java.lang.Integer.valueOf(r8);
        r6.add(r7);
        goto L_0x0026;
    L_0x0038:
        r5 = new java.lang.String[r9];
        r0 = 0;
        r1 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x00a8, all -> 0x00b4 }
        r6 = new java.io.FileReader;	 Catch:{ Exception -> 0x00a8, all -> 0x00b4 }
        r7 = "/productinfo/mmcinfo_last";
        r6.<init>(r7);	 Catch:{ Exception -> 0x00a8, all -> 0x00b4 }
        r1.<init>(r6);	 Catch:{ Exception -> 0x00a8, all -> 0x00b4 }
        r3 = 0;
    L_0x0048:
        r4 = r1.readLine();	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        if (r4 == 0) goto L_0x0057;
    L_0x004e:
        if (r3 <= r9) goto L_0x007b;
    L_0x0050:
        r6 = "mmcinfo";
        r7 = "the file format is wrong";
        android.util.Log.e(r6, r7);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
    L_0x0057:
        r3 = 0;
    L_0x0058:
        if (r3 >= r9) goto L_0x0080;
    L_0x005a:
        r6 = lastcrc;	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r7 = r3 / 2;
        r8 = r5[r3];	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r8 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r6.set(r7, r8);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r6 = "mmcinfo";
        r7 = lastcrc;	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r8 = r3 / 2;
        r7 = r7.get(r8);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r7 = java.lang.String.valueOf(r7);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        android.util.Log.e(r6, r7);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r3 = r3 + 2;
        goto L_0x0058;
    L_0x007b:
        r5[r3] = r4;	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r3 = r3 + 1;
        goto L_0x0048;
    L_0x0080:
        r3 = 1;
    L_0x0081:
        if (r3 >= r9) goto L_0x00a4;
    L_0x0083:
        r6 = lastTimeout;	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r7 = r3 / 2;
        r8 = r5[r3];	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r8 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r6.set(r7, r8);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r6 = "mmcinfo";
        r7 = lastTimeout;	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r8 = r3 / 2;
        r7 = r7.get(r8);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r7 = java.lang.String.valueOf(r7);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        android.util.Log.e(r6, r7);	 Catch:{ Exception -> 0x00c2, all -> 0x00bf }
        r3 = r3 + 2;
        goto L_0x0081;
    L_0x00a4:
        r1.close();	 Catch:{ Exception -> 0x00b9 }
    L_0x00a7:
        return;
    L_0x00a8:
        r2 = move-exception;
    L_0x00a9:
        if (r0 == 0) goto L_0x00ae;
    L_0x00ab:
        r0.close();	 Catch:{ IOException -> 0x00bb, all -> 0x00b4 }
    L_0x00ae:
        r0.close();	 Catch:{ Exception -> 0x00b2 }
        goto L_0x00a7;
    L_0x00b2:
        r6 = move-exception;
        goto L_0x00a7;
    L_0x00b4:
        r6 = move-exception;
    L_0x00b5:
        r0.close();	 Catch:{ Exception -> 0x00bd }
    L_0x00b8:
        throw r6;
    L_0x00b9:
        r6 = move-exception;
        goto L_0x00a7;
    L_0x00bb:
        r6 = move-exception;
        goto L_0x00ae;
    L_0x00bd:
        r7 = move-exception;
        goto L_0x00b8;
    L_0x00bf:
        r6 = move-exception;
        r0 = r1;
        goto L_0x00b5;
    L_0x00c2:
        r2 = move-exception;
        r0 = r1;
        goto L_0x00a9;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.spreadtrum.android.eng.MmcInfo.getLastValue():void");
    }
}
