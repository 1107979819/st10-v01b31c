package com.yuneec.flightmode15;

import android.content.Intent;
import android.media.SoundPool;
import android.os.Vibrator;
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import com.yuneec.widget.CounterView;
import com.yuneec.widget.CounterView.OnTickListener;

public class TimerHelper implements OnTickListener {
    public static final int F_MODE_INDEX = 0;
    public static final String KEY_DURATION = "duration";
    public static final String KEY_STYLE = "style";
    public static final String KEY_TITTLE = "title";
    public static final String KEY_TRIGGER = "trigger";
    public static final String KEY_TRIGGER_POPUP = "trigger_popup";
    public static final String KEY_WARNING = "warning";
    private static final String TAG = "TimerHelper";
    public static final int THROTTLE_25 = 0;
    public static final int THROTTLE_50 = 1;
    public static final int THROTTLE_75 = 2;
    public static final int THROTTLE_INDEX = 1;
    public static final int WARNING_BOTH = 2;
    public static final int WARNING_RING = 0;
    public static final int WARNING_VIBRATE = 1;
    private CounterView mCounter;
    private int mDuration;
    private boolean mIsTimerStarted;
    private SoundPool mSoundPool;
    private int mStyle;
    private int mThrottleTimerStartThreshold;
    private int mTimerTrigger = -1;
    private Vibrator mVibrator;
    private int mWarningSoundId;
    private int mWarningStreamId;
    private int mWarningType = 0;

    public TimerHelper(CounterView counter) {
        if (counter == null) {
            throw new IllegalArgumentException("you must assgin a CounterView to monitor the Timer");
        }
        this.mCounter = counter;
        this.mCounter.setTickListener(this);
        this.mSoundPool = new SoundPool(2, 3, 0);
        this.mVibrator = (Vibrator) this.mCounter.getContext().getSystemService("vibrator");
        this.mWarningSoundId = this.mSoundPool.load(this.mCounter.getContext(), R.raw.warning_beep_1, 1);
    }

    public void setupDefault() {
        this.mDuration = 36000;
        this.mWarningType = 0;
        this.mStyle = 1;
        this.mTimerTrigger = 0;
        this.mThrottleTimerStartThreshold = -1;
        this.mCounter.resetCounterColor();
        this.mCounter.setDuration(this.mDuration);
        this.mCounter.setStyle(this.mStyle);
    }

    public void setupUsingTimerSettingResult(Intent data) {
        this.mDuration = data.getIntExtra(KEY_DURATION, 0);
        this.mStyle = data.getIntExtra(KEY_STYLE, 0);
        this.mTimerTrigger = data.getIntExtra(KEY_TRIGGER, 0);
        this.mThrottleTimerStartThreshold = data.getIntExtra(KEY_TRIGGER_POPUP, -1);
        this.mWarningType = data.getIntExtra(KEY_WARNING, 0);
        this.mCounter.resetCounterColor();
        this.mCounter.setDuration(this.mDuration);
        this.mCounter.setStyle(this.mStyle);
    }

    public void fillIntentForTimerSetting(Intent intent) {
        intent.putExtra(KEY_STYLE, this.mStyle);
        intent.putExtra(KEY_DURATION, this.mDuration);
        intent.putExtra(KEY_WARNING, this.mWarningType);
        intent.putExtra(KEY_TRIGGER, this.mTimerTrigger);
        intent.putExtra(KEY_TRIGGER_POPUP, this.mThrottleTimerStartThreshold);
    }

    public void handleThrottleTrigger(Channel cmsg) {
        if (this.mDuration > 0 && !this.mIsTimerStarted) {
            int ch_num = cmsg.channels.size();
            if (this.mTimerTrigger == 1 && this.mThrottleTimerStartThreshold != -1 && ch_num >= 1 && ((int) ((((Float) cmsg.channels.get(0)).floatValue() / 4096.0f) * 100.0f)) > this.mThrottleTimerStartThreshold) {
                this.mCounter.start();
                this.mIsTimerStarted = true;
            }
        }
    }

    public void handleFmodeTrigger() {
        if (this.mTimerTrigger == 0 && !this.mIsTimerStarted) {
            this.mCounter.start();
            this.mIsTimerStarted = true;
        }
    }

    public void startFlight() {
        this.mCounter.reset();
        this.mCounter.setDuration(this.mDuration);
        this.mCounter.setStyle(this.mStyle);
    }

    public void endFlight() {
        this.mIsTimerStarted = false;
        this.mCounter.stop();
        this.mSoundPool.stop(this.mWarningStreamId);
        this.mVibrator.cancel();
    }

    public void releaseResource() {
        this.mSoundPool.release();
        this.mSoundPool = null;
    }

    public void onTick(int seconds) {
    }

    public void onAlmostEnd() {
        if (this.mWarningType == 0) {
            this.mWarningStreamId = this.mSoundPool.play(this.mWarningSoundId, 1.0f, 1.0f, 0, 2, Utilities.K_MAX);
        } else if (this.mWarningType == 1) {
            this.mVibrator.vibrate(new long[]{200, 800, 200, 800}, -1);
        } else if (this.mWarningType == 2) {
            this.mWarningStreamId = this.mSoundPool.play(this.mWarningSoundId, 1.0f, 1.0f, 0, 2, Utilities.K_MAX);
            this.mVibrator.vibrate(new long[]{200, 800, 200, 800}, -1);
        }
    }

    public void onEnd() {
        if (this.mWarningType == 0) {
            this.mWarningStreamId = this.mSoundPool.play(this.mWarningSoundId, 1.0f, 1.0f, 0, -1, 1.0f);
        } else if (this.mWarningType == 1) {
            this.mVibrator.vibrate(new long[]{1000, 2000, 1000}, 0);
        } else if (this.mWarningType == 2) {
            this.mWarningStreamId = this.mSoundPool.play(this.mWarningSoundId, 1.0f, 1.0f, 0, -1, 1.0f);
            this.mVibrator.vibrate(new long[]{1000, 2000, 1000}, 0);
        }
        startWarningCounter();
    }

    private void startWarningCounter() {
        this.mCounter.setStyle(1);
        this.mCounter.setDuration(36000);
        this.mCounter.start();
    }
}
