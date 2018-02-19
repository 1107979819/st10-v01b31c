package com.yuneec.IPCameraManager;

import android.content.Context;
import android.os.Messenger;
import android.util.Log;
import com.yuneec.IPCameraManager.cgo4.LumixGH4;

public abstract class IPCameraManager {
    public static final int APACHE_GET = 301;
    public static final int APACHE_SET_NORESPONSE = 302;
    public static final int CAMERA_AMBA = 102;
    public static final int CAMERA_AMBA_2 = 104;
    public static final int CAMERA_CC4IN1 = 101;
    public static final int CAMERA_DM368 = 100;
    public static final int CAMERA_GOPRO = 105;
    public static final int CAMERA_LUMIX_GH4 = 106;
    public static final int CAMERA_NUVOTON = 103;
    public static final int HTTP_RESPONSE = 1;
    public static final String HTTP_RESPONSE_CODE_BAD_REQUEST = "HTTPCODE Bad Request";
    public static final String HTTP_RESPONSE_CODE_CONN_TIMEOUT = "HTTPCODE Connect Timeout";
    public static final String HTTP_RESPONSE_CODE_HEADER = "HTTPCODE ";
    public static final String HTTP_RESPONSE_CODE_INTERNAL_ERROR = "HTTPCODE Internal Error";
    public static final String HTTP_RESPONSE_CODE_IO_EXP = "HTTPCODE IOException";
    public static final String HTTP_RESPONSE_CODE_OK = "HTTPCODE OK";
    public static final String HTTP_RESPONSE_CODE_RSP_TIMEOUT = "HTTPCODE Response Timeout";
    public static final String HTTP_RESPONSE_CODE_UNKNOWN = "Unknown";
    public static final String HTTP_RESPONSE_CODE_USER_CANCELLED = "HTTPCODE User Cancelled";
    public static final int JDK_GET = 303;
    public static final int JDK_SET_NORESPONSE = 304;
    public static final int PHOTO_MODE = 0;
    public static final int RECORD_MODE = 1;
    public static final int REQUEST_BIND_CAMERA = 1001;
    public static final int REQUEST_BIND_STATE = 1002;
    public static final int REQUEST_CAMERA_CONFIG = 9;
    public static final int REQUEST_FIX_FOCUS = 64;
    public static final int REQUEST_FORMAT_SDCARD = 14;
    public static final int REQUEST_GET_AE_ENABLE = 48;
    public static final int REQUEST_GET_APERTURE = 71;
    public static final int REQUEST_GET_AUDIO_STATE = 60;
    public static final int REQUEST_GET_BETTERY = 30;
    public static final int REQUEST_GET_CAMERA_MODE = 62;
    public static final int REQUEST_GET_CURRENT_MENU = 74;
    public static final int REQUEST_GET_DEVICE_STATUS = 44;
    public static final int REQUEST_GET_EXPOSURE_VALUE = 56;
    public static final int REQUEST_GET_FOV = 36;
    public static final int REQUEST_GET_IQ_TYPE = 52;
    public static final int REQUEST_GET_ISO = 67;
    public static final int REQUEST_GET_LENS_INFO = 79;
    public static final int REQUEST_GET_MEDIA_FILE = 13;
    public static final int REQUEST_GET_PHOTO_FORMAT = 46;
    public static final int REQUEST_GET_PHOTO_MODE = 42;
    public static final int REQUEST_GET_PHOTO_SIZE = 29;
    public static final int REQUEST_GET_RECORD_MODE = 72;
    public static final int REQUEST_GET_REC_TIME = 25;
    public static final int REQUEST_GET_SDCARD_FORMAT = 40;
    public static final int REQUEST_GET_SDCARD_FREE_SPACE = 39;
    public static final int REQUEST_GET_SDCARD_SPACE = 38;
    public static final int REQUEST_GET_SHUTTER_TIME = 69;
    public static final int REQUEST_GET_SH_TM_ISO = 50;
    public static final int REQUEST_GET_SINGLE_MENU = 82;
    public static final int REQUEST_GET_VIDEO_MODE = 58;
    public static final int REQUEST_GET_VIDEO_RESOLUTION = 32;
    public static final int REQUEST_GET_VIDEO_STANDARD = 34;
    public static final int REQUEST_GET_WHITEBALANCE_MODE = 54;
    public static final int REQUEST_GET_WORK_STATUS = 37;
    public static final int REQUEST_INIT = 24;
    public static final int REQUEST_IS_RECORDING = 21;
    public static final int REQUEST_MANUAL_FOCUS = 65;
    public static final int REQUEST_RESET_DEFAULT = 43;
    public static final int REQUEST_RESET_STATUS = 63;
    public static final int REQUEST_RESET_TONE = 20;
    public static final int REQUEST_REST_VF = 26;
    public static final int REQUEST_RTSP_LOACTION = 11;
    public static final int REQUEST_SDCARD_STATUS = 10;
    public static final int REQUEST_SET_AE_ENABLE = 47;
    public static final int REQUEST_SET_APERTURE = 70;
    public static final int REQUEST_SET_AUDIO_STATE = 59;
    public static final int REQUEST_SET_CAMERA_CONFIG = 12;
    public static final int REQUEST_SET_CAMERA_MODE = 61;
    public static final int REQUEST_SET_CURRENT_MENU = 75;
    public static final int REQUEST_SET_EXPOSURE_VALUE = 55;
    public static final int REQUEST_SET_FOV = 35;
    public static final int REQUEST_SET_IQ_TYPE = 51;
    public static final int REQUEST_SET_ISO = 66;
    public static final int REQUEST_SET_PHOTO_FORMAT = 45;
    public static final int REQUEST_SET_PHOTO_MODE = 41;
    public static final int REQUEST_SET_PHOTO_SIZE = 28;
    public static final int REQUEST_SET_PROGRAM_SHIFT = 78;
    public static final int REQUEST_SET_RECORD_MODE = 73;
    public static final int REQUEST_SET_SHUTTER_TIME = 68;
    public static final int REQUEST_SET_SH_TM_ISO = 49;
    public static final int REQUEST_SET_VIDEO_MODE = 57;
    public static final int REQUEST_SET_VIDEO_RESOLUTION = 31;
    public static final int REQUEST_SET_VIDEO_STANDARD = 33;
    public static final int REQUEST_SET_WHITEBLANCE_MODE = 53;
    public static final int REQUEST_START_UDP = 76;
    public static final int REQUEST_STOP_UDP = 77;
    public static final int REQUEST_STOP_VF = 27;
    public static final int REQUEST_SWITCH_MODE = 22;
    public static final int REQUEST_TONE_BRIGHTNESS = 16;
    public static final int REQUEST_TONE_CONTRAST = 17;
    public static final int REQUEST_TONE_SATURATION = 19;
    public static final int REQUEST_TONE_SETTINS = 15;
    public static final int REQUEST_TONE_SHARPNESS = 18;
    public static final int REQUEST_TRUN_ON_CAMERA = 80;
    public static final int REQUEST_TURN_OFF_CAMERA = 81;
    public static final int REQUEST_TYPE_AP_STATTION = 7;
    public static final int REQUEST_TYPE_AP_SWITCH_COMMAND = 6;
    public static final int REQUEST_TYPE_SDDEL = 5;
    public static final int REQUEST_TYPE_SNAPSHOT = 4;
    public static final int REQUEST_TYPE_START_RECORD = 2;
    public static final int REQUEST_TYPE_STOP_RECORD = 3;
    public static final int REQUEST_TYPE_SYNC_TIME = 1;
    public static final int REQUEST_TYPE_UNSPECIFIC = 0;
    public static final int REQUEST_TYPE_ZOOM = 8;
    public static final int REQUEST_VERSION = 23;
    public static final int RTSP_RESOLUTION_1920X1080 = 201;
    public static final int RTSP_RESOLUTION_720x480 = 200;
    public static final int SD_STATUS_ERROR = -2;
    public static final int SD_STATUS_FULL = -1;
    public static final int SD_STATUS_NA = 0;
    public static final int SD_STATUS_NORMAL = 1;
    public static final String SPECIAL_RESPONSE_HANDLED = "special_response_handled";
    public static final String SPECIAL_RESPONSE_NOT_HANDLED = "special_response_not_handled";
    private static final String TAG = "IPCameraManager";
    protected String FILE_PATH = "sdget.htm";
    protected int HTTP_CONNECTION_TIMEOUT = 5000;
    protected int HTTP_SOCKET_TIMEOUT = 5000;
    protected String REGEX_FORMAT_1 = "IMG_[\\w]*\\.jpg";
    protected String REGEX_FORMAT_2 = "MOV_[\\w]*\\.avi";
    protected String REGEX_FORMAT_3 = "\\d{14}\\.jpg";
    protected String REGEX_FORMAT_4 = "\\d{14}\\.avi";
    protected String SERVER_URL = "http://192.168.73.254/";

