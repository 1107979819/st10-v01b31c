package com.yuneec.channelsettings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flight_settings.ChannelMap;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import com.yuneec.widget.MyToast;
import java.util.Arrays;

public class ChannelSettings extends FragmentActivity {
    public static final int CURVE_TYPE_BROKENLINE = 3;
    public static final int CURVE_TYPE_EXPO_1 = 1;
    public static final int CURVE_TYPE_EXPO_2 = 2;
    public static final int CURVE_TYPE_SPLINE = 4;
    public static final int STICK_RATE_0_OR_M100 = 683;
    public static final int STICK_RATE_100_OR_100 = 3412;
    public static final int STICK_RATE_125_OR_150 = 4095;
    public static final int STICK_RATE_50_OR_0 = 2048;
    public static final int STICK_RATE_M25_OR_M150 = 0;
    private static final String TAG = "FlightSettings";
    private ChannelMap[] mChMap;
    private UARTController mController;
    private DR_Fragment mDRFrgmt;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (ChannelSettings.this.mController == null) {
                Log.i("FlightSettings", "UARTController is null");
            } else if (msg.obj instanceof UARTInfoMessage) {
                UARTInfoMessage umsg = msg.obj;
                switch (umsg.what) {
                    case 2:
                        Channel cmsg = (Channel) umsg;
                        if (ChannelSettings.this.mThrFrgmt != null && ChannelSettings.this.mThrFrgmt.getUserVisibleHint()) {
                            int hw_index = ChannelSettings.getHardwareIndex(ChannelSettings.this, ChannelSettings.this.mChMap[0].hardware);
                            if (hw_index < 0 || hw_index >= cmsg.channels.size()) {
                                Log.e("FlightSettings", "Can't get hardware value");
                                return;
                            }
                            ChannelSettings.this.mThrFrgmt.setHardwarePos(((Float) cmsg.channels.get(hw_index)).intValue(), ChannelSettings.this.mChMap[0].function);
                            return;
                        } else if (ChannelSettings.this.mDRFrgmt != null && ChannelSettings.this.mDRFrgmt.getUserVisibleHint()) {
                            ChannelSettings.this.mDRFrgmt.setHardwarePos(cmsg, ChannelSettings.this.mChMap);
                            return;
                        } else {
                            return;
                        }
                    case 4:
                    case 5:
                    case 14:
                        return;
                    default:
                        return;
                }
            }
        }
    };
    private long mModel_id;
    private ServoSetupFragment mSrvFrgmt;
    private ThrottleCurveFragment mThrFrgmt;
    private ViewPager mViewPager;

    class FragmentAdapter extends FragmentStatePagerAdapter {
        public FragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Throttle Curve";
                case 1:
                    return "D/R";
                case 2:
                    return "Servo Setup";
                default:
                    return null;
            }
        }

        public Fragment getItem(int arg0) {
            switch (arg0) {
                case 0:
                    ChannelSettings.this.mThrFrgmt = (ThrottleCurveFragment) Fragment.instantiate(ChannelSettings.this, "com.yuneec.channelsettings.ThrottleCurveFragment");
                    return ChannelSettings.this.mThrFrgmt;
                case 1:
                    ChannelSettings.this.mDRFrgmt = (DR_Fragment) Fragment.instantiate(ChannelSettings.this, "com.yuneec.channelsettings.DR_Fragment");
                    return ChannelSettings.this.mDRFrgmt;
                case 2:
                    ChannelSettings.this.mSrvFrgmt = (ServoSetupFragment) Fragment.instantiate(ChannelSettings.this, "com.yuneec.channelsettings.ServoSetupFragment");
                    return ChannelSettings.this.mSrvFrgmt;
                default:
                    return null;
            }
        }

        public int getCount() {
            return 3;
        }
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getWindow().addFlags(Utilities.FLAG_HOMEKEY_DISPATCHED);
        getWindow().addFlags(128);
        setContentView(R.layout.channelsettings_main);
        this.mViewPager = (ViewPager) findViewById(R.id.viewpager);
        this.mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        this.mModel_id = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        this.mChMap = DataProviderHelper.readChannelMapFromDatabase(this, this.mModel_id);
    }

    protected void onResume() {
        super.onResume();
        this.mController = UARTController.getInstance();
        if (this.mController == null) {
            Log.e("FlightSettings", "UARTController is null");
            return;
        }
        this.mController.registerReaderHandler(this.mHandler);
        this.mController.startReading();
        if (!Utilities.isRunningMode() && !Utilities.ensureSimState(this.mController)) {
            MyToast.makeText((Context) this, getString(R.string.str_get_sim_state_failure), 0, 0);
        }
    }

    protected void onPause() {
        super.onPause();
        if (!(Utilities.isRunningMode() || Utilities.ensureAwaitState(this.mController))) {
            Log.e("FlightSettings", "fail to change to await");
        }
        Utilities.UartControllerStandBy(this.mController);
        this.mController = null;
    }

    public static int getHardwareIndex(Context context, String name) {
        String[] hwList = null;
        if (Utilities.PROJECT_TAG.equals("ST15")) {
            hwList = context.getResources().getStringArray(R.array.all_hardware_array_st15);
        } else if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
            hwList = context.getResources().getStringArray(R.array.all_hardware_array_st10);
        } else if (Utilities.PROJECT_TAG.equals("ST12")) {
            hwList = context.getResources().getStringArray(R.array.all_hardware_array_st12);
        }
        return Arrays.asList(hwList).indexOf(name);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 3 || event.isCanceled()) {
            return super.onKeyUp(keyCode, event);
        }
        Utilities.backToFlightScreen(this);
        return true;
    }

    public ThrottleCurveFragment getThrFrgmt() {
        return this.mThrFrgmt;
    }

    public DR_Fragment getDRFrgmt() {
        return this.mDRFrgmt;
    }

    public ServoSetupFragment getSrvFrgmt() {
        return this.mSrvFrgmt;
    }

    public UARTController getUARTController() {
        return this.mController;
    }
}
