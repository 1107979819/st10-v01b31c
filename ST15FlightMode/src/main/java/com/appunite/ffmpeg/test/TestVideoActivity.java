package com.appunite.ffmpeg.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.NotPlayingException;
import com.appunite.ffmpeg.R;

public class TestVideoActivity extends Activity implements FFmpegListener, OnClickListener, OnCheckedChangeListener {
    private Button mIsRecording;
    private FFmpegPlayer mMpegPlayer;
    private Button mPlayBtn;
    private Button mPlaying;
    private ToggleButton mRecording;
    private Button mStopBtn;
    private View mVideoView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        getWindow().addFlags(128);
        this.mVideoView = findViewById(R.id.video_view);
        this.mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) this.mVideoView, this);
        this.mMpegPlayer.setMpegListener(this);
        this.mPlayBtn = (Button) findViewById(R.id.play);
        this.mStopBtn = (Button) findViewById(R.id.stop);
        this.mPlaying = (Button) findViewById(R.id.is_playing);
        this.mIsRecording = (Button) findViewById(R.id.is_recording);
        this.mRecording = (ToggleButton) findViewById(R.id.record);
        this.mPlayBtn.setOnClickListener(this);
        this.mStopBtn.setOnClickListener(this);
        this.mPlaying.setOnClickListener(this);
        this.mIsRecording.setOnClickListener(this);
        this.mRecording.setOnCheckedChangeListener(this);
    }

    public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams) {
        if (err != null) {
            Toast.makeText(this, "Load stream result" + err.toString(), 0).show();
        } else {
            Toast.makeText(this, "Load stream ok", 0).show();
        }
    }

    public void onFFResume(NotPlayingException result) {
        Toast.makeText(this, "onFFResume", 0).show();
    }

    public void onFFPause(NotPlayingException err) {
        Toast.makeText(this, "onFFPause", 0).show();
    }

    public void onFFStop() {
        Toast.makeText(this, "onFFStop", 0).show();
    }

    public void onFFUpdateTime(long mCurrentTimeUs, long mVideoDurationUs, boolean isFinished) {
        if (isFinished) {
            Toast.makeText(this, "Stream End Reached", 0).show();
        }
    }

    public void onFFSeeked(NotPlayingException result) {
    }

    public void onClick(View v) {
        if (v.equals(this.mPlayBtn)) {
            this.mMpegPlayer.setDataSource("file:///storage/sdcard0/test.mp4");
        } else if (v.equals(this.mStopBtn)) {
            this.mMpegPlayer.stop();
        } else if (v.equals(this.mPlaying)) {
            Toast.makeText(this, "isPlaying ?" + this.mMpegPlayer.isPlaying(), 0).show();
        } else if (v.equals(this.mIsRecording)) {
            Toast.makeText(this, "isRecording ?" + this.mMpegPlayer.isRecording(), 0).show();
        }
    }

    protected void onPause() {
        super.onPause();
        this.mMpegPlayer.stop();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!buttonView.equals(this.mRecording)) {
            return;
        }
        if (isChecked) {
            this.mMpegPlayer.startRecord(Environment.getExternalStorageDirectory() + "/test.mp4");
        } else {
            this.mMpegPlayer.stopRecord();
        }
    }
}