    public static class Bettery {
        public int level;

        public Bettery(int level) {
            this.level = level;
        }
    }

    public static class DevicesStatus {
        public int sd_free;
        public int sd_status;
        public int sd_total;
        public String video_status;

        public DevicesStatus(int sd_free, int sd_total, int sd_status, String video_status) {
            this.sd_free = sd_free;
            this.sd_total = sd_total;
            this.sd_status = sd_status;
            this.video_status = video_status;
        }
    }

    public interface GetMediaFileCallback {
        void MediaFileGot(String[] strArr);
    }

    public static class PhotoMode {
        public int photoMode;

        public PhotoMode(int photoMode) {
            this.photoMode = photoMode;
        }
    }

    public static class PhotoSize {
        public int photoSize;

        public PhotoSize(int photoSize) {
            this.photoSize = photoSize;
        }
    }

    public static class RecordStatus {
        public boolean isRecording;

        public RecordStatus(boolean isRecording) {
            this.isRecording = isRecording;
        }
    }

    public static class RecordTime {
        public int recTime;

        public RecordTime(int recTime) {
            this.recTime = recTime;
        }
    }

    public static class RequestResult {
        public String result;
    }

    public static class SDCardFormat {
        public int format;

        public SDCardFormat(int format) {
            this.format = format;
        }
    }

