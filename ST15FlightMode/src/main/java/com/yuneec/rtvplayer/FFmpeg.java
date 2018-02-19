package com.yuneec.rtvplayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.NotPlayingException;

public class FFmpeg extends RTVPlayer implements Callback, FFmpegListener {
    private static final String RECORD_FILE_EXTENSION = ".avc";
    private static final String TAG = "FFmpeg";
    private FFmpegPlayer mMpegPlayer;
    private boolean mSurfaceCreated = false;
    private String mVideoLocation;

    public void init(Context application_context, int image_format, boolean hardware_accelerate) {
        if (application_context instanceof Activity) {
            this.mMpegPlayer = new FFmpegPlayer(null, (Activity) application_context);
            this.mMpegPlayer.setMpegListener(this);
            return;
        }
        throw new IllegalArgumentException("You should pass a Activity instead of other context");
    }

    public void deinit() {
    }

    public void setSurfaceView(SurfaceView surface_view) {
        if (this.mMpegPlayer == null) {
            Log.e(TAG, "You should call init method first");
        } else if (surface_view == null) {
            Log.e(TAG, "no surface view was assgined");
        } else {
            SurfaceHolder holder = surface_view.getHolder();
            holder.setFormat(1);
            holder.addCallback(this);
        }
    }

    public void setVideoLocation(String url) {
        this.mVideoLocation = url;
    }

    public boolean play() {
        if (this.mMpegPlayer == null) {
            Log.e(TAG, "play -- Initialize the ffmpeg first");
            return false;
        } else if (this.mMpegPlayer.isPlaying()) {
            Log.i(TAG, "Player has already been played");
            return false;
        } else if (this.mVideoLocation == null) {
            Log.e(TAG, "tell me the video location first");
            return false;
        } else {
            this.mMpegPlayer.setDataSource(this.mVideoLocation);
            return true;
        }
    }

    public boolean play(String location) {
        this.mVideoLocation = location;
        return play();
    }

    public void stop() {
        if (this.mMpegPlayer == null) {
            Log.e(TAG, "stop -- Initialize the ffmpeg first");
        } else if (this.mMpegPlayer.isPlaying()) {
            this.mMpegPlayer.stop();
        } else {
            Log.i(TAG, "Player has already been stopped");
        }
    }

    public boolean isPlaying() {
        if (this.mMpegPlayer != null) {
            return this.mMpegPlayer.isPlaying();
        }
        Log.e(TAG, "isPlaying -- Initialize the ffmpeg first");
        return false;
    }

    public boolean canRecord() {
        if (this.mMpegPlayer != null) {
            return isPlaying();
        }
        Log.e(TAG, "canRecord -- Initialize the ffmpeg first");
        return false;
    }

    public boolean isRecording() {
        if (this.mMpegPlayer != null) {
            return this.mMpegPlayer.isRecording();
        }
        Log.e(TAG, "isRecording -- Initialize the ffmpeg first");
        return false;
    }

    public int startRecord(String fullpathname) {
        if (this.mMpegPlayer == null) {
            Log.e(TAG, "startRecord -- Initialize the ffmpeg first");
            return -1;
        } else if (fullpathname != null) {
            return this.mMpegPlayer.startRecord(new StringBuilder(String.valueOf(fullpathname)).append(RECORD_FILE_EXTENSION).toString());
        } else {
            Log.e(TAG, "startRecord -- fullpathname null!");
            return -1;
        }
    }

    public int stopRecord() {
        if (this.mMpegPlayer != null) {
            return this.mMpegPlayer.stopRecord();
        }
        Log.e(TAG, "startRecord -- Initialize the ffmpeg first");
        return -1;
    }

    public int snapShot(int track_id, String fullpathname, int width, int height) {
        return -1;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (this.mVideoEventCallback != null) {
            this.mVideoEventCallback.onPlayerSurfaceCreated(holder);
        }
        if (this.mSurfaceCreated) {
            surfaceDestroyed(holder);
        }
        this.mMpegPlayer.render(holder.getSurface());
        this.mSurfaceCreated = true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (this.mVideoEventCallback != null) {
            this.mVideoEventCallback.onPlayerSurfaceChanged(holder, format, width, height);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (this.mVideoEventCallback != null) {
            this.mVideoEventCallback.onPlayerSurfaceDestroyed(holder);
        }
        this.mMpegPlayer.renderFrameStop();
        this.mSurfaceCreated = false;
    }

    public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams) {
        if (err != null) {
            Log.i(TAG, "player encounter error:" + err.toString());
            if (this.mVideoEventCallback != null) {
                this.mVideoEventCallback.onPlayerEncoutneredError();
                return;
            }
            return;
        }
        Log.i(TAG, "player load source OK:");
        if (this.mVideoEventCallback != null) {
            this.mVideoEventCallback.onPlayerPlaying();
        }
    }

    public void onFFResume(NotPlayingException result) {
    }

    public void onFFPause(NotPlayingException err) {
    }

    public void onFFStop() {
        if (this.mVideoEventCallback != null) {
            Log.i(TAG, "Stream Stopped");
            this.mVideoEventCallback.onPlayerStopped();
        }
    }

    public void onFFUpdateTime(long mCurrentTimeUs, long mVideoDurationUs, boolean isFinished) {
        if (isFinished) {
            Log.i(TAG, "Stream End Reached");
            if (this.mVideoEventCallback != null) {
                this.mVideoEventCallback.onPlayerEndReached();
            }
        }
    }

    public void onFFSeeked(NotPlayingException result) {
    }
}
