package com.yuneec.flightmode15;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import com.yuneec.IPCameraManager.DES;
import com.yuneec.uartcontroller.GPSUpLinkData;
import com.yuneec.uartcontroller.UARTInfoMessage.Telemetry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FlightLog {
    private static FlightLog instance = null;
    private static int maxNum = 10000;
    private static int reserveNum = 100;
    private String apppath = "sdcard";
    private Runnable closeFlightModeRunnable = new Runnable() {
        public void run() {
            if (!FlightLog.this.isCreatedNode) {
                FlightLog.this.flightNoteClose();
            }
        }
    };
    private Context context;
    private boolean isCreatedNode = false;
    private Handler mHandler = new Handler();
    private FileOutputStream mRemoteFos = null;
    private FileOutputStream mRemoteGpsFos = null;
    private FileOutputStream mTelemetryFos = null;
    private String remFilePath;
    private String remGPSFilePath;
    private String telFilePath;

    private FlightLog() {
    }

    public static FlightLog getInstance() {
        if (instance == null) {
            instance = new FlightLog();
        }
        return instance;
    }

    public synchronized void createFlightNote(Context context) {
        this.context = context;
        this.isCreatedNode = true;
        if (isSdCardExist()) {
            this.apppath = sdCardPath()[0];
            if (this.mRemoteGpsFos == null && this.mRemoteFos == null && this.mTelemetryFos == null) {
                int logFileIndex = 1;
                File blackBox = new File(this.apppath + "/FlightLog");
                if (!blackBox.exists()) {
                    blackBox.mkdir();
                }
                File file = new File(this.apppath + "/FlightLog/Telemetry");
                if (file.exists()) {
                    delOtherFile(this.apppath, file);
                    String[] names = file.list();
                    for (int i = 0; i < names.length; i++) {
                        if (names[i].contains("Telemetry")) {
                            int nameIndex = Integer.parseInt(names[i].substring(10, 15));
                            if (nameIndex > logFileIndex) {
                                logFileIndex = nameIndex;
                            }
                        }
                    }
                    if (logFileIndex == maxNum) {
                        logFileIndex = 1;
                    } else {
                        logFileIndex++;
                    }
                } else {
                    file.mkdir();
                }
                delLogFile(logFileIndex, reserveNum, maxNum);
                this.telFilePath = this.apppath + "/FlightLog/Telemetry/" + ("Telemetry_" + String.format("%05d", new Object[]{Integer.valueOf(logFileIndex)}) + ".csv");
                String title = Telemetry.getParamsName();
                try {
                    this.mTelemetryFos = new FileOutputStream(this.telFilePath);
                    this.mTelemetryFos.write(title.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                File remoteFile = new File(this.apppath + "/FlightLog/Remote");
                if (!remoteFile.exists()) {
                    remoteFile.mkdir();
                }
                this.remFilePath = this.apppath + "/FlightLog/Remote/" + ("Remote_" + String.format("%05d", new Object[]{Integer.valueOf(logFileIndex)}) + ".csv");
                String remTitle = ",CH0,CH1,CH2,CH3,CH4,CH5,CH6,CH7,CH8,CH9,CH10,CH11,CH12,CH13,CH14,CH15,CH16,CH17,CH18,CH19,CH20,CH21,CH22,CH23\n";
                try {
                    this.mRemoteFos = new FileOutputStream(this.remFilePath);
                    this.mRemoteFos.write(remTitle.getBytes());
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                File remoteGPSFile = new File(this.apppath + "/FlightLog/RemoteGPS");
                if (!remoteGPSFile.exists()) {
                    remoteGPSFile.mkdir();
                }
                this.remGPSFilePath = this.apppath + "/FlightLog/RemoteGPS/" + ("RemoteGPS_" + String.format("%05d", new Object[]{Integer.valueOf(logFileIndex)}) + ".csv");
                String remGPSTitle = GPSUpLinkData.getParamsName();
                try {
                    this.mRemoteGpsFos = new FileOutputStream(this.remGPSFilePath);
                    this.mRemoteGpsFos.write(remGPSTitle.getBytes());
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
        }
    }

    public boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    private String[] sdCardPath() {
        String[] paths = null;
        StorageManager sm = (StorageManager) this.context.getSystemService("storage");
        try {
            paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
            int length = paths.length;
            return paths;
        } catch (Exception e) {
            e.printStackTrace();
            return paths;
        }
    }

    public void saveTelemetryFos(byte[] fosData) {
        try {
            if (this.mTelemetryFos != null) {
                this.mTelemetryFos.write(fosData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRemoteFos(byte[] fosData) {
        try {
            if (this.mRemoteFos != null) {
                this.mRemoteFos.write(fosData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRemoteGpsFos(byte[] fosData) {
        try {
            if (this.mRemoteGpsFos != null) {
                this.mRemoteGpsFos.write(fosData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void delLogFile(int logFileIndex, int reserveNum, int maxNum) {
        int delLogFileIndex = logFileIndex - reserveNum;
        if (logFileIndex < reserveNum) {
            delLogFileIndex = (logFileIndex - reserveNum) + maxNum;
        }
        String delTelFileName = this.apppath + File.separator + "FlightLog" + File.separator + "Telemetry" + File.separator + "Telemetry_" + String.format("%05d", new Object[]{Integer.valueOf(delLogFileIndex)}) + ".csv";
        String delRemFileName = this.apppath + File.separator + "FlightLog" + File.separator + "Remote" + File.separator + "Remote_" + String.format("%05d", new Object[]{Integer.valueOf(delLogFileIndex)}) + ".csv";
        String delRemGPSFileName = this.apppath + File.separator + "FlightLog" + File.separator + "RemoteGPS" + File.separator + "RemoteGPS_" + String.format("%05d", new Object[]{Integer.valueOf(delLogFileIndex)}) + ".csv";
        File fileDelTelFileName = new File(delTelFileName);
        if (fileDelTelFileName.exists()) {
            fileDelTelFileName.delete();
        }
        File fileDelRemFileName = new File(delRemFileName);
        if (fileDelRemFileName.exists()) {
            fileDelRemFileName.delete();
        }
        File fileDelRemGPSFileName = new File(delRemGPSFileName);
        if (fileDelRemGPSFileName.exists()) {
            fileDelRemGPSFileName.delete();
        }
    }

    private void delOtherFile(String apppath, File telemtryFile) {
        String[] names = telemtryFile.list();
        for (String fileName : names) {
            String fileLastName = fileName.substring(10, 15);
            if (!fileName.contains("Telemetry_") || !fileLastName.matches("[0-9]+")) {
                File file = new File(new StringBuilder(String.valueOf(apppath)).append("/FlightLog/Telemetry/").append(fileName).toString());
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    public synchronized void closedFlightNote(boolean forceClose) {
        if (forceClose) {
            flightNoteClose();
            this.isCreatedNode = false;
            this.mHandler.removeCallbacksAndMessages(null);
        } else if (this.isCreatedNode) {
            this.isCreatedNode = false;
            this.mHandler.removeCallbacks(this.closeFlightModeRunnable);
            this.mHandler.postDelayed(this.closeFlightModeRunnable, 30000);
        }
    }

    private void flightNoteClose() {
        if (this.mTelemetryFos != null) {
            try {
                this.mTelemetryFos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.mRemoteFos != null) {
            try {
                this.mRemoteFos.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (this.mRemoteGpsFos != null) {
            try {
                this.mRemoteGpsFos.close();
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        }
        if (!(this.mRemoteGpsFos == null && this.mRemoteFos == null && this.mTelemetryFos == null)) {
            encryptLog();
        }
        this.mRemoteGpsFos = null;
        this.mRemoteFos = null;
        this.mTelemetryFos = null;
    }

    private void encryptLog() {
        DES.logEncrypt(this.telFilePath);
        this.telFilePath = null;
        DES.logEncrypt(this.remFilePath);
        this.remFilePath = null;
        DES.logEncrypt(this.remGPSFilePath);
        this.remGPSFilePath = null;
    }
}
