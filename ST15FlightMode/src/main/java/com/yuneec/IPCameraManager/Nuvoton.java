package com.yuneec.IPCameraManager;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import com.yuneec.IPCameraManager.IPCameraManager.Bettery;
import com.yuneec.IPCameraManager.IPCameraManager.PhotoMode;
import com.yuneec.IPCameraManager.IPCameraManager.RecordStatus;
import com.yuneec.IPCameraManager.IPCameraManager.RecordTime;
import com.yuneec.IPCameraManager.IPCameraManager.RequestResult;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardFormat;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardFreeSpace;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardTotalSpace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Nuvoton extends DM368 {
    public static final int RESULT_APP_NOT_READY = -22;
    public static final int RESULT_CARD_PROTECTED = -18;
    public static final int RESULT_HDMI_INSERTED = -16;
    public static final int RESULT_INVALID_OPERATION = -14;
    public static final int RESULT_INVALID_OPTION_VALUE = -13;
    public static final int RESULT_JSON_PACKAGE_ERROR = -7;
    public static final int RESULT_JSON_PACKAGE_TIMEOUT = -8;
    public static final int RESULT_JSON_SYNTAX_ERROR = -9;
    public static final int RESULT_NO_MORE_SPACE = -17;
    public static final int RESULT_OK = 0;
    public static final int RESULT_OPERATION_MISMATCH = -15;
    public static final int RESULT_PIV_NOT_ALLOWED = -20;
    public static final int RESULT_REACH_MAX_CLNT = -5;
    public static final int RESULT_SESSION_START_FAIL = -3;
    public static final int RESULT_SYSTEM_BUSY = -21;
    public static final int RESULT_UNKNOWN_ERROR = -1;
    private static final String TAG = "Nuvoton";

    public static class SDcardInfo {
        public int available;
        public String storage;
        public int total;
        public String unit;
        public int used;

        public SDcardInfo(String storage, int total, int used, int available, String unit) {
            this.storage = storage;
            this.total = total;
            this.used = used;
            this.available = available;
            this.unit = unit;
        }
    }

    public void setCC4In1Config(Messenger relyTo, Cameras camera) {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = 12;
        msg.obj = IPCameraManager.HTTP_RESPONSE_CODE_OK;
        try {
            relyTo.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void syncTime(Messenger relyTo) {
        Log.i(TAG, "syncTime");
        Time now = new Time();
        now.setToNow();
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_TIME&time=" + now.format("%Y-%m-%d_%H:%M:%S"), relyTo, 1, IPCameraManager.APACHE_GET);
    }

    public void init() {
        this.SERVER_URL = "http://192.168.100.1/";
        this.FILE_PATH = "DCIM/100MEDIA/";
        this.REGEX_FORMAT_1 = "YUNC[\\w]*\\.THM";
        this.REGEX_FORMAT_2 = "YUNC[\\w]*\\.mp4";
        this.REGEX_FORMAT_3 = null;
        this.REGEX_FORMAT_4 = null;
        this.HTTP_CONNECTION_TIMEOUT = 10000;
        this.HTTP_SOCKET_TIMEOUT = 10000;
    }

    public void initCamera(Messenger relyTo) {
        Log.i(TAG, "init camera");
        postNullRequest(relyTo, 24);
    }

    public void getVersion(Messenger relyTo) {
        Log.i(TAG, "getVersion");
        postNullRequest(relyTo, 23);
    }

    public void startRecord(Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "startRecord");
        postRequest(this.SERVER_URL + "SkyEye/server.command?command=start_record_pipe&type=h264&pipe=0", relyTo, 2, IPCameraManager.APACHE_GET);
    }

    public void stopRecord(Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "stopRecord");
        postRequest(this.SERVER_URL + "SkyEye/server.command?command=stop_record&type=h264&pipe=0", relyTo, 3, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void snapShot(String filename, Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "snapShot");
        postRequest(this.SERVER_URL + "SkyEye/server.command?command=snapshot&pipe=0", relyTo, 4, IPCameraManager.APACHE_GET);
    }

    public void isRecording(Messenger relyTo) {
        Log.i(TAG, "isRecording");
        postRequest(this.SERVER_URL + "SkyEye/server.command?command=is_recording", relyTo, 21, IPCameraManager.APACHE_GET);
    }

    public void formatSDCard(Messenger relyTo) {
        Log.i(TAG, "formatSDCard");
        postNullRequest(relyTo, 14);
    }

    public void getRecordTime(Messenger relyTo) {
        postNullRequest(relyTo, 25);
    }

    public void restartVF(Messenger relyTo) {
        Log.i(TAG, "restartVF");
        postNullRequest(relyTo, 26);
    }

    public void stopVF(Messenger relyTo) {
        Log.i(TAG, "stopVF");
        postNullRequest(relyTo, 27);
    }

    public void setPhotoSize(Messenger relyTo, int mode) {
        Log.i(TAG, "setPhotoSize: mode=" + mode);
        postNullRequest(relyTo, 28);
    }

    public void getPhotoSize(Messenger relyTo) {
        Log.i(TAG, "getPhotoSize");
        postNullRequest(relyTo, 29);
    }

    public void getBattery(Messenger relyTo) {
        postNullRequest(relyTo, 30);
    }

    public void setVideoResolution(Messenger relyTo, int res) {
        Log.i(TAG, "setVideoResolution: value=" + res);
        postNullRequest(relyTo, 31);
    }

    public void getVideoResolution(Messenger relyTo) {
        Log.i(TAG, "getVideoResolution");
        postNullRequest(relyTo, 32);
    }

    public void setVideoStandard(Messenger relyTo, int param) {
        Log.i(TAG, "setVideoStandard: param= " + param);
        postNullRequest(relyTo, 33);
    }

    public void getVideoStandard(Messenger relyTo) {
        Log.i(TAG, "getVideoStandard");
        postNullRequest(relyTo, 34);
    }

    public void setFieldOfView(Messenger relyTo, int param) {
        Log.i(TAG, "setFieldOfView: param = " + param);
        postNullRequest(relyTo, 35);
    }

    public void getFieldOfView(Messenger relyTo) {
        Log.i(TAG, "getFieldOfView");
        postNullRequest(relyTo, 36);
    }

    public void getWorkStatus(Messenger relyTo) {
        postNullRequest(relyTo, 37);
    }

    public void getSDCardSpace(Messenger relyTo) {
        postNullRequest(relyTo, 38);
    }

    public void getSDCardFreeSpace(Messenger relyTo) {
        postNullRequest(relyTo, 39);
    }

    public void getSDCardFormat(Messenger relyTo) {
        Log.i(TAG, "getSDCardFormat");
        postNullRequest(relyTo, 40);
    }

    public void setPhotoMode(Messenger relyTo, int mode) {
        Log.i(TAG, "setPhotoMode: mode=" + mode);
        postNullRequest(relyTo, 41);
    }

    public void getPhotoMode(Messenger relyTo) {
        Log.i(TAG, "getPhotoMode");
        postNullRequest(relyTo, 42);
    }

    public void resetDefault(Messenger relyTo) {
        Log.i(TAG, "resetDefault");
        postNullRequest(relyTo, 43);
    }

    public void getDeviceStatus(Messenger relyTo) {
        Log.i(TAG, "getDeviceStatus");
        postRequest(this.SERVER_URL + "SkyEye/server.command?command=check_storage", relyTo, 44, IPCameraManager.APACHE_GET);
    }

    public void getSDCardStatus(Messenger relyTo) {
        Log.i(TAG, "getSDCardStatus");
        postRequest(this.SERVER_URL + "SkyEye/GetStorageCapacity.ncgi", relyTo, 10, IPCameraManager.JDK_GET);
    }

    protected Object onHandleSpecialRequest(Message msg, Object new_rsp, RequestResult rr) {
        Object rtn = new_rsp;
        String new_rsp2;
        String rtn2;
        switch (msg.arg1) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 24:
            case IPCameraManager.REQUEST_SET_VIDEO_STANDARD /*33*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        int result = new JSONObject((String) new_rsp).getInt("value");
                        if (result == 0) {
                            new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_OK;
                        } else {
                            new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                        }
                        Log.i(TAG, "request resoult: " + new_rsp2 + ",value=" + result);
                    } catch (JSONException e) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                        Log.e(TAG, "JSON error");
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case 10:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    char[] c = ((String) new_rsp).toCharArray();
                    StringBuilder sb = new StringBuilder();
                    int i = 0;
                    while (i < c.length) {
                        sb.append(c[i]);
                        if (c[i] == '}' && i != c.length - 1) {
                            sb.append(",");
                        }
                        i++;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray("[" + sb.toString() + "]");
                        new_rsp = new SDcardInfo(jsonArray.getJSONObject(0).getString("storage"), jsonArray.getJSONObject(1).getInt("total"), jsonArray.getJSONObject(2).getInt("used"), jsonArray.getJSONObject(3).getInt("available"), jsonArray.getJSONObject(4).getString("unit"));
                    } catch (JSONException e2) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case 21:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        int status = new JSONObject((String) new_rsp).getInt("value");
                        RecordStatus recordStatus = new RecordStatus(false);
                        if (status == 1) {
                            recordStatus.isRecording = true;
                        } else {
                            recordStatus.isRecording = false;
                        }
                        new_rsp = recordStatus;
                    } catch (JSONException e3) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case 23:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp2 = new JSONObject((String) new_rsp).getString("YUNEEC_ver");
                    } catch (JSONException e4) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_REC_TIME /*25*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp = new RecordTime(new JSONObject((String) new_rsp).getInt("param"));
                    } catch (JSONException e5) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_PHOTO_SIZE /*29*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp2 = new JSONObject((String) new_rsp).getString("param");
                    } catch (JSONException e6) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_BETTERY /*30*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp = new Bettery(new JSONObject((String) new_rsp).getInt("param"));
                    } catch (JSONException e7) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case 32:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp2 = new JSONObject((String) new_rsp).getString("param");
                    } catch (JSONException e8) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_VIDEO_STANDARD /*34*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp2 = new JSONObject((String) new_rsp).getString("param");
                    } catch (JSONException e9) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_FOV /*36*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp2 = new JSONObject((String) new_rsp).getString("param");
                    } catch (JSONException e10) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_WORK_STATUS /*37*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp2 = new JSONObject((String) new_rsp).getString("param");
                    } catch (JSONException e11) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_SDCARD_SPACE /*38*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp = new SDCardTotalSpace(new JSONObject((String) new_rsp).getInt("param"));
                    } catch (JSONException e12) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_SDCARD_FREE_SPACE /*39*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp = new SDCardFreeSpace(new JSONObject((String) new_rsp).getInt("param"));
                    } catch (JSONException e13) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case 40:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp = new SDCardFormat(new JSONObject((String) new_rsp).getInt("param"));
                    } catch (JSONException e14) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_PHOTO_MODE /*42*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp = new PhotoMode(new JSONObject((String) new_rsp).getInt("param"));
                    } catch (JSONException e15) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_DEVICE_STATUS /*44*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        new_rsp = Integer.valueOf(new JSONObject((String) new_rsp).getInt("value"));
                    } catch (JSONException e16) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
        }
        rr.result = IPCameraManager.SPECIAL_RESPONSE_HANDLED;
        return rtn;
    }
}
