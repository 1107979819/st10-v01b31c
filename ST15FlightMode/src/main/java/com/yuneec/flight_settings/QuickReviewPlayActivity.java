package com.yuneec.flight_settings;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.rtvplayer.RTVPlayer;
import com.yuneec.rtvplayer.RTVPlayer.VideoEventCallback;
import com.yuneec.widget.MyToast;

public class QuickReviewPlayActivity extends Activity {
    private static final String TAG = "QuickReviewActivity";
    private String mFullPathName;
    private Handler mHandler = new Handler();
    private RTVPlayer mPlayer;
    private String mShortName;
    private boolean mSurfaceCreated = false;
    private VideoEventCallback mVideoEventCallback = new VideoEventCallback() {
        public void onPlayerSurfaceDestroyed(SurfaceHolder holder) {
            QuickReviewPlayActivity.this.mSurfaceCreated = false;
        }

        public void onPlayerSurfaceCreated(SurfaceHolder holder) {
            QuickReviewPlayActivity.this.mSurfaceCreated = true;
        }

        public void onPlayerSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void onPlayerStopped() {
            MyToast.makeText(QuickReviewPlayActivity.this, (int) R.string.str_quick_review_review_stop, 0, 1).show();
        }

        public void onPlayerSnapshotTaken() {
        }

        public void onPlayerRecordableChanged() {
        }

        public void onPlayerPlaying() {
        }

        public void onPlayerPlayerRecordingFinished() {
        }

        public void onPlayerEndReached() {
        }

        public void onPlayerEncoutneredError() {
            MyToast.makeText(QuickReviewPlayActivity.this, (int) R.string.str_quick_review_play_fail, 0, 1).show();
        }
    };
    private Runnable playReviewRunnable = new Runnable() {
        public void run() {
            if (QuickReviewPlayActivity.this.mSurfaceCreated) {
                QuickReviewPlayActivity.this.mPlayer.play(QuickReviewPlayActivity.this.mFullPathName);
            } else {
                QuickReviewPlayActivity.this.mHandler.postDelayed(QuickReviewPlayActivity.this.playReviewRunnable, 500);
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.quick_review_play);
        this.mShortName = getIntent().getStringExtra("short_file_name");
        if (this.mShortName == null) {
            Log.w(TAG, "No File specified, activity about to finish");
            finish();
            return;
        }
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        this.mPlayer = RTVPlayer.getPlayer(2);
        this.mPlayer.init(this, 1, false);
        this.mPlayer.setSurfaceView(surfaceView);
        this.mPlayer.setVideoEventCallback(this.mVideoEventCallback);
        if (this.mShortName != null) {
            this.mFullPathName = "file:///sdcard/FPV-Video/Local/" + this.mShortName;
            this.mHandler.postDelayed(this.playReviewRunnable, 500);
        }
    }

    protected void onPause() {
        super.onPause();
        this.mPlayer.stop();
        this.mHandler.removeCallbacks(this.playReviewRunnable);
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mPlayer.deinit();
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 3 || event.isCanceled()) {
            return super.onKeyUp(keyCode, event);
        }
        Utilities.backToFlightScreen(this);
        return true;
    }
}
