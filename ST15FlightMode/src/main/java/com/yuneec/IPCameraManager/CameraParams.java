package com.yuneec.IPCameraManager;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CameraParams implements Parcelable {
    public static final Creator<CameraParams> CREATOR = new Creator<CameraParams>() {
        public CameraParams createFromParcel(Parcel source) {
            return new CameraParams(source);
        }

        public CameraParams[] newArray(int size) {
            return new CameraParams[size];
        }
    };
    public int ae_enable;
    public int audio_enable;
    public int audio_sw;
    public int cam_mode;
    public String exposure_value;
    public String fw_ver;
    public int iq_type;
    public String iso;
    public String photo_format;
    public int record_time;
    public String response;
    public int sdFree;
    public int sdTotal;
    public int shutter_time;
    public String status;
    public String video_mode;
    public int white_balance;

    public CameraParams(Parcel source) {
        this.response = source.readString();
        this.fw_ver = source.readString();
        this.cam_mode = source.readInt();
        this.status = source.readString();
        this.iq_type = source.readInt();
        this.white_balance = source.readInt();
        this.sdFree = source.readInt();
        this.sdTotal = source.readInt();
        this.exposure_value = source.readString();
        this.video_mode = source.readString();
        this.record_time = source.readInt();
        this.ae_enable = source.readInt();
        this.audio_sw = source.readInt();
        this.shutter_time = source.readInt();
        this.iso = source.readString();
        this.photo_format = source.readString();
        this.audio_enable = source.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(this.response);
        arg0.writeString(this.fw_ver);
        arg0.writeInt(this.cam_mode);
        arg0.writeString(this.status);
        arg0.writeInt(this.iq_type);
        arg0.writeInt(this.white_balance);
        arg0.writeInt(this.sdFree);
        arg0.writeInt(this.sdTotal);
        arg0.writeString(this.exposure_value);
        arg0.writeString(this.video_mode);
        arg0.writeInt(this.record_time);
        arg0.writeInt(this.ae_enable);
        arg0.writeInt(this.audio_sw);
        arg0.writeInt(this.shutter_time);
        arg0.writeString(this.iso);
        arg0.writeString(this.photo_format);
        arg0.writeInt(this.audio_enable);
    }
}
