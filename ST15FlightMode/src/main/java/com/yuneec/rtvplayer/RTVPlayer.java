package com.yuneec.rtvplayer;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class RTVPlayer {
    public static final int IMAGE_FORMAT_RGB565 = 3;
    public static final int IMAGE_FORMAT_RV32 = 2;
    public static final int IMAGE_FORMAT_YV12 = 1;
    public static final int PLAYER_FFMPEG = 2;
    public static final int PLAYER_VLC = 1;
    protected VideoEventCallback mVideoEventCallback;

    public interface VideoEventCallback {
        void onPlayerEncoutneredError();

        void onPlayerEndReached();

        void onPlayerPlayerRecordingFinished();

        void onPlayerPlaying();

        void onPlayerRecordableChanged();

        void onPlayerSnapshotTaken();

        void onPlayerStopped();

        void onPlayerSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3);

        void onPlayerSurfaceCreated(SurfaceHolder surfaceHolder);

        void onPlayerSurfaceDestroyed(SurfaceHolder surfaceHolder);
    }

    public abstract boolean canRecord();

    public abstract void deinit();

    public abstract void init(Context context, int i, boolean z);

    public abstract boolean isPlaying();

    public abstract boolean isRecording();

    public abstract boolean play();

    public abstract boolean play(String str);

    public abstract void setSurfaceView(SurfaceView surfaceView);

    public abstract void setVideoLocation(String str);

    public abstract int snapShot(int i, String str, int i2, int i3);

    public abstract int startRecord(String str);

    public abstract void stop();

    public abstract int stopRecord();

    public static RTVPlayer getPlayer(int which_player) {
        switch (which_player) {
            case 1:
                return new VLC();
            case 2:
                return new FFmpeg();
            default:
                throw new IllegalArgumentException("Unknown RTVPlayer :" + which_player);
        }
    }

    public void setVideoEventCallback(VideoEventCallback callback) {
        this.mVideoEventCallback = callback;
    }
}