    public static class SDCardFreeSpace {
        public int freeSpace;

        public SDCardFreeSpace(int freeSpace) {
            this.freeSpace = freeSpace;
        }
    }

    public static class SDCardStatus {
        public long free_space;
        public boolean isInsert;
        public long used_space;

        public SDCardStatus(boolean isInsert, long free_space, long used_space) {
            this.isInsert = isInsert;
            this.free_space = free_space;
            this.used_space = used_space;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SDCardStatus other = (SDCardStatus) obj;
            if (this.free_space != other.free_space) {
                return false;
            }
            if (this.isInsert != other.isInsert) {
                return false;
            }
            if (this.used_space != other.used_space) {
                return false;
            }
            return true;
        }
    }

    public static class SDCardTotalSpace {
        public int totalSpace;

        public SDCardTotalSpace(int totalSpace) {
            this.totalSpace = totalSpace;
        }
    }

    public static class ShutterTimeISO {
        public String iso;
        public int time;
    }

    public static class ToneSetting {
        public int brightness;
        public int contrast;
        public int saturation;
        public int sharpness;

        public ToneSetting(int brightness, int contrast, int sharpness, int saturation) {
            this.brightness = brightness;
            this.contrast = contrast;
            this.sharpness = sharpness;
            this.saturation = saturation;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ToneSetting other = (ToneSetting) obj;
            if (this.brightness != other.brightness) {
                return false;
            }
            if (this.contrast != other.contrast) {
                return false;
            }
            if (this.saturation != other.saturation) {
                return false;
            }
            if (this.sharpness != other.sharpness) {
                return false;
            }
            return true;
        }
    }

