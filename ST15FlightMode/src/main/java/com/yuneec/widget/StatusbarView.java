package com.yuneec.widget;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;
import com.yuneec.database.DBOpenHelper;
import com.yuneec.database.DataProvider;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flightmode15.R;

public class StatusbarView extends RelativeLayout {
    private static final boolean DEBUG = false;
    private static final String TAG = "StatusbarView";
    private ImageView mBatteryImage;
    private BroadcastReceiver mBatteryReceiveer;
    private TextView mBatteryText;
    private ViewAnimator mCenterText;
    private TextView mCenterText_0;
    private TextView mCenterText_1;
    private TextView mGpsIndictor;
    private Listener mGpsStatusListener;
    private Handler mHandler;
    private boolean mIsCenterText1;
    private LocationManager mLM;
    private TextView mLeftText0;
    private TextView mLeftText1;
    private String mOwner;
    private ImageView mWifiIndictor;
    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private Runnable showOwnerRunnable;

    public StatusbarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mOwner = "Pilot";
        this.mIsCenterText1 = false;
        this.mHandler = new Handler();
        this.showOwnerRunnable = new Runnable() {
            public void run() {
                StatusbarView.this.setInfoTextInternal(StatusbarView.this.getResources().getString(R.string.greeting, new Object[]{StatusbarView.this.mOwner}), -1, false);
            }
        };
        this.mWifiReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.net.wifi.RSSI_CHANGED")) {
                    StatusbarView.this.mWifiIndictor.setImageLevel(WifiManager.calculateSignalLevel(intent.getIntExtra("newRssi", -200), 100));
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    if (intent.getIntExtra("wifi_state", 4) != 3) {
                        StatusbarView.this.mWifiIndictor.setVisibility(8);
                        StatusbarView.this.mWifiIndictor.setImageLevel(0);
                    }
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    DetailedState ds = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState();
                    if (DetailedState.CONNECTED == ds) {
                        StatusbarView.this.mWifiIndictor.setVisibility(0);
                        StatusbarView.this.mWifiIndictor.setImageLevel(WifiManager.calculateSignalLevel(StatusbarView.this.mWifiManager.getConnectionInfo().getRssi(), 100));
                    } else if (DetailedState.DISCONNECTING == ds || DetailedState.DISCONNECTED == ds || DetailedState.SCANNING == ds) {
                        StatusbarView.this.mWifiIndictor.setImageLevel(0);
                        StatusbarView.this.mWifiIndictor.setVisibility(8);
                    }
                }
            }
        };
        this.mBatteryReceiveer = new BroadcastReceiver() {
            int last_level;
            int plugged = -1;

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                    int plugged = intent.getIntExtra("plugged", -1);
                    int level = intent.getIntExtra("level", -1);
                    if (level != -1) {
                        if (this.plugged != plugged) {
                            this.plugged = plugged;
                            if (plugged != 0) {
                                StatusbarView.this.mBatteryImage.setImageResource(R.drawable.stat_battery_charging);
                            } else {
                                StatusbarView.this.mBatteryImage.setImageResource(R.drawable.stat_battery);
                            }
                        }
                        if (plugged == 0) {
                            StatusbarView.this.mBatteryImage.setImageLevel(level);
                        }
                        StatusbarView.this.mBatteryText.setText(String.valueOf(level) + "%");
                        this.last_level = level;
                    }
                } else if (intent.getAction().equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                    StatusbarView.this.mBatteryImage.setImageResource(R.drawable.stat_battery);
                    StatusbarView.this.mBatteryImage.setImageLevel(this.last_level);
                    StatusbarView.this.mBatteryText.setText(new StringBuilder(String.valueOf(String.valueOf(this.last_level))).append("%").toString());
                } else if (intent.getAction().equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                    StatusbarView.this.mBatteryImage.setImageResource(R.drawable.stat_battery_charging);
                }
            }
        };
        this.mGpsStatusListener = new Listener() {
            public void onGpsStatusChanged(int event) {
                GpsStatus gs = StatusbarView.this.mLM.getGpsStatus(null);
                int gps_num = -1;
                if (gs != null && event == 4) {
                    gps_num = 0;
                    for (GpsSatellite satellite : gs.getSatellites()) {
                        if (satellite.usedInFix()) {
                            gps_num++;
                        }
                    }
                }
                if (gps_num >= 0) {
                    StatusbarView.this.mGpsIndictor.setVisibility(0);
                    StatusbarView.this.mGpsIndictor.setText(String.valueOf(gps_num));
                    StatusbarView.this.mGpsIndictor.getCompoundDrawables()[2].setLevel(gps_num);
                    return;
                }
                StatusbarView.this.mGpsIndictor.setVisibility(8);
            }
        };
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.statusbar, this, true);
        setPadding(4, 0, 4, 0);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mLM = (LocationManager) context.getSystemService("location");
    }

    public StatusbarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusbarView(Context context) {
        this(context, null);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLeftText0 = (TextView) findViewById(R.id.leftText0);
        this.mLeftText1 = (TextView) findViewById(R.id.leftText1);
        this.mBatteryImage = (ImageView) findViewById(R.id.stat_battery);
        this.mBatteryText = (TextView) findViewById(R.id.stat_battery_text);
        this.mCenterText = (ViewAnimator) findViewById(R.id.centerText);
        this.mCenterText_0 = (TextView) findViewById(R.id.centerText_greeting);
        this.mCenterText_1 = (TextView) findViewById(R.id.centerText_message);
        this.mWifiIndictor = (ImageView) findViewById(R.id.stat_wifi);
        this.mGpsIndictor = (TextView) findViewById(R.id.stat_gps);
        this.mCenterText_0.setText(getResources().getString(R.string.greeting, new Object[]{this.mOwner}));
        this.mCenterText.setInAnimation(getContext(), R.anim.push_up_in);
        this.mCenterText.setOutAnimation(getContext(), R.anim.push_up_out);
        initLeftText(getContext());
        initStatIcon();
    }

    private void initLeftText(Context context) {
        String model_name;
        long model_id = context.getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        if (model_id == -2) {
            model_name = context.getString(R.string.no_model);
        } else {
            Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id), new String[]{DBOpenHelper.KEY_NAME}, null, null, null);
            if (DataProviderHelper.isCursorValid(cursor)) {
                String str = cursor.getString(cursor.getColumnIndex(DBOpenHelper.KEY_NAME));
                cursor.close();
                if (str != null) {
                    model_name = context.getString(R.string.model_name) + str;
                } else {
                    model_name = context.getString(R.string.no_model);
                }
            } else {
                Log.e(TAG, "Get date from MODEL_URI, Cursor is invalid");
                return;
            }
        }
        setLeftText(0, model_name);
    }

    private void initStatIcon() {
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter bat_filter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        bat_filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        bat_filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        getContext().registerReceiver(this.mBatteryReceiveer, bat_filter);
        IntentFilter wifi_filter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        wifi_filter.addAction("android.net.wifi.RSSI_CHANGED");
        wifi_filter.addAction("android.net.wifi.STATE_CHANGE");
        getContext().registerReceiver(this.mWifiReceiver, wifi_filter);
        this.mLM.addGpsStatusListener(this.mGpsStatusListener);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(this.mBatteryReceiveer);
        getContext().unregisterReceiver(this.mWifiReceiver);
        this.mHandler.removeCallbacks(this.showOwnerRunnable);
        TextView tv = this.mIsCenterText1 ? this.mCenterText_1 : this.mCenterText_0;
        tv.setText(getResources().getString(R.string.greeting, new Object[]{this.mOwner}));
        tv.setTextColor(-1);
        this.mLM.removeGpsStatusListener(this.mGpsStatusListener);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, MeasureSpec.getMode(widthMeasureSpec)), MeasureSpec.makeMeasureSpec(getResources().getDimensionPixelSize(R.dimen.statusbar_height), MeasureSpec.getMode(heightMeasureSpec)));
    }

    public void setLeftText(int index, String text) {
        setLeftTextVisibility(index, true);
        switch (index) {
            case 0:
                this.mLeftText0.setVisibility(0);
                this.mLeftText0.setText(text);
                return;
            case 1:
                this.mLeftText1.setVisibility(0);
                this.mLeftText1.setText(text);
                return;
            default:
                return;
        }
    }

    public void setLeftTextVisibility(int index, boolean visibility) {
        switch (index) {
            case 0:
                if (visibility) {
                    this.mLeftText0.setVisibility(0);
                    return;
                } else {
                    this.mLeftText0.setVisibility(8);
                    return;
                }
            case 1:
                if (visibility) {
                    this.mLeftText1.setVisibility(0);
                    return;
                } else {
                    this.mLeftText1.setVisibility(8);
                    return;
                }
            default:
                return;
        }
    }

    public void setInfoText(String text, int color) {
        setInfoTextInternal(text, color, true);
    }

    private void setInfoTextInternal(String text, int color, boolean showOwner) {
        TextView tv;
        int index;
        this.mHandler.removeCallbacks(this.showOwnerRunnable);
        if (this.mIsCenterText1) {
            tv = this.mCenterText_0;
            index = 0;
        } else {
            tv = this.mCenterText_1;
            index = 1;
        }
        this.mIsCenterText1 = !this.mIsCenterText1;
        tv.setText(text);
        if (color != 0) {
            tv.setTextColor(color);
        } else {
            tv.setTextColor(-1);
        }
        this.mCenterText.setDisplayedChild(index);
        if (showOwner) {
            this.mHandler.postDelayed(this.showOwnerRunnable, 10000);
        }
    }

    public void setmOwner(String owner) {
        this.mOwner = owner;
    }
}
