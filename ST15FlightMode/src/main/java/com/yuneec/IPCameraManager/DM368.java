package com.yuneec.IPCameraManager;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import com.yuneec.IPCameraManager.IPCameraManager.GetMediaFileCallback;
import com.yuneec.IPCameraManager.IPCameraManager.RequestResult;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardStatus;
import com.yuneec.IPCameraManager.IPCameraManager.ToneSetting;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

@TargetApi(11)
public class DM368 extends IPCameraManager {
    protected static final String Authorization_Header = "Authorization";
    protected static final String Authorization_Values = "Basic YWRtaW46OTk5OQ==";
    private static final ArrayList<String> INTERESTED_CAMERA_CONFIG = new ArrayList();
    private static final String REQUEST_DELSCHEDULE = "delschedule=";
    private static final String REQUEST_SCHEDULE = "schedule=";
    protected static final String REQUEST_SET = "vb.htm";
    private static final String REQUEST_SETDATE = "newdate=";
    private static final String REQUEST_SETTIME = "newtime=";
    private static final String REQUEST_SNAPSHOT = "clicksnapfilename=";
    private static final String TAG = "DM368";
    private Runnable mAfterDelSchedule;
    private int mDesiredRtspResolution;
    private Handler mInternalHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case 1:
                            return;
                        case 3:
                            Log.i(DM368.TAG, "stop record result:" + msg.obj);
                            if (!msg.obj.equals(IPCameraManager.HTTP_RESPONSE_CODE_OK)) {
                                notifySender(msg, DM368.this.mStopRecordMessenger, 3);
                            } else if (DM368.this.mAfterDelSchedule != null) {
                                DM368.this.mAfterDelSchedule.run();
                                DM368.this.mAfterDelSchedule = null;
                            }
                            DM368.this.mStopRecordMessenger = null;
                            return;
                        case 9:
                            switch (msg.arg2) {
                                case 11:
                                    handleRtspLocation(msg);
                                    DM368.this.mRtspStreamMessenger = null;
                                    DM368.this.mDesiredRtspResolution = 0;
                                    return;
                                default:
                                    return;
                            }
                        case 10:
                            handleSDCardStatus(msg);
                            DM368.this.mSDCardStatusMessenger = null;
                            return;
                        default:
                            Log.i(DM368.TAG, "response :" + ((String) msg.obj));
                            return;
                    }
                default:
                    return;
            }
        }

        private void handleSDCardStatus(Message msg) {
            if (msg.obj instanceof String) {
                String rsp = msg.obj;
                if (rsp.startsWith("OK")) {
                    try {
                        int p = rsp.indexOf("sdleft=");
                        int end = rsp.indexOf(85, p);
                        long free = Long.parseLong(rsp.substring("sdleft=".length() + p, end));
                        p = rsp.indexOf("sdused=", end);
                        msg.obj = new SDCardStatus(Integer.parseInt(rsp.substring("sdinsert=".length() + rsp.indexOf("sdinsert="))) == 3, free >> 10, Long.parseLong(rsp.substring("sdused=".length() + p, rsp.indexOf(85, p))) >> 10);
                        notifySender(msg, DM368.this.mSDCardStatusMessenger, 10);
                        return;
                    } catch (Exception e) {
                        Log.e(DM368.TAG, e.getMessage());
                        msg.obj = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                        notifySender(msg, DM368.this.mSDCardStatusMessenger, 10);
                        return;
                    }
                }
                notifySender(msg, DM368.this.mSDCardStatusMessenger, 10);
                return;
            }
            notifySender(msg, DM368.this.mSDCardStatusMessenger, 10);
        }

        private void handleRtspLocation(Message msg) {
            if (msg.obj instanceof HashMap) {
                String resolution;
                HashMap<String, String> properties = msg.obj;
                String rtsp_location = null;
                boolean location_found = false;
                if (DM368.this.mDesiredRtspResolution == 200) {
                    resolution = "720x480";
                } else if (DM368.this.mDesiredRtspResolution == 201) {
                    resolution = "1920*1080";
                } else {
                    Log.w(DM368.TAG, "Unknown Resolution : " + DM368.this.mDesiredRtspResolution);
                    msg.obj = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                    notifySender(msg, DM368.this.mRtspStreamMessenger, 11);
                    return;
                }
                String streamname = (String) properties.get("streamname1");
                if (streamname != null) {
                    rtsp_location = getRtspLocation(streamname, resolution);
                    if (rtsp_location != null) {
                        location_found = true;
                    }
                }
                if (!location_found) {
                    streamname = (String) properties.get("streamname2");
                    if (streamname != null) {
                        rtsp_location = getRtspLocation(streamname, resolution);
                        if (rtsp_location != null) {
                            location_found = true;
                        }
                    }
                }
                if (!location_found) {
                    streamname = (String) properties.get("streamname3");
                    if (streamname != null) {
                        rtsp_location = getRtspLocation(streamname, resolution);
                        if (rtsp_location != null) {
                        }
                    }
                }
                msg.obj = rtsp_location;
                notifySender(msg, DM368.this.mRtspStreamMessenger, 11);
                return;
            }
            notifySender(msg, DM368.this.mRtspStreamMessenger, 11);
        }

        private String getRtspLocation(String streamname, String resolution) {
            if (!streamname.contains(resolution)) {
                return null;
            }
            String[] str = streamname.split("@");
            if (str.length >= 2) {
                return str[1];
            }
            Log.w(DM368.TAG, "a corrupted stream name :" + streamname);
            return null;
        }

        private void notifySender(Message msg, Messenger messenger, int arg1) {
            if (messenger != null) {
                Message message = Message.obtain();
                message.obj = msg.obj;
                message.arg1 = arg1;
                message.what = 1;
                try {
                    messenger.send(message);
                    return;
                } catch (RemoteException e) {
                    Log.e(DM368.TAG, "RemoteException Messenger was killed");
                    return;
                }
            }
            Log.e(DM368.TAG, "No Messenager was Found,notify abort");
        }
    };
    private Messenger mInternalMessenger = new Messenger(this.mInternalHandler);
    private Messenger mRtspStreamMessenger;
    private Messenger mSDCardStatusMessenger;
    private Messenger mStopRecordMessenger;
    private Object mSyncLock = new Object();
    private WorkerThread myWorker = new WorkerThread();

    private class GetMediaFileTask extends AsyncTask<String, Void, String[]> {
        private GetMediaFileCallback callback;

        public GetMediaFileTask(GetMediaFileCallback callback) {
            this.callback = callback;
        }

        protected String[] doInBackground(String... params) {
            return getStreamAndParse(params[0]);
        }

        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            this.callback.MediaFileGot(result);
        }

        private String[] getStreamAndParse(String url) {
            RequestResult result = new RequestResult();
            String content = DM368.this.rawRequestBlock(url, true, result);
            if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(result.result)) {
                return parseHttpContent(content);
            }
            return null;
        }

        private String[] parseHttpContent(String content) {
            ArrayList<String> media = new ArrayList();
            parse(content, DM368.this.REGEX_FORMAT_1, media);
            parse(content, DM368.this.REGEX_FORMAT_2, media);
            parse(content, DM368.this.REGEX_FORMAT_3, media);
            parse(content, DM368.this.REGEX_FORMAT_4, media);
            String[] result = new String[media.size()];
            media.toArray(result);
            return result;
        }

        private void parse(String content, String regex, ArrayList<String> out) {
            if (regex != null) {
                Matcher matcher = Pattern.compile(regex).matcher(content);
                while (matcher.find()) {
                    out.add(matcher.group());
                }
            }
        }
    }

    private class WorkerThread extends Thread {
        private Handler mHandler;
        private Looper myLooper;

        private WorkerThread() {
        }

        public void run() {
            setName("HttpRequest");
            Log.d(DM368.TAG, "Http worker is about to run");
            Looper.prepare();
            Log.d(DM368.TAG, "Http worker prepared");
            this.myLooper = Looper.myLooper();
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    RequestResult rr = new RequestResult();
                    switch (msg.what) {
                        case IPCameraManager.APACHE_GET /*301*/:
                            WorkerThread.this.notifySender(msg, WorkerThread.this.handleSpecialResponse(msg, DM368.this.rawRequestBlock((String) msg.obj, true, rr), rr));
                            return;
                        case IPCameraManager.APACHE_SET_NORESPONSE /*302*/:
                            WorkerThread.this.notifySender(msg, WorkerThread.this.handleSpecialResponse(msg, DM368.this.rawRequestBlock((String) msg.obj, false, rr), rr));
                            return;
                        case IPCameraManager.JDK_GET /*303*/:
                            WorkerThread.this.notifySender(msg, WorkerThread.this.handleSpecialResponse(msg, DM368.this.rawRequestBlockJDK((String) msg.obj, true, rr), rr));
                            return;
                        default:
                            Log.i(DM368.TAG, "Unknown Message:" + msg.what);
                            return;
                    }
                }
            };
            Log.d(DM368.TAG, "Http worker syncing with UI thread");
            synchronized (DM368.this.mSyncLock) {
                DM368.this.mSyncLock.notifyAll();
            }
            Log.d(DM368.TAG, "Http worker ready,start working");
            Looper.loop();
        }

        public void stopThread() {
            if (this.myLooper == null) {
                Log.i(DM368.TAG, "Looper not running,namely,thread is not running");
            } else {
                this.myLooper.quit();
            }
        }

        private Object handleSpecialResponse(Message msg, Object original_rsp, RequestResult rr) {
            Object rsp = DM368.this.onHandleSpecialRequest(msg, original_rsp, rr);
            if (IPCameraManager.SPECIAL_RESPONSE_HANDLED.equals(rr.result)) {
                return rsp;
            }
            Object new_rsp = original_rsp;
            boolean success;
            switch (msg.arg1) {
                case 1:
                    if (!IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                        new_rsp = rr.result;
                        break;
                    }
                    new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_OK;
                    try {
                        Thread.sleep(1000);
                        break;
                    } catch (InterruptedException e) {
                        break;
                    }
                case 9:
                    new_rsp = parseCameraConfig((String) original_rsp);
                    break;
                case 15:
                    if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                        HashMap<String, String> kv = parseParatest((String) original_rsp);
                        success = false;
                        ToneSetting ts = null;
                        try {
                            success = true;
                            ts = new ToneSetting(Integer.parseInt((String) kv.get("brightness")), Integer.parseInt((String) kv.get("contrast")), Integer.parseInt((String) kv.get("sharpness")), Integer.parseInt((String) kv.get("saturation")));
                        } catch (Exception e2) {
                            Log.e(DM368.TAG, "encounter error while parsing paratest:" + e2.getMessage());
                        }
                        if (success) {
                            ToneSetting new_rsp2 = ts;
                            break;
                        }
                    }
                    break;
                case 21:
                    if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                        success = false;
                        Integer rtn = null;
                        try {
                            rtn = Integer.valueOf((String) parseParatest((String) original_rsp).get("getalarmstatus"), 16);
                            success = true;
                        } catch (Exception e22) {
                            Log.e(DM368.TAG, "encounter error while parsing paratest:" + e22.getMessage());
                        }
                        if (success) {
                            Integer new_rsp3 = rtn;
                            break;
                        }
                    }
                    break;
                case 23:
                    if (!IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(rr.result)) {
                        Log.i(DM368.TAG, "HTTP CODE:" + rr.result);
                        new_rsp = IPCameraManager.HTTP_RESPONSE_CODE_UNKNOWN;
                        break;
                    }
                    new_rsp = original_rsp;
                    break;
            }
            return new_rsp;
        }

        private HashMap<String, String> parseParatest(String rsp) {
            HashMap<String, String> rtn = new HashMap();
            String[] ss = rsp.replace("\n", "").split("OK ");
            for (String split : ss) {
                String[] ss1 = split.split("=");
                if (ss1.length >= 2) {
                    rtn.put(ss1[0].trim(), ss1[1].trim());
                }
            }
            return rtn;
        }

        private Object parseCameraConfig(String rsp) {
            String[] properties = rsp.split("<br>");
            if (properties.length == 1) {
                return rsp;
            }
            ArrayList<String> interested_config_copy = new ArrayList(DM368.INTERESTED_CAMERA_CONFIG);
            HashMap<String, String> camera_config = new HashMap();
            for (int i = 0; i < properties.length; i++) {
                int j = 0;
                while (j < interested_config_copy.size()) {
                    if (properties[i].startsWith((String) interested_config_copy.get(j))) {
                        String[] kv = properties[i].split("=");
                        if (kv.length < 2) {
                            Log.w(DM368.TAG, "corrupted camera config");
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (int k = 1; k < kv.length; k++) {
                                sb.append(kv[k]);
                                sb.append("=");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            camera_config.put(kv[0], sb.toString());
                            interested_config_copy.remove(j);
                        }
                        if (interested_config_copy.size() != 0) {
                            break;
                        }
                    } else {
                        j++;
                    }
                }
                if (interested_config_copy.size() != 0) {
                    break;
                }
            }
            return camera_config;
        }

        private void notifySender(Message msg, Object response) {
            if (msg.replyTo == null) {
                Log.i(DM368.TAG, "No Messenager was Found,notify abort");
                return;
            }
            Message message = Message.obtain();
            message.obj = response;
            message.arg1 = msg.arg1;
            message.arg2 = msg.arg2;
            message.what = 1;
            try {
                msg.replyTo.send(message);
            } catch (RemoteException e) {
                Log.e(DM368.TAG, "RemoteException messenger was killed");
                stopThread();
            }
        }
    }

    static {
        INTERESTED_CAMERA_CONFIG.add("sdinsert");
        INTERESTED_CAMERA_CONFIG.add("sdleft");
        INTERESTED_CAMERA_CONFIG.add("sdused");
        INTERESTED_CAMERA_CONFIG.add("streamname1");
        INTERESTED_CAMERA_CONFIG.add("streamname2");
        INTERESTED_CAMERA_CONFIG.add("streamname3");
    }

    protected DM368() {
        this.myWorker.start();
    }

    protected Object onHandleSpecialRequest(Message msg, Object new_rsp, RequestResult rr) {
        return null;
    }

    protected void postNullRequest(Messenger relyTo, int type) {
        if (relyTo != null) {
            Message msg = Message.obtain();
            msg.what = 1;
            msg.arg1 = type;
            msg.obj = IPCameraManager.HTTP_RESPONSE_CODE_OK;
            try {
                relyTo.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void postRequest(String requestUrl, Messenger relyTo, int type, int responseType) {
        postRequest(requestUrl, relyTo, type, 0, responseType);
    }

    private void postRequest(String requestUrl, Messenger relyTo, int type, int subtype, int responseType) {
        if (this.myWorker.mHandler == null) {
            Log.i(TAG, "Worker Thread Handler is null!!");
            synchronized (this.mSyncLock) {
                if (this.myWorker.mHandler == null) {
                    try {
                        this.mSyncLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        Message msg = this.myWorker.mHandler.obtainMessage();
        msg.obj = requestUrl;
        msg.arg1 = type;
        msg.arg2 = subtype;
        if (relyTo != null) {
            msg.replyTo = relyTo;
        }
        msg.what = responseType;
        this.myWorker.mHandler.sendMessage(msg);
    }

    public void sendCustomCommand(String command, Messenger relyTo, int type, int responseType) {
        postRequest(command, relyTo, type, responseType);
    }

    public void sendAPInfo(String SSID, String password, Messenger relyTo) {
        StringBuilder sb = new StringBuilder();
        sb.append(REQUEST_SET).append("?").append("wificonfigsta=").append(SSID).append(':').append(password).append(':').append("1");
        postRequest(sb.toString(), relyTo, 6, IPCameraManager.APACHE_GET);
    }

    public void switchAPStation(String execute) {
        StringBuilder sb = new StringBuilder();
        sb.append(REQUEST_SET).append("?").append("wifitosta=").append(execute);
        postRequest(sb.toString(), null, 7, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void startRecord(Messenger relyTo, String cameraInfo) {
        new Time().setToNow();
        StringBuilder sb = new StringBuilder();
        sb.append(REQUEST_SET).append("?").append("sdrenable=1&schedule=" + String.format("001%02d%02d%02d", new Object[]{Integer.valueOf(8), Integer.valueOf(now.hour), Integer.valueOf(now.minute)}) + "00240000");
        Log.d(TAG, "schedule:" + sb.toString());
        postRequest(sb.toString(), relyTo, 2, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void stopRecord(Messenger relyTo, String cameraInfo) {
        postRequest("vb.htm?delschedule=1", relyTo, 3, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void snapShot(String filename, Messenger relyTo, String cameraInfo) {
        postRequest("vb.htm?clicksnapfilename=" + Uri.encode(filename) + "&" + "clicksnapstorage=0", relyTo, 4, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void zoom(int pos, Messenger relyTo) {
        if (pos < 1) {
            pos = 1;
        } else if (pos > 18) {
            pos = 18;
        }
        postRequest("vb.htm?cameractrl=" + Uri.encode("-d /dev/ttyS1 -c ZoomPos -p " + pos), relyTo, 8, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void syncTime(Messenger relyTo) {
        Time now = new Time();
        now.setToNow();
        StringBuilder sb = new StringBuilder();
        sb.append(REQUEST_SET).append("?").append(REQUEST_SETDATE).append(now.format("%Y/%m/%d")).append("&").append(REQUEST_SETTIME).append(now.format("%H:%M:%S"));
        Log.d(TAG, "Date Time:" + sb.toString());
        postRequest(sb.toString(), relyTo, 1, IPCameraManager.APACHE_GET);
    }

    public void getCameraConfig(Messenger relyTo) {
        getCameraConfigInternal(relyTo, 0);
    }

    private void getCameraConfigInternal(Messenger relyTo, int forWhat) {
        postRequest("ini.htm", relyTo, 9, forWhat, IPCameraManager.APACHE_GET);
    }

    public void finish() {
        this.myWorker.stopThread();
    }

    public void getSDCardStatus(Messenger relyTo) {
        if (this.mSDCardStatusMessenger != null) {
            Log.w(TAG, "getSDCardStatus the previous one was still process, request abort");
            return;
        }
        this.mSDCardStatusMessenger = relyTo;
        postRequest("vb.htm?paratest=sdleft&&paratest=sdused&&paratest=sdinsert", this.mInternalMessenger, 10, IPCameraManager.APACHE_GET);
    }

    public void getRtspStreamLocation(int resolution, Messenger relyTo) {
        if (this.mRtspStreamMessenger != null) {
            Log.w(TAG, "getRtspStreamLocation the previous one was still process, request abort");
            return;
        }
        this.mRtspStreamMessenger = relyTo;
        this.mDesiredRtspResolution = resolution;
        getCameraConfigInternal(this.mInternalMessenger, 11);
    }

    public void setCC4In1Config(Messenger relyTo, Cameras camera) {
        postRequest("vb.htm?cc4in1select=name:" + camera.getName() + "@" + "dr" + ":" + camera.getDr() + "@" + "f" + ":" + camera.getF() + "@" + "n" + ":" + camera.getN() + "@" + "t1" + ":" + camera.getT1() + "@" + "t2" + ":" + camera.getT2() + "@" + "intervalp" + ":" + camera.getIntervalp() + "@" + "codep" + ":" + camera.getCodep() + "@" + "intervalv" + ":" + camera.getIntervalv() + "@" + "codev" + ":" + camera.getCodev(), relyTo, 12, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getMediaFile(GetMediaFileCallback callback) {
        new GetMediaFileTask(callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{this.SERVER_URL + this.FILE_PATH});
    }

    public String rawRequestBlock(String requestUrl, boolean needResponse, RequestResult result) {
        return rawRequestBlock(requestUrl, needResponse, result, this.HTTP_CONNECTION_TIMEOUT, this.HTTP_SOCKET_TIMEOUT);
    }

    public String rawRequestBlock(String requestUrl, boolean needResponse, RequestResult result, int connectionTimeout, int SoTimeout) {
        String request;
        if (requestUrl.startsWith(this.SERVER_URL)) {
            request = requestUrl;
        } else {
            request = this.SERVER_URL + requestUrl;
        }
        if (requestUrl.startsWith("http://192.168.73.254/")) {
            request = requestUrl;
        }
        try {
            String rtn_response;
            HttpGet httpget = new HttpGet(request);
            httpget.addHeader(Authorization_Header, Authorization_Values);
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), connectionTimeout);
            HttpConnectionParams.setSoTimeout(client.getParams(), SoTimeout);
            try {
                HttpResponse response = client.execute(httpget);
                if (response.getStatusLine().getStatusCode() != 200) {
                    rtn_response = response.getStatusLine().getReasonPhrase();
                    Log.e(TAG, "rtn_response: " + rtn_response + ", response: " + response.toString());
                    putRequestResult(result, rtn_response);
                } else if (needResponse) {
                    InputStream is = response.getEntity().getContent();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    while (true) {
                        String line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line);
                    }
                    br.close();
                    is.close();
                    rtn_response = sb.toString();
                    putRequestResult(result, IPCameraManager.HTTP_RESPONSE_CODE_OK);
                } else {
                    rtn_response = IPCameraManager.HTTP_RESPONSE_CODE_OK;
                    putRequestResult(result, IPCameraManager.HTTP_RESPONSE_CODE_OK);
                }
                client.getConnectionManager().shutdown();
            } catch (ClientProtocolException e) {
                Log.e(TAG, "ClientProtocolException :" + e.getMessage());
                rtn_response = IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR;
                putRequestResult(result, rtn_response);
            } catch (ConnectTimeoutException e2) {
                Log.e(TAG, "ConnectTimeoutException :" + e2.getMessage());
                rtn_response = IPCameraManager.HTTP_RESPONSE_CODE_CONN_TIMEOUT;
                putRequestResult(result, rtn_response);
            } catch (SocketTimeoutException e3) {
                Log.e(TAG, "SocketTimeoutException :" + e3.getMessage());
                rtn_response = IPCameraManager.HTTP_RESPONSE_CODE_RSP_TIMEOUT;
                putRequestResult(result, rtn_response);
            } catch (IOException e4) {
                Log.e(TAG, "IOException :" + e4.getMessage());
                rtn_response = IPCameraManager.HTTP_RESPONSE_CODE_IO_EXP;
                putRequestResult(result, rtn_response);
            }
            HttpGet httpGet = httpget;
            return rtn_response;
        } catch (IllegalArgumentException e5) {
            Log.e(TAG, "IllegalArgumentException :" + e5.getMessage());
            putRequestResult(result, IPCameraManager.HTTP_RESPONSE_CODE_BAD_REQUEST);
            return IPCameraManager.HTTP_RESPONSE_CODE_BAD_REQUEST;
        }
    }

    public String rawRequestBlockJDK(String requestUrl, boolean needResponse, RequestResult result) {
        return rawRequestBlockJDK(requestUrl, needResponse, result, this.HTTP_CONNECTION_TIMEOUT, this.HTTP_SOCKET_TIMEOUT);
    }

    public String rawRequestBlockJDK(String requestUrl, boolean needResponse, RequestResult result, int connectionTimeout, int SoTimeout) {
        String request;
        String rtn_response;
        HttpURLConnection connection = null;
        if (requestUrl.startsWith(this.SERVER_URL)) {
            request = requestUrl;
        } else {
            request = this.SERVER_URL + requestUrl;
        }
        try {
            connection = (HttpURLConnection) new URL(request).openConnection();
            if (needResponse) {
                InputStream inStr = connection.getInputStream();
                InputStreamReader inStrRdr = new InputStreamReader(inStr);
                BufferedReader br = new BufferedReader(inStrRdr);
                StringBuffer sb = new StringBuffer();
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                }
                br.close();
                inStrRdr.close();
                inStr.close();
                rtn_response = sb.toString();
                putRequestResult(result, IPCameraManager.HTTP_RESPONSE_CODE_OK);
            } else {
                rtn_response = IPCameraManager.HTTP_RESPONSE_CODE_OK;
                putRequestResult(result, IPCameraManager.HTTP_RESPONSE_CODE_OK);
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException :" + e.getMessage());
            rtn_response = IPCameraManager.HTTP_RESPONSE_CODE_BAD_REQUEST;
            putRequestResult(result, rtn_response);
            if (connection != null) {
                connection.disconnect();
            }
        } catch (IOException e2) {
            Log.e(TAG, "IOException :" + e2.getMessage());
            rtn_response = IPCameraManager.HTTP_RESPONSE_CODE_IO_EXP;
            putRequestResult(result, rtn_response);
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Throwable th) {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return rtn_response;
    }

    private void putRequestResult(RequestResult result, String rsp) {
        if (result != null) {
            result.result = rsp;
        }
    }

    public void formatSDCard(Messenger relyTo) {
        postRequest("vb.htm?sdformat=1", relyTo, 14, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getCameraToneSetting(Messenger relyTo) {
        postRequest("vb.htm?paratest=brightness&paratest=contrast&paratest=sharpness&paratest=saturation", relyTo, 15, IPCameraManager.APACHE_GET);
    }

    public void setBrightness(Messenger relyTo, int brightness) {
        postRequest("vb.htm?cameractrl=" + Uri.encode("-d /dev/ttyS1 -c BrightnessPos  -p " + brightness), relyTo, 16, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void setSharpness(Messenger relyTo, int sharpness) {
        postRequest("vb.htm?sharpness=" + sharpness, relyTo, 18, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void setContrast(Messenger relyTo, int contrast) {
        postRequest("vb.htm?cameractrl=" + Uri.encode("-d /dev/ttyS1 -c AperturePos  -p " + contrast), relyTo, 17, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void setSaturation(Messenger relyTo, int saturation) {
        postRequest("vb.htm?saturation=" + saturation, relyTo, 19, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void resetToneSetting(Messenger relyTo) {
        postRequest("vb.htm?brightness=98&contrast=128&sharpness=128&saturation=128", relyTo, 20, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void isRecording(Messenger relyTo) {
        postRequest("vb.htm?getalarmstatus", relyTo, 21, IPCameraManager.APACHE_GET);
    }

    public void switchMode(int mode, Messenger relyTo) {
        postRequest("vb.htm?setcameramode=" + mode, relyTo, 22, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getVersion(Messenger relyTo) {
        postRequest("version.txt", relyTo, 23, IPCameraManager.APACHE_GET);
    }

    public void init() {
        this.SERVER_URL = "http://192.168.73.254/";
        this.FILE_PATH = "sdget.htm";
        this.REGEX_FORMAT_1 = "IMG_[\\w]*\\.jpg";
        this.REGEX_FORMAT_2 = "MOV_[\\w]*\\.avi";
        this.REGEX_FORMAT_3 = "\\d{14}\\.jpg";
        this.REGEX_FORMAT_4 = "\\d{14}\\.avi";
    }

    public void initCamera(Messenger relyTo) {
        postNullRequest(relyTo, 24);
        Log.w(TAG, "Invalid command----initCamera");
    }

    public void getRecordTime(Messenger relyTo) {
        postNullRequest(relyTo, 25);
        Log.w(TAG, "Invalid command----getRecordTime");
    }

    public void restartVF(Messenger relyTo) {
        postNullRequest(relyTo, 26);
        Log.w(TAG, "Invalid command----restVF");
    }

    public void stopVF(Messenger relyTo) {
        postNullRequest(relyTo, 27);
        Log.w(TAG, "Invalid command----stopVF");
    }

    public void setPhotoSize(Messenger relyTo, int mode) {
        postNullRequest(relyTo, 28);
        Log.w(TAG, "Invalid command----setPhotoSize");
    }

    public void getPhotoSize(Messenger relyTo) {
        postNullRequest(relyTo, 29);
        Log.w(TAG, "Invalid command----getPhotoSize");
    }

    public void getBattery(Messenger relyTo) {
        postNullRequest(relyTo, 30);
        Log.w(TAG, "Invalid command----getBattery");
    }

    public void setVideoResolution(Messenger relyTo, int res) {
        postNullRequest(relyTo, 31);
        Log.w(TAG, "Invalid command----setResolution");
    }

    public void getVideoResolution(Messenger relyTo) {
        postNullRequest(relyTo, 32);
        Log.w(TAG, "Invalid command----getResolution");
    }

    public void setVideoStandard(Messenger relyTo, int param) {
        postNullRequest(relyTo, 33);
        Log.w(TAG, "Invalid command----setVideoStandard");
    }

    public void getVideoStandard(Messenger relyTo) {
        postNullRequest(relyTo, 34);
        Log.w(TAG, "Invalid command----getVideoStandard");
    }

    public void setFieldOfView(Messenger relyTo, int param) {
        postNullRequest(relyTo, 35);
        Log.w(TAG, "Invalid command----setFieldOfView");
    }

    public void getFieldOfView(Messenger relyTo) {
        postNullRequest(relyTo, 36);
        Log.w(TAG, "Invalid command----getFieldOfView");
    }

    public void getWorkStatus(Messenger relyTo) {
        postNullRequest(relyTo, 37);
        Log.w(TAG, "Invalid command----getWorkStatus");
    }

    public void getSDCardSpace(Messenger relyTo) {
        postNullRequest(relyTo, 38);
        Log.w(TAG, "Invalid command----getSDCardSpace");
    }

    public void getSDCardFreeSpace(Messenger relyTo) {
        postNullRequest(relyTo, 39);
        Log.w(TAG, "Invalid command----getSDCardFreeSpace");
    }

    public void getSDCardFormat(Messenger relyTo) {
        postNullRequest(relyTo, 40);
        Log.w(TAG, "Invalid command----getSDCardFormat");
    }

    public void setPhotoMode(Messenger relyTo, int mode) {
        postNullRequest(relyTo, 41);
        Log.w(TAG, "Invalid command----setPhotoMode");
    }

    public void getPhotoMode(Messenger relyTo) {
        postNullRequest(relyTo, 42);
        Log.w(TAG, "Invalid command----getPhotoMode");
    }

    public void resetDefault(Messenger relyTo) {
        postNullRequest(relyTo, 43);
        Log.w(TAG, "Invalid command----resetDefault");
    }

    public void getDeviceStatus(Messenger relyTo) {
        postNullRequest(relyTo, 44);
        Log.w(TAG, "Invalid command----resetDefault");
    }

    public void setAudioState(Messenger relyTo, int state) {
        postNullRequest(relyTo, 59);
        Log.w(TAG, "Invalid command----resetDefault");
    }

    public void getAudioState(Messenger relyTo) {
        postNullRequest(relyTo, 59);
        Log.w(TAG, "Invalid command----resetDefault");
    }
}
