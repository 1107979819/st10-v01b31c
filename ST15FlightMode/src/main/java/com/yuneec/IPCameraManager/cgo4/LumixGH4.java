package com.yuneec.IPCameraManager.cgo4;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.yuneec.IPCameraManager.Cameras;
import com.yuneec.IPCameraManager.DM368;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.IPCameraManager.IPCameraManager.RequestResult;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LumixGH4 extends DM368 {
    private static final String TAG = "LumixGH4";

    private enum CommandType {
        GET_COMMAND,
        SET_COMMAND
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

    public void init() {
        this.SERVER_URL = "http://192.168.54.1/";
        this.FILE_PATH = "DCIM/100MEDIA/";
        this.REGEX_FORMAT_1 = "YUNC[\\w]*\\.THM";
        this.REGEX_FORMAT_2 = "YUNC[\\w]*\\.mp4";
        this.REGEX_FORMAT_3 = null;
        this.REGEX_FORMAT_4 = null;
        this.HTTP_CONNECTION_TIMEOUT = 10000;
        this.HTTP_SOCKET_TIMEOUT = 10000;
    }

    public void initCamera(Messenger relyTo) {
        postRequest(this.SERVER_URL + "cam.cgi?mode=camcmd&value=recmode", relyTo, 24, IPCameraManager.APACHE_GET);
    }

    public void syncTime(Messenger relyTo) {
        Log.i(TAG, "syncTime");
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=clock&value=" + new StringBuilder(String.valueOf(new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()))).append("+0000").toString(), relyTo, 1, IPCameraManager.APACHE_GET);
    }

    public void startRecord(Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "startRecord");
        postRequest(this.SERVER_URL + "cam.cgi?mode=camcmd&value=video_recstart", relyTo, 2, IPCameraManager.APACHE_GET);
    }

    public void stopRecord(Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "stopRecord");
        postRequest(this.SERVER_URL + "cam.cgi?mode=camcmd&value=video_recstop", relyTo, 3, IPCameraManager.APACHE_GET);
    }

    public void snapShot(String filename, Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "snapShot");
        postRequest(this.SERVER_URL + "cam.cgi?mode=camcmd&value=capture", relyTo, 4, IPCameraManager.APACHE_GET);
    }

    public void resetDefault(Messenger relyTo) {
        Log.i(TAG, "resetDefault");
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=systemmenu&value=reset", relyTo, 43, IPCameraManager.APACHE_GET);
    }

    public void formatSDCard(Messenger relyTo) {
        Log.i(TAG, "formatSDCard");
        postRequest(this.SERVER_URL + "cam.cgi?mode=editcmd&type=format&value=sd", relyTo, 14, IPCameraManager.APACHE_GET);
    }

    public void setAEenable(Messenger relyTo, boolean enable) {
        Log.i(TAG, "setAEenable: " + enable);
        postRequest(this.SERVER_URL + "cam.cgi?mode=camctrl&type=af_ae_lock&value=" + (enable ? "on" : "off"), relyTo, 47, IPCameraManager.APACHE_GET);
    }

    public void zoom(String operate, Messenger relyTo) {
        Log.i(TAG, "zoom--operate:" + operate);
        postRequest(this.SERVER_URL + "cam.cgi?mode=camcmd&value=" + operate, relyTo, 8, IPCameraManager.APACHE_GET);
    }

    public void stopZoom(Messenger relyTo) {
        Log.i(TAG, "stopZoom");
        postRequest(this.SERVER_URL + "cam.cgi?mode=camcmd&value=zoomstop", relyTo, 8, IPCameraManager.APACHE_GET);
    }

    public void fixFocus(Messenger relyTo, String value, boolean enable) {
        Log.i(TAG, "fixFocus--value=" + value + ", enable=" + enable);
        postRequest(this.SERVER_URL + "cam.cgi?mode=camctrl&type=touch&value=" + value + "&value2=" + (enable ? "on" : "off"), relyTo, 64, IPCameraManager.APACHE_GET);
    }

    public void manualFocus(Messenger relyTo, String operate) {
        Log.i(TAG, "manualFocus--operate:" + operate);
        postRequest(this.SERVER_URL + "cam.cgi?mode=camctrl&type=focus&value=" + operate, relyTo, 65, IPCameraManager.APACHE_GET);
    }

    public void getWorkStatus(Messenger relyTo) {
        Log.i(TAG, "getWorkStatus");
        postRequest(this.SERVER_URL + "cam.cgi?mode=getstate", relyTo, 37, IPCameraManager.APACHE_GET);
    }

    public void setExposure(Messenger relyTo, String value) {
        Log.i(TAG, "setExposure:" + value);
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=exposure&value=" + value, relyTo, 55, IPCameraManager.APACHE_GET);
    }

    public void setWhiteBalance(Messenger relyTo, String mode) {
        Log.i(TAG, "setWhiteBalance: " + mode);
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=whitebalance&value=" + mode, relyTo, 53, IPCameraManager.APACHE_GET);
    }

    public void getWhiteBalance(Messenger relyTo) {
        Log.i(TAG, "getWhiteBalance");
        postRequest(this.SERVER_URL + "cam.cgi?mode=getsetting&type=whitebalance", relyTo, 54, IPCameraManager.APACHE_GET);
    }

    public void setISO(Messenger relyTo, String value) {
        Log.i(TAG, "setISO:" + value);
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=iso&value=" + value, relyTo, 66, IPCameraManager.APACHE_GET);
    }

    public void getISO(Messenger relyTo) {
        Log.i(TAG, "getISO");
        postRequest(this.SERVER_URL + "cam.cgi?mode=getsetting&type=iso", relyTo, 67, IPCameraManager.APACHE_GET);
    }

    public void setShutterTime(Messenger relyTo, String value) {
        Log.i(TAG, "setShutterTime:" + value);
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=shtrspeed&value=" + value + "/256", relyTo, 68, IPCameraManager.APACHE_GET);
    }

    public void setAperture(Messenger relyTo, String value) {
        Log.i(TAG, "setAperture:" + value);
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=focal&value=" + value + "/256", relyTo, 70, IPCameraManager.APACHE_GET);
    }

    public void setCameraMode(Messenger relyTo, String mode) {
        Log.i(TAG, "setCameraMode:" + mode);
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=remote_rec_mode&value=" + mode, relyTo, 61, IPCameraManager.APACHE_GET);
    }

    public void getCameraMode(Messenger relyTo) {
        Log.i(TAG, "getCameraMode");
        postRequest(this.SERVER_URL + "cam.cgi?mode=getsetting&type=remote_rec_mode", relyTo, 62, IPCameraManager.APACHE_GET);
    }

    public void getRecordMode(Messenger relyTo) {
        Log.i(TAG, "setRecordingMode");
        postRequest(this.SERVER_URL + "cam.cgi?mode=getsetting&type=remote_rec_mode", relyTo, 72, IPCameraManager.APACHE_GET);
    }

    public void setRecordMode(Messenger relyTo, String mode) {
        Log.i(TAG, "setRecordMode:" + mode);
        postRequest(this.SERVER_URL + "cam.cgi?mode=setsetting&type=remote_rec_mode&value=" + mode, relyTo, 73, IPCameraManager.APACHE_GET);
    }

    public void setProgramShift(Messenger relyTo, String value) {
        Log.i(TAG, "setProgrameShift:" + value);
        postRequest(this.SERVER_URL + "cam.cgi?mode=camctrl&type=program_shift&value=" + value, relyTo, 78, IPCameraManager.APACHE_GET);
    }

    public void getLensInfo(Messenger relyTo) {
        Log.i(TAG, "getLensInfo");
        postRequest(this.SERVER_URL + "cam.cgi?mode=getinfo&type=lens", relyTo, 79, IPCameraManager.APACHE_GET);
    }

    public void getCurrentMenu(Messenger relyTo) {
        Log.i(TAG, "getCurrentMenu");
        postRequest(this.SERVER_URL + "cam.cgi?mode=getinfo&type=curmenu", relyTo, 74, IPCameraManager.APACHE_GET);
    }

    public void getSingleCurrentMenu(Messenger relyTo, String type) {
        Log.i(TAG, "getSingleCurrentMenu");
        postRequest(this.SERVER_URL + "cam.cgi?mode=getsetting&type=" + type, relyTo, 82, IPCameraManager.APACHE_GET);
    }

    public void setCurrentMenu(Messenger relyTo, String type, String value, String value2) {
        String currentMenuApi;
        if (value2 == null) {
            currentMenuApi = this.SERVER_URL + "cam.cgi?mode=setsetting&type=" + type + "&value=" + value;
        } else {
            currentMenuApi = this.SERVER_URL + "cam.cgi?mode=setsetting&type=" + type + "&value=" + value + "&value2=" + value2;
        }
        Log.i(TAG, "setCurrentMenu : " + currentMenuApi);
        postRequest(currentMenuApi, relyTo, 75, IPCameraManager.APACHE_GET);
    }

    public void startUdpDatagram(Messenger relyTo, int listenPort) {
        Log.i(TAG, "startUdpDatagram");
        postRequest(this.SERVER_URL + "cam.cgi?mode=startstream&value=" + String.valueOf(listenPort), relyTo, 76, IPCameraManager.APACHE_GET);
    }

    public void stopUdpDatagram(Messenger relyTo) {
        Log.i(TAG, "stopUdpDatagram");
        postRequest(this.SERVER_URL + "cam.cgi?mode=stopstream", relyTo, 77, IPCameraManager.APACHE_GET);
    }

    public void turnOnCamera(Messenger relyTo) {
        Log.i(TAG, "turnOnLens");
        postRequest("http://192.168.73.254/cgi-bin/cgi?CMD=START_CAMERA", relyTo, 80, IPCameraManager.APACHE_GET);
    }

    public void turnOffCamera(Messenger relyTo) {
        Log.i(TAG, "turnOffLens");
        postRequest("http://192.168.73.254/cgi-bin/cgi?CMD=STOP_CAMERA", relyTo, 81, IPCameraManager.APACHE_GET);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.yuneec.IPCameraManager.cgo4.ResponseResult parseXML(java.lang.String r14, com.yuneec.IPCameraManager.cgo4.LumixGH4.CommandType r15) {
        /*
        r13 = this;
        r11 = 1;
        r12 = 0;
        r4 = 1;
        r7 = android.util.Xml.newPullParser();
        r6 = new java.io.ByteArrayInputStream;
        r9 = r14.getBytes();
        r6.<init>(r9);
        r9 = "UTF-8";
        r7.setInput(r6, r9);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r4 = r7.getEventType();	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
    L_0x0019:
        if (r4 != r11) goto L_0x0024;
    L_0x001b:
        r6.close();	 Catch:{ IOException -> 0x0107 }
    L_0x001e:
        r5 = new com.yuneec.IPCameraManager.cgo4.SettingResponse;
        r5.<init>(r12);
    L_0x0023:
        return r5;
    L_0x0024:
        switch(r4) {
            case 2: goto L_0x002c;
            default: goto L_0x0027;
        };
    L_0x0027:
        r4 = r7.next();	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        goto L_0x0019;
    L_0x002c:
        r9 = "result";
        r10 = r7.getName();	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = r9.equals(r10);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        if (r9 == 0) goto L_0x0074;
    L_0x0038:
        r7.next();	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r8 = r7.getText();	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = "ok";
        r9 = r9.equals(r8);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        if (r9 == 0) goto L_0x005e;
    L_0x0047:
        r9 = com.yuneec.IPCameraManager.cgo4.LumixGH4.CommandType.SET_COMMAND;	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = r15.equals(r9);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        if (r9 == 0) goto L_0x0027;
    L_0x004f:
        r5 = new com.yuneec.IPCameraManager.cgo4.SettingResponse;	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = 1;
        r5.<init>(r9);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r6.close();	 Catch:{ IOException -> 0x0059 }
        goto L_0x0023;
    L_0x0059:
        r3 = move-exception;
        r3.printStackTrace();
        goto L_0x0023;
    L_0x005e:
        r9 = "LumixGH4";
        r10 = "set response failure";
        android.util.Log.e(r9, r10);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r5 = new com.yuneec.IPCameraManager.cgo4.SettingResponse;	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = 0;
        r5.<init>(r9);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r6.close();	 Catch:{ IOException -> 0x006f }
        goto L_0x0023;
    L_0x006f:
        r3 = move-exception;
        r3.printStackTrace();
        goto L_0x0023;
    L_0x0074:
        r9 = "settingvalue";
        r10 = r7.getName();	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = r9.equals(r10);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        if (r9 == 0) goto L_0x0027;
    L_0x0080:
        r9 = 0;
        r0 = r7.getAttributeName(r9);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = 0;
        r1 = r7.getAttributeValue(r9, r0);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r2 = r7.nextText();	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = "LumixGH4";
        r10 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r11 = "settingvalue:";
        r10.<init>(r11);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r10 = r10.append(r0);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r11 = "=";
        r10 = r10.append(r11);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r10 = r10.append(r1);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r11 = ", attributeValue2=";
        r10 = r10.append(r11);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r10 = r10.append(r2);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r10 = r10.toString();	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        android.util.Log.i(r9, r10);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r5 = new com.yuneec.IPCameraManager.cgo4.GettingResponse;	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r9 = 1;
        r5.<init>(r9, r0, r1);	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r5.value2 = r2;	 Catch:{ XmlPullParserException -> 0x00c9, IOException -> 0x00ee }
        r6.close();	 Catch:{ IOException -> 0x00c3 }
        goto L_0x0023;
    L_0x00c3:
        r3 = move-exception;
        r3.printStackTrace();
        goto L_0x0023;
    L_0x00c9:
        r3 = move-exception;
        r4 = 1;
        r9 = "LumixGH4";
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00fd }
        r11 = "XmlPullParserException:";
        r10.<init>(r11);	 Catch:{ all -> 0x00fd }
        r11 = r3.toString();	 Catch:{ all -> 0x00fd }
        r10 = r10.append(r11);	 Catch:{ all -> 0x00fd }
        r10 = r10.toString();	 Catch:{ all -> 0x00fd }
        android.util.Log.e(r9, r10);	 Catch:{ all -> 0x00fd }
        r6.close();	 Catch:{ IOException -> 0x00e8 }
        goto L_0x001e;
    L_0x00e8:
        r3 = move-exception;
        r3.printStackTrace();
        goto L_0x001e;
    L_0x00ee:
        r3 = move-exception;
        r3.printStackTrace();	 Catch:{ all -> 0x00fd }
        r6.close();	 Catch:{ IOException -> 0x00f7 }
        goto L_0x001e;
    L_0x00f7:
        r3 = move-exception;
        r3.printStackTrace();
        goto L_0x001e;
    L_0x00fd:
        r9 = move-exception;
        r6.close();	 Catch:{ IOException -> 0x0102 }
    L_0x0101:
        throw r9;
    L_0x0102:
        r3 = move-exception;
        r3.printStackTrace();
        goto L_0x0101;
    L_0x0107:
        r3 = move-exception;
        r3.printStackTrace();
        goto L_0x001e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.IPCameraManager.cgo4.LumixGH4.parseXML(java.lang.String, com.yuneec.IPCameraManager.cgo4.LumixGH4$CommandType):com.yuneec.IPCameraManager.cgo4.ResponseResult");
    }

    protected Object onHandleSpecialRequest(Message msg, Object new_rsp, RequestResult rr) {
        Object rtn = new_rsp;
        Log.i(TAG, "onHandleSpecialRequest msg.arg1=" + msg.arg1);
        switch (msg.arg1) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 24:
            case IPCameraManager.REQUEST_SET_WHITEBLANCE_MODE /*53*/:
            case IPCameraManager.REQUEST_SET_EXPOSURE_VALUE /*55*/:
            case 64:
            case IPCameraManager.REQUEST_SET_ISO /*66*/:
            case IPCameraManager.REQUEST_SET_RECORD_MODE /*73*/:
            case IPCameraManager.REQUEST_SET_CURRENT_MENU /*75*/:
            case IPCameraManager.REQUEST_START_UDP /*76*/:
            case IPCameraManager.REQUEST_STOP_UDP /*77*/:
            case 81:
                rtn = parseXML((String) rtn, CommandType.SET_COMMAND);
                break;
            case IPCameraManager.REQUEST_GET_WORK_STATUS /*37*/:
                rtn = StateParser.parse((String) rtn);
                break;
            case IPCameraManager.REQUEST_GET_WHITEBALANCE_MODE /*54*/:
            case IPCameraManager.REQUEST_GET_ISO /*67*/:
            case IPCameraManager.REQUEST_GET_RECORD_MODE /*72*/:
            case 82:
                rtn = parseXML((String) rtn, CommandType.GET_COMMAND);
                break;
            case IPCameraManager.REQUEST_SET_SHUTTER_TIME /*68*/:
            case IPCameraManager.REQUEST_GET_LENS_INFO /*79*/:
                LensInformation lensInformation = new LensInformation((String) rtn);
                break;
            case IPCameraManager.REQUEST_GET_CURRENT_MENU /*74*/:
                rtn = new MenuSettingsXmlParser().parse((String) rtn);
                break;
        }
        rr.result = IPCameraManager.SPECIAL_RESPONSE_HANDLED;
        return rtn;
    }
}
