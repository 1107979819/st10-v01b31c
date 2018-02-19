package com.appunite.ffmpeg;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class FFmpegSurfaceView extends SurfaceView implements FFmpegDisplay, Callback {
    private boolean mCreated;
    private FFmpegPlayer mMpegPlayer;

    public enum ScaleType {
        CENTER_CROP,
        CENTER_INSIDE,
        FIT_XY
    }

    public FFmpegSurfaceView(Context context) {
        this(context, null, 0);
    }

    public FFmpegSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FFmpegSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMpegPlayer = null;
        this.mCreated = false;
        SurfaceHolder holder = getHolder();
        holder.setFormat(1);
        holder.addCallback(this);
    }

    public void setMpegPlayer(FFmpegPlayer fFmpegPlayer) {
        if (this.mMpegPlayer != null) {
            throw new RuntimeException("setMpegPlayer could not be called twice");
        }
        this.mMpegPlayer = fFmpegPlayer;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (this.mCreated) {
            surfaceDestroyed(holder);
        }
        this.mMpegPlayer.render(holder.getSurface());
        this.mCreated = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.mMpegPlayer.renderFrameStop();
        this.mCreated = false;
    }
}
