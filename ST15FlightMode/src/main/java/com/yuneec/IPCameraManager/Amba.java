package com.yuneec.IPCameraManager;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import com.yuneec.IPCameraManager.IPCameraManager.Bettery;
import com.yuneec.IPCameraManager.IPCameraManager.DevicesStatus;
import com.yuneec.IPCameraManager.IPCameraManager.PhotoMode;
import com.yuneec.IPCameraManager.IPCameraManager.RecordStatus;
import com.yuneec.IPCameraManager.IPCameraManager.RecordTime;
import com.yuneec.IPCameraManager.IPCameraManager.RequestResult;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardFormat;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardFreeSpace;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardStatus;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardTotalSpace;
import com.yuneec.IPCameraManager.IPCameraManager.ShutterTimeISO;
import org.json.JSONException;
import org.json.JSONObject;

public class Amba extends DM368 {
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
    private static final String TAG = "Amba";

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
        this.SERVER_URL = "http://192.168.42.1/";
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
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=INDEX_PAGE", relyTo, 24, IPCameraManager.APACHE_GET);
    }

    public void getVersion(Messenger relyTo) {
        Log.i(TAG, "getVersion");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_FW_VERSION", relyTo, 23, IPCameraManager.APACHE_GET);
    }

    public void startRecord(Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "startRecord");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=START_RECORD", relyTo, 2, IPCameraManager.APACHE_GET);
    }

    public void stopRecord(Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "stopRecord");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=STOP_RECORD", relyTo, 3, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void snapShot(String filename, Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "snapShot");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=TAKE_PHOTO", relyTo, 4, IPCameraManager.APACHE_GET);
    }

    public void isRecording(Messenger relyTo) {
        Log.i(TAG, "isRecording");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_STATUS", relyTo, 21, IPCameraManager.APACHE_GET);
    }

    public void formatSDCard(Messenger relyTo) {
        Log.i(TAG, "formatSDCard");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=FORMAT_CARD", relyTo, 14, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getRecordTime(Messenger relyTo) {
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_REC_TIME", relyTo, 25, IPCameraManager.APACHE_GET);
    }

    public void restartVF(Messenger relyTo) {
        Log.i(TAG, "restartVF");
        stopVF(relyTo);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=REST_VF", relyTo, 26, IPCameraManager.APACHE_GET);
    }

    public void stopVF(Messenger relyTo) {
        Log.i(TAG, "stopVF");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=STOP_VF", relyTo, 27, IPCameraManager.APACHE_GET);
    }

    public void setPhotoSize(Messenger relyTo, int mode) {
        Log.i(TAG, "setPhotoSize: mode=" + mode);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_PHOTO_SIZE&MODE=" + mode, relyTo, 28, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getPhotoSize(Messenger relyTo) {
        Log.i(TAG, "getPhotoSize");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_PHOTO_SIZE", relyTo, 29, IPCameraManager.APACHE_GET);
    }

    public void getBattery(Messenger relyTo) {
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_BATTERY_LEVEL", relyTo, 30, IPCameraManager.APACHE_GET);
    }

    public void setVideoResolution(Messenger relyTo, int res) {
        Log.i(TAG, "setVideoResolution: value=" + res);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_SETTING&resolution=" + res, relyTo, 31, IPCameraManager.APACHE_GET);
    }

    public void getVideoResolution(Messenger relyTo) {
        Log.i(TAG, "getVideoResolution");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_SETTING", relyTo, 32, IPCameraManager.APACHE_GET);
    }

    public void setVideoStandard(Messenger relyTo, int param) {
        Log.i(TAG, "setVideoStandard: param= " + param);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_VIDEO_STANDARD&PARAM=" + param, relyTo, 33, IPCameraManager.APACHE_GET);
    }

    public void getVideoStandard(Messenger relyTo) {
        Log.i(TAG, "getVideoStandard");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_VIDEO_STANDARD", relyTo, 34, IPCameraManager.APACHE_GET);
    }

    public void setFieldOfView(Messenger relyTo, int param) {
        Log.i(TAG, "setFieldOfView: param = " + param);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_FOV&PARAM=" + param, relyTo, 35, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getFieldOfView(Messenger relyTo) {
        Log.i(TAG, "getFieldOfView");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_FOV", relyTo, 36, IPCameraManager.APACHE_GET);
    }

    public void getWorkStatus(Messenger relyTo) {
        Log.i(TAG, "getWorkStatus");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_STATUS", relyTo, 37, IPCameraManager.APACHE_GET);
    }

    public void getDeviceStatus(Messenger relyTo) {
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=DETECT_CARD", relyTo, 44, IPCameraManager.APACHE_GET);
    }

    public void getSDCardSpace(Messenger relyTo) {
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_SPACE", relyTo, 38, IPCameraManager.APACHE_GET);
    }

    public void getSDCardFreeSpace(Messenger relyTo) {
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_SPACE_FREE", relyTo, 39, IPCameraManager.APACHE_GET);
    }

    public void getSDCardFormat(Messenger relyTo) {
        Log.i(TAG, "getSDCardFormat");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_CARD_FORMAT", relyTo, 40, IPCameraManager.APACHE_GET);
    }

    public void setPhotoMode(Messenger relyTo, int mode) {
        Log.i(TAG, "setPhotoMode: mode=" + mode);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_PHOTO_MODE&MODE=" + mode, relyTo, 41, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getPhotoMode(Messenger relyTo) {
        Log.i(TAG, "getPhotoMode");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_PHOTO_MODE", relyTo, 42, IPCameraManager.APACHE_GET);
    }

    public void resetDefault(Messenger relyTo) {
        Log.i(TAG, "resetDefault");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=RESET_DEFAULT", relyTo, 43, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getSDCardStatus(Messenger relyTo) {
        Log.i(TAG, "getSDCardStatus");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_SPACE_FREE", relyTo, 10, IPCameraManager.APACHE_GET);
    }

    public void setAudioState(Messenger relyTo, int state) {
        Log.i(TAG, "set audio state: state=" + state);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_AUDIO_SW&mode=" + state, relyTo, 59, IPCameraManager.APACHE_GET);
    }

    public void getAudioState(Messenger relyTo) {
        Log.i(TAG, "get audio state");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_AUDIO_SW", relyTo, 60, IPCameraManager.APACHE_GET);
    }

    public void setPhotoFormat(Messenger relyTo, String format) {
        Log.i(TAG, "setPhotoFormat: " + format);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_PHOTO_FORMAT&value=" + format, relyTo, 45, IPCameraManager.APACHE_GET);
    }

    public void getPhotoFormat(Messenger relyTo) {
        Log.i(TAG, "getPhotoFormat");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_PHOTO_FORMAT", relyTo, 46, IPCameraManager.APACHE_GET);
    }

    public void setAEenable(Messenger relyTo, int enable) {
        Log.i(TAG, "setAEenable: " + enable);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_AE_ENABLE&mode=" + enable, relyTo, 47, IPCameraManager.APACHE_GET);
    }

    public void getAEenable(Messenger relyTo) {
        Log.i(TAG, "getAEenable");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_AE_ENABLE", relyTo, 48, IPCameraManager.APACHE_GET);
    }

    public void setShutterTimeAndISO(Messenger relyTo, int time, String iso) {
        Log.i(TAG, "setShutterTimeAndISO: time=" + time + ",iso=" + iso);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_SH_TM_ISO&time=" + time + "&value=" + iso, relyTo, 49, IPCameraManager.APACHE_GET);
    }

    public void getShutterTimeAndISO(Messenger relyTo) {
        Log.i(TAG, "getShutterTimeAndISO");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_SH_TM_ISO", relyTo, 50, IPCameraManager.APACHE_GET);
    }

    public void setIQtype(Messenger relyTo, int type) {
        Log.i(TAG, "setIQtype: " + type);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_IQ_TYPE&mode=" + type, relyTo, 51, IPCameraManager.APACHE_GET);
    }

    public void getIQtype(Messenger relyTo) {
        Log.i(TAG, "getIQtype");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_IQ_TYPE", relyTo, 52, IPCameraManager.APACHE_GET);
    }

    public void setWhiteBalance(Messenger relyTo, int mode) {
        Log.i(TAG, "setWhiteBalance: " + mode);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_WHITEBLANCE_MODE&mode=" + mode, relyTo, 53, IPCameraManager.APACHE_GET);
    }

    public void getWhiteBalance(Messenger relyTo) {
        Log.i(TAG, "getWhiteBalance");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_WHITEBLANCE_MODE", relyTo, 54, IPCameraManager.APACHE_GET);
    }

    public void setExposure(Messenger relyTo, String value) {
        Log.i(TAG, "setExposure:" + value);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_EXPOSURE_VALUE&mode=" + value, relyTo, 55, IPCameraManager.APACHE_GET);
    }

    public void getExposure(Messenger relyTo) {
        Log.i(TAG, "getExposure");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_EXPOSURE_VALUE", relyTo, 56, IPCameraManager.APACHE_GET);
    }

    public void setVideoMode(Messenger relyTo, String mode) {
        Log.i(TAG, "setVideoMode锛� " + mode);
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_VIDEO_MODE&video_mode=" + mode, relyTo, 57, IPCameraManager.APACHE_GET);
    }

    public void getVideoMode(Messenger relyTo) {
        Log.i(TAG, "getVideoMode");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_VIDEO_MODE", relyTo, 58, IPCameraManager.APACHE_GET);
    }

    public void setCameraMode(Messenger relyTo, String mode) {
        Log.i(TAG, "setCameraMode");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=SET_CAM_MODE&mode=" + mode, relyTo, 61, IPCameraManager.APACHE_GET);
    }

    public void getCameraMode(Messenger relyTo) {
        Log.i(TAG, "getCameraMode");
        postRequest(this.SERVER_URL + "cgi-bin/cgi?CMD=GET_CAM_MODE", relyTo, 62, IPCameraManager.APACHE_GET);
    }

    protected Object onHandleSpecialRequest(Message msg, Object new_rsp, RequestResult rr) {
        Object rtn = new_rsp;
        String new_rsp2;
        String rtn2;
        JSONObject jSONObject;
        switch (msg.arg1) {
            case 1:
            case 2:
            case 4:
            case IPCameraManager.REQUEST_REST_VF /*26*/:
            case IPCameraManager.REQUEST_STOP_VF /*27*/:
            case IPCameraManager.REQUEST_SET_VIDEO_RESOLUTION /*31*/:
            case IPCameraManager.REQUEST_SET_VIDEO_STANDARD /*33*/:
            case IPCameraManager.REQUEST_SET_PHOTO_FORMAT /*45*/:
            case IPCameraManager.REQUEST_SET_AE_ENABLE /*47*/:
            case IPCameraManager.REQUEST_SET_SH_TM_ISO /*49*/:
            case IPCameraManager.REQUEST_SET_IQ_TYPE /*51*/:
            case IPCameraManager.REQUEST_SET_WHITEBLANCE_MODE /*53*/:
            case IPCameraManager.REQUEST_SET_EXPOSURE_VALUE /*55*/:
            case IPCameraManager.REQUEST_SET_VIDEO_MODE /*57*/:
            case IPCameraManager.REQUEST_SET_CAMERA_MODE /*61*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        if (new JSONObject((String) new_rsp).getInt("rval") == 0) {
                            new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_OK;
                        } else {
                            new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                        }
                    } catch (JSONException e) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case 10:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        int space = new JSONObject((String) new_rsp).getInt("param") >> 10;
                        SDCardStatus status = new SDCardStatus(true, (long) space, 0);
                        if (space <= 18) {
                            status.isInsert = false;
                        }
                        new_rsp = status;
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
                        String status2 = new JSONObject((String) new_rsp).getString("param");
                        RecordStatus recordStatus = new RecordStatus(false);
                        if ("record".equals(status2)) {
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
                if (!IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    rtn = IPCameraManager.HTTP_RESPONSE_CODE_UNKNOWN;
                    break;
                }
                try {
                    new_rsp2 = new JSONObject((String) new_rsp).getString("YUNEEC_ver");
                } catch (JSONException e4) {
                    new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                }
                rtn2 = new_rsp2;
                break;
            case 24:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        if (new JSONObject((String) new_rsp).getInt("rval") == 0) {
                            new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_OK;
                        } else {
                            new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                        }
                    } catch (JSONException e5) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_REC_TIME /*25*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        jSONObject = new JSONObject((String) new_rsp);
                        if (jSONObject.getInt("rval") == 0) {
                            new_rsp = new RecordTime(jSONObject.getInt("param"));
                        }
                    } catch (JSONException e6) {
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
                    } catch (JSONException e7) {
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
                    } catch (JSONException e8) {
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
                    } catch (JSONException e9) {
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
                    } catch (JSONException e10) {
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
                    } catch (JSONException e11) {
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
                    } catch (JSONException e12) {
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
                    } catch (JSONException e13) {
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
                    } catch (JSONException e14) {
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
                    } catch (JSONException e15) {
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
                    } catch (JSONException e16) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_DEVICE_STATUS /*44*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        jSONObject = new JSONObject((String) new_rsp);
                        int sd_free = jSONObject.getInt("free");
                        int sd_total = jSONObject.getInt("total");
                        int sd_status = jSONObject.getInt("status");
                        String video_status = jSONObject.getString("video_status");
                        Log.i(TAG, "Work status is: " + video_status);
                        new_rsp = new DevicesStatus(sd_free, sd_total, sd_status, video_status);
                    } catch (JSONException e17) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_PHOTO_FORMAT /*46*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        String photo_format = new JSONObject((String) new_rsp).getString("photo_format");
                        Log.i(TAG, "Photo format is: " + photo_format);
                        new_rsp2 = photo_format;
                    } catch (JSONException e18) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_AE_ENABLE /*48*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        Integer ae_enable = Integer.valueOf(new JSONObject((String) new_rsp).getInt("ae_enable"));
                        Log.i(TAG, "AE enable is: " + ae_enable);
                        new_rsp = ae_enable;
                    } catch (JSONException e19) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_SH_TM_ISO /*50*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        jSONObject = new JSONObject((String) new_rsp);
                        int time = jSONObject.getInt("shutter_time");
                        String iso = jSONObject.getString("iso_value");
                        Log.i(TAG, "Shutter time is: " + time + "ISO is: " + iso);
                        ShutterTimeISO sti = new ShutterTimeISO();
                        sti.iso = iso;
                        sti.time = time;
                        new_rsp = sti;
                    } catch (JSONException e20) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_IQ_TYPE /*52*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        Integer iq_type = Integer.valueOf(new JSONObject((String) new_rsp).getInt("IQ_type"));
                        Log.i(TAG, "IQ type is: " + iq_type);
                        new_rsp = iq_type;
                    } catch (JSONException e21) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_WHITEBALANCE_MODE /*54*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        Integer wb_mode = Integer.valueOf(new JSONObject((String) new_rsp).getInt("wb_mode"));
                        Log.i(TAG, "White balance is: " + wb_mode);
                        new_rsp = wb_mode;
                    } catch (JSONException e22) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_EXPOSURE_VALUE /*56*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        String expo_value = new JSONObject((String) new_rsp).getString("exposure_value");
                        Log.i(TAG, "Exposure value is: " + expo_value);
                        new_rsp2 = expo_value;
                    } catch (JSONException e23) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_VIDEO_MODE /*58*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        String video_mode = new JSONObject((String) new_rsp).getString("video_mode");
                        Log.i(TAG, "Video mode is: " + video_mode);
                        new_rsp2 = video_mode;
                    } catch (JSONException e24) {
                        new_rsp2 = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn2 = new_rsp2;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_SET_AUDIO_STATE /*59*/:
                rtn = new_rsp;
                break;
            case IPCameraManager.REQUEST_GET_AUDIO_STATE /*60*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        Integer audio_state = Integer.valueOf(new JSONObject((String) new_rsp).getInt("audio_sw"));
                        Log.i(TAG, "Audio state is: " + audio_state);
                        new_rsp = audio_state;
                    } catch (JSONException e25) {
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    }
                    rtn = new_rsp;
                    break;
                }
                break;
            case IPCameraManager.REQUEST_GET_CAMERA_MODE /*62*/:
                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                    try {
                        Integer mode = Integer.valueOf(new JSONObject((String) new_rsp).getInt("cam_mode"));
                        Log.i(TAG, "camera mode is: " + mode);
                        new_rsp = mode;
                    } catch (JSONException e26) {
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