    public abstract void finish();

    public abstract void formatSDCard(Messenger messenger);

    public abstract void getAudioState(Messenger messenger);

    public abstract void getBattery(Messenger messenger);

    public abstract void getCameraConfig(Messenger messenger);

    public abstract void getCameraToneSetting(Messenger messenger);

    public abstract void getDeviceStatus(Messenger messenger);

    public abstract void getFieldOfView(Messenger messenger);

    public abstract void getMediaFile(GetMediaFileCallback getMediaFileCallback);

    public abstract void getPhotoMode(Messenger messenger);

    public abstract void getPhotoSize(Messenger messenger);

    public abstract void getRecordTime(Messenger messenger);

    public abstract void getRtspStreamLocation(int i, Messenger messenger);

    public abstract void getSDCardFormat(Messenger messenger);

    public abstract void getSDCardFreeSpace(Messenger messenger);

    public abstract void getSDCardSpace(Messenger messenger);

    public abstract void getSDCardStatus(Messenger messenger);

    public abstract void getVersion(Messenger messenger);

    public abstract void getVideoResolution(Messenger messenger);

    public abstract void getVideoStandard(Messenger messenger);

    public abstract void getWorkStatus(Messenger messenger);

    public abstract void init();

    public abstract void initCamera(Messenger messenger);

    public abstract void isRecording(Messenger messenger);

    public abstract String rawRequestBlock(String str, boolean z, RequestResult requestResult);

    public abstract String rawRequestBlock(String str, boolean z, RequestResult requestResult, int i, int i2);

    public abstract String rawRequestBlockJDK(String str, boolean z, RequestResult requestResult);

    public abstract String rawRequestBlockJDK(String str, boolean z, RequestResult requestResult, int i, int i2);

    public abstract void resetDefault(Messenger messenger);

    public abstract void resetToneSetting(Messenger messenger);

    public abstract void restartVF(Messenger messenger);

    public abstract void sendAPInfo(String str, String str2, Messenger messenger);

    public abstract void sendCustomCommand(String str, Messenger messenger, int i, int i2);

    public abstract void setAudioState(Messenger messenger, int i);

    public abstract void setBrightness(Messenger messenger, int i);

    public abstract void setCC4In1Config(Messenger messenger, Cameras cameras);

    public abstract void setContrast(Messenger messenger, int i);

    public abstract void setFieldOfView(Messenger messenger, int i);

    public abstract void setPhotoMode(Messenger messenger, int i);

    public abstract void setPhotoSize(Messenger messenger, int i);

    public abstract void setSaturation(Messenger messenger, int i);

    public abstract void setSharpness(Messenger messenger, int i);

    public abstract void setVideoResolution(Messenger messenger, int i);

    public abstract void setVideoStandard(Messenger messenger, int i);

    public abstract void snapShot(String str, Messenger messenger, String str2);

    public abstract void startRecord(Messenger messenger, String str);

    public abstract void stopRecord(Messenger messenger, String str);

    public abstract void stopVF(Messenger messenger);

    public abstract void switchAPStation(String str);

    public abstract void switchMode(int i, Messenger messenger);

    public abstract void syncTime(Messenger messenger);

    public abstract void zoom(int i, Messenger messenger);

    public static IPCameraManager getIPCameraManager(Context context, int which_camera) {
        IPCameraManager instance;
        switch (which_camera) {
            case 100:
                instance = new DM368();
                instance.init();
                return instance;
            case 101:
                instance = new CC4in1();
                instance.init();
                return instance;
            case 102:
                instance = new Amba();
                instance.init();
                return instance;
            case 103:
                instance = new Nuvoton();
                instance.init();
                return instance;
            case 104:
                instance = new Amba2();
                instance.init();
                return instance;
            case 105:
                instance = new GOPro();
                instance.init();
                return instance;
            case 106:
                instance = new LumixGH4();
                instance.init();
                return instance;
            default:
                Log.e(TAG, "Unknown Camera: " + which_camera);
                return null;
        }
    }
}
