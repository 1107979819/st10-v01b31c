package com.yuneec.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Clock extends TextView {
    private static final int AM_PM_STYLE = 1;
    private static final int AM_PM_STYLE_GONE = 2;
    private static final int AM_PM_STYLE_NORMAL = 0;
    private static final int AM_PM_STYLE_SMALL = 1;
    private static final String FORMAT_12 = "h:mm:ss a";
    private static final String FORMAT_24 = "HH:mm:ss";
    private static final String TAG = "Clock";
    private boolean mAttached;
    private Calendar mCalendar;
    private SimpleDateFormat mClockFormat;
    private String mClockFormatString;
    private Handler mHandler;
    private final BroadcastReceiver mIntentReceiver;
    private Runnable mTicker;

    public Clock(Context context) {
        this(context, null);
    }

    public Clock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Clock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
                    Clock.this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
                    if (Clock.this.mClockFormat != null) {
                        Clock.this.mClockFormat.setTimeZone(Clock.this.mCalendar.getTimeZone());
                    }
                }
                if (Clock.this.mHandler != null) {
                    Clock.this.mHandler.removeCallbacks(Clock.this.mTicker);
                }
                if (Clock.this.mTicker != null) {
                    Clock.this.mTicker.run();
                }
            }
        };
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            getContext().registerReceiver(this.mIntentReceiver, filter, null, getHandler());
        }
        this.mCalendar = Calendar.getInstance(TimeZone.getDefault());
        this.mHandler = new Handler();
        this.mTicker = new Runnable() {
            public void run() {
                if (Clock.this.mAttached) {
                    Clock.this.updateClock();
                    long now = SystemClock.uptimeMillis();
                    Clock.this.mHandler.postAtTime(Clock.this.mTicker, now + (1000 - (now % 1000)));
                }
            }
        };
        this.mTicker.run();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
        }
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mTicker);
        }
        this.mTicker = null;
        this.mHandler = null;
    }

    final void updateClock() {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        setText(getSmallTime());
    }

    private final CharSequence getSmallTime() {
        String format;
        SimpleDateFormat sdf;
        if (DateFormat.is24HourFormat(getContext())) {
            format = FORMAT_24;
        } else {
            format = FORMAT_12;
        }
        if (format.equals(this.mClockFormatString)) {
            sdf = this.mClockFormat;
        } else {
            int a = -1;
            boolean quoted = false;
            for (int i = 0; i < format.length(); i++) {
                char c = format.charAt(i);
                if (c == '\'') {
                    quoted = !quoted;
                }
                if (!quoted && c == 'a') {
                    a = i;
                    break;
                }
            }
            if (a >= 0) {
                int b = a;
                while (a > 0 && Character.isWhitespace(format.charAt(a - 1))) {
                    a--;
                }
                format = format.substring(0, a) + '' + format.substring(a, b) + "a" + '' + format.substring(b + 1);
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            this.mClockFormat = simpleDateFormat;
            this.mClockFormatString = format;
        }
        String result = sdf.format(this.mCalendar.getTime());
        int magic1 = result.indexOf(61184);
        int magic2 = result.indexOf(61185);
        if (magic1 < 0 || magic2 <= magic1) {
            return result;
        }
        SpannableStringBuilder formatted = new SpannableStringBuilder(result);
        formatted.setSpan(new RelativeSizeSpan(0.7f), magic1, magic2, 34);
        formatted.delete(magic2, magic2 + 1);
        formatted.delete(magic1, magic1 + 1);
        return formatted;
    }
}
