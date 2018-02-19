package com.yuneec.rtvplayer;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

public class VLC extends RTVPlayer implements Callback, IVideoPlayer {
    private static final int SURFACE_SIZE_CHANED = 100;
    private static final String TAG = "RTVPlayer_VLC";
    private Handler eventHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.getData().getInt("event")) {
                case EventHandler.MediaPlayerPlaying /*260*/:
                    Log.i(VLC.TAG, "MediaPlayerPlaying");
                    break;
                case EventHandler.MediaPlayerPaused /*261*/:
                    Log.i(VLC.TAG, "MediaPlayerPaused");
                    break;
                case EventHandler.MediaPlayerStopped /*262*/:
                    Log.i(VLC.TAG, "MediaPlayerStopped");
                    break;
                case EventHandler.MediaPlayerEndReached /*265*/:
                    if (VLC.this.mVideoEventCallback != null) {
                        VLC.this.mVideoEventCallback.onPlayerEndReached();
                        break;
                    }
                    break;
                case EventHandler.MediaPlayerEncounteredError /*266*/:
                    Log.i(VLC.TAG, "MediaPlayerEncounteredError");
                    if (VLC.this.mVideoEventCallback != null) {
                        VLC.this.mVideoEventCallback.onPlayerEncoutneredError();
                        break;
                    }
                    break;
                case EventHandler.MediaPlayerPositionChanged /*268*/:
                case EventHandler.MediaPlayerVout /*274*/:
                    break;
                case EventHandler.MediaPlayerSnapshotTaken /*272*/:
                    Log.i(VLC.TAG, "MediaPlayerSnapshotTaken");
                    break;
                case EventHandler.MediaPlayerRecordableChanged /*275*/:
                    Log.i(VLC.TAG, "MediaPlayerRecordableChanged");
                    break;
                case EventHandler.MediaPlayerRecordingFinished /*276*/:
                    Log.i(VLC.TAG, "MediaPlayerRecordingFinished");
                    break;
                default:
                    Log.e(VLC.TAG, String.format("Event not handled (0x%x)", new Object[]{Integer.valueOf(msg.getData().getInt("event"))}));
                    break;
            }
            switch (msg.what) {
                case 100:
                    VLC.this.mSurfaceView.getHolder().setFixedSize((VLC.this.mVideoWidth + VLC.this.mSurfaceAlign) & (VLC.this.mSurfaceAlign ^ -1), VLC.this.mVideoHeight);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean isSurfaceReady;
    private int mImageFormat;
    private LibVLC mLibVLC;
    private int mSurfaceAlign;
    private SurfaceView mSurfaceView;
    private int mVideoHeight;
    private String mVideoLocation;
    private int mVideoWidth;

    public void init(Context application_context, int image_format, boolean hardware_accelerate) {
        if (this.mLibVLC != null) {
            Log.i(TAG, "VLC Player has already been prepared");
            return;
        }
        String chroma;
        switch (image_format) {
            case 1:
                chroma = "YV12";
                this.mImageFormat = image_format;
                break;
            case 2:
                chroma = "RV32";
                this.mImageFormat = image_format;
                break;
            default:
                try {
                    Log.i(TAG, "Unknown image format, set to default RGB8888");
                    chroma = "RV32";
                    this.mImageFormat = 2;
                    break;
                } catch (LibVlcException e) {
                    Log.d(TAG, "LibVLC initialisation failed");
                    return;
                }
        }
        this.mLibVLC = LibVLC.getInstance();
        this.mLibVLC.setIomx(hardware_accelerate);
        this.mLibVLC.setSubtitlesEncoding("");
        this.mLibVLC.setTimeStretching(false);
        this.mLibVLC.setChroma(chroma);
        this.mLibVLC.setVerboseMode(true);
        this.mLibVLC.setAout(-1);
        this.mLibVLC.init(application_context);
        EventHandler.getInstance().addHandler(this.eventHandler);
    }

    public void deinit() {
        EventHandler.getInstance().removeHandler(this.eventHandler);
    }

    public void setSurfaceView(SurfaceView surface_view) {
        if (this.mLibVLC == null) {
            Log.e(TAG, "Initialize the VLC before set SurfaceView");
        } else if (surface_view == null) {
            Log.e(TAG, "no surface view was assgined");
        } else if (this.isSurfaceReady) {
            Log.e(TAG, "the surface view is ready for play,destroy it before reassign");
        } else {
            int pitch;
            this.mSurfaceView = surface_view;
            surface_view.getHolder().addCallback(this);
            if (this.mImageFormat == 1) {
                surface_view.getHolder().setFormat(842094169);
                pitch = ImageFormat.getBitsPerPixel(842094169);
            } else {
                surface_view.getHolder().setFormat(2);
                PixelFormat info = new PixelFormat();
                PixelFormat.getPixelFormatInfo(2, info);
                pitch = info.bytesPerPixel;
            }
            this.mSurfaceAlign = (16 / pitch) - 1;
        }
    }

    public void setVideoLocation(String url) {
        this.mVideoLocation = url;
    }

    public boolean play() {
        if (this.mLibVLC == null) {
            Log.e(TAG, "play -- Initialize the VLC first");
            return false;
        } else if (this.mLibVLC.isPlaying()) {
            Log.i(TAG, "Player has already been played");
            return false;
        } else {
            if (!this.isSurfaceReady) {
                int i = 0;
                while (!this.isSurfaceReady && i < 10) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    i++;
                }
                if (i >= 10 && !this.isSurfaceReady) {
                    Log.i(TAG, "Surface initializing,pls wait");
                    return false;
                }
            }
            if (this.mVideoLocation == null) {
                Log.e(TAG, "tell me the video location first");
                return false;
            }
            this.mLibVLC.readMedia(this.mVideoLocation);
            return true;
        }
    }

    public boolean play(String location) {
        this.mVideoLocation = location;
        return play();
    }

    public void stop() {
        if (this.mLibVLC == null) {
            Log.e(TAG, "stop -- Initialize the VLC first");
        } else if (this.mLibVLC.isPlaying()) {
            this.mLibVLC.stop();
        } else {
            Log.i(TAG, "Player has already been stopped");
        }
    }

    public boolean isPlaying() {
        if (this.mLibVLC != null) {
            return this.mLibVLC.isPlaying();
        }
        Log.e(TAG, "isPlaying -- Initialize the VLC first");
        return false;
    }

    public boolean canRecord() {
        if (this.mLibVLC != null) {
            return this.mLibVLC.canRecord();
        }
        Log.e(TAG, "canRecord -- Initialize the VLC first");
        return false;
    }

    public boolean isRecording() {
        if (this.mLibVLC != null) {
            return this.mLibVLC.isRecording();
        }
        Log.e(TAG, "isRecording -- Initialize the VLC first");
        return false;
    }

    public int startRecord(String fullpathname) {
        if (this.mLibVLC != null) {
            return this.mLibVLC.startRecord(fullpathname);
        }
        Log.e(TAG, "startRecord -- Initialize the VLC first");
        return -1;
    }

    public int stopRecord() {
        if (this.mLibVLC != null) {
            return this.mLibVLC.stopRecord();
        }
        Log.e(TAG, "stopRecod -- Initialize the VLC first");
        return -1;
    }

    public int snapShot(int track_id, String fullpathname, int width, int height) {
        if (this.mLibVLC != null) {
            return this.mLibVLC.snapShot(track_id, fullpathname, width, height);
        }
        Log.e(TAG, "snapShot -- Initialize the VLC first");
        return -1;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (this.mVideoEventCallback != null) {
            this.mVideoEventCallback.onPlayerSurfaceCreated(holder);
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (this.mVideoEventCallback != null) {
            this.mVideoEventCallback.onPlayerSurfaceChanged(holder, format, width, height);
        }
        if (holder != this.mSurfaceView.getHolder()) {
            Log.w(TAG, "surfaceChanged -- the surface view has changed!");
            return;
        }
        Log.d(TAG, "Pixel format is " + format);
        Log.d(TAG, "width and height " + width + " " + height);
        this.isSurfaceReady = true;
        this.mLibVLC.attachSurface(holder.getSurface(), this, width, height);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (this.mVideoEventCallback != null) {
            this.mVideoEventCallback.onPlayerSurfaceDestroyed(holder);
        }
        if (holder != this.mSurfaceView.getHolder()) {
            Log.w(TAG, "surfaceDestroyed -- the surface view has changed!");
            return;
        }
        this.isSurfaceReady = false;
        this.mLibVLC.detachSurface();
    }

    public void setSurfaceSize(int width, int height, int sar_num, int sar_den) {
        Log.d(TAG, "setSurfaceSize: " + width + ", " + height + ", " + sar_num + ", " + sar_den);
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        this.eventHandler.sendMessage(this.eventHandler.obtainMessage(100));
    }
}
