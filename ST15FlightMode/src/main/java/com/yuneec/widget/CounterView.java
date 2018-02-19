package com.yuneec.widget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import java.util.Timer;
import java.util.TimerTask;

public class CounterView extends TextView {
    public static final int COUNT_DOWN = 2;
    public static final int COUNT_UP = 1;
    private static final String TAG = "CounterView";
    private int mAlmostEndThreshold = 60000;
    private long mBase;
    private long mDuration;
    private String mFormatString;
    private boolean mIsAlmostEndPosted = false;
    private int mNormalColor;
    private OnTickListener mOnTickListener;
    private boolean mRunning = false;
    private int mStartTime = 0;
    private int mStyle = 1;
    private int mTickEndColor = -65536;
    private int mTrigger = 0;
    private Timer mUpdateTimer;
    private UpdateTimerTask mUpdateTimerTask;

    public interface OnTickListener {
        void onAlmostEnd();

        void onEnd();

        void onTick(int i);
    }

    private class UpdateTimerTask extends TimerTask {
        private UpdateTimerTask() {
        }

        public void run() {
            CounterView.this.post(new Runnable() {
                public void run() {
                    if (CounterView.this.mRunning) {
                        long milliseconds;
                        if (CounterView.this.mStyle == 2) {
                            milliseconds = CounterView.this.mBase - SystemClock.elapsedRealtime();
                        } else {
                            milliseconds = (SystemClock.elapsedRealtime() - CounterView.this.mBase) + ((long) CounterView.this.mStartTime);
                        }
                        long end_milliseconds = CounterView.this.mDuration;
                        if (!CounterView.this.mIsAlmostEndPosted && milliseconds <= ((long) CounterView.this.mAlmostEndThreshold) && CounterView.this.mStyle == 2) {
                            CounterView.this.mIsAlmostEndPosted = true;
                            if (CounterView.this.mOnTickListener != null) {
                                CounterView.this.mOnTickListener.onAlmostEnd();
                            }
                        }
                        if (!CounterView.this.mIsAlmostEndPosted && CounterView.this.mStyle == 1 && end_milliseconds - milliseconds <= ((long) CounterView.this.mAlmostEndThreshold)) {
                            CounterView.this.mIsAlmostEndPosted = true;
                            if (CounterView.this.mOnTickListener != null) {
                                CounterView.this.mOnTickListener.onAlmostEnd();
                            }
                        }
                        if (milliseconds <= 0 && CounterView.this.mStyle == 2) {
                            milliseconds = 0;
                            CounterView.this.stop();
                            if (CounterView.this.mOnTickListener != null) {
                                CounterView.this.mOnTickListener.onEnd();
                            }
                        }
                        if (milliseconds >= end_milliseconds && CounterView.this.mStyle == 1) {
                            milliseconds = end_milliseconds;
                            CounterView.this.stop();
                            if (CounterView.this.mOnTickListener != null) {
                                CounterView.this.mOnTickListener.onEnd();
                            }
                        }
                        CounterView.this.setText(CounterView.this.setFormatTime(milliseconds));
                        if (CounterView.this.mOnTickListener != null) {
                            CounterView.this.mOnTickListener.onTick((int) (milliseconds / 1000));
                        }
                    }
                }
            });
        }
    }

    public CounterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CounterView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.mFormatString = context.getString(R.string.counter_format_time);
        this.mNormalColor = getTextColors().getDefaultColor();
    }

    public boolean isStarted() {
        return this.mRunning;
    }

    public void start() {
        if (this.mDuration == 0) {
            Log.i(TAG, "duration is 0, no need to start");
        } else if (this.mRunning) {
            Log.e(TAG, "Counter is already running");
        } else {
            this.mRunning = true;
            if (this.mDuration <= ((long) this.mAlmostEndThreshold)) {
                this.mIsAlmostEndPosted = true;
            }
            if (this.mStyle == 2) {
                this.mBase = SystemClock.elapsedRealtime() + this.mDuration;
            } else {
                this.mBase = SystemClock.elapsedRealtime();
            }
            this.mUpdateTimer = new Timer("Counter");
            this.mUpdateTimerTask = new UpdateTimerTask();
            this.mUpdateTimer.scheduleAtFixedRate(this.mUpdateTimerTask, 0, 1000);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setText(setFormatTime(0));
    }

    public void stop() {
        this.mRunning = false;
        this.mIsAlmostEndPosted = false;
        if (this.mUpdateTimer != null) {
            this.mUpdateTimer.cancel();
            if (this.mUpdateTimerTask != null) {
                this.mUpdateTimerTask.cancel();
            }
            this.mUpdateTimer = null;
        }
        this.mStartTime = 0;
        setText(setFormatTime(0));
    }

    public void setStartTime(int startTime) {
        this.mStartTime = startTime;
    }

    public void setStyle(int style) {
        if (this.mRunning) {
            Log.e(TAG, "Counter already running cannot change style");
            return;
        }
        this.mStyle = style;
        if (this.mStyle == 2) {
            setText(setFormatTime(this.mDuration));
        } else {
            setText(setFormatTime(0));
        }
    }

    public int getTrigger() {
        return this.mTrigger;
    }

    public void setTrigger(int trigger) {
        this.mTrigger = trigger;
    }

    public int getStyle() {
        return this.mStyle;
    }

    public int getDuration() {
        return (int) (this.mDuration / 1000);
    }

    public void setDuration(int seconds) {
        if (this.mRunning) {
            Log.e(TAG, "Counter already running cannot change duration");
            return;
        }
        this.mDuration = (long) (seconds * 1000);
        if (this.mStyle == 2) {
            setText(setFormatTime(this.mDuration));
        } else {
            setText(setFormatTime(0));
        }
    }

    public void setTickListener(OnTickListener listener) {
        this.mOnTickListener = listener;
    }

    public void reset() {
        setTextColor(this.mNormalColor);
        stop();
        setDuration(getDuration());
    }

    public void resetCounterColor() {
        setTextColor(this.mNormalColor);
    }

    private void onTickEndInternal() {
        setTextColor(this.mTickEndColor);
    }

    private String setFormatTime(long ms) {
        int elapsedSeconds = (int) (ms / 1000);
        int minutes = 0;
        int hours = 0;
        if (elapsedSeconds >= 3600) {
            hours = elapsedSeconds / 3600;
            elapsedSeconds -= hours * 3600;
        }
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
        }
        int seconds = elapsedSeconds;
        return String.format(this.mFormatString, new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
    }
}
