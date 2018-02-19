package com.yuneec.channelsettings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import com.yuneec.channelsettings.ThrottleData.ThrCurve;
import com.yuneec.curve.BrokenLineView;
import com.yuneec.curve.CurveView.OnCurveTouchListener;
import com.yuneec.curve.CurveView.onAllPointsUpdateListener;
import com.yuneec.curve.SplineView;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flight_settings.ChannelMap;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.MixedData;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.widget.ButtonPicker;
import com.yuneec.widget.ButtonPicker.OnPickerListener;
import com.yuneec.widget.ButtonSeekBar;
import com.yuneec.widget.ButtonSeekBar.onButtonSeekChangeListener;
import com.yuneec.widget.MyProgressDialog;
import com.yuneec.widget.MyToast;
import com.yuneec.widget.OneButtonPopDialog;
import com.yuneec.widget.TwoButtonPopDialog;
import java.util.ArrayList;
import java.util.Arrays;

public class ThrottleCurveFragment extends Fragment implements onButtonSeekChangeListener {
    private static final float[] CURVE_X = new float[]{-100.0f, -50.0f, 0.0f, 50.0f, 100.0f};
    private static final String TAG = "ThrottleCurveFragment";
    private static final float[] THR_DATA_DEFAULT_CURVE_POINTS = new float[]{0.0f, 25.0f, 50.0f, 75.0f, 100.0f};
    private static final boolean THR_DATA_DEFAULT_EXPO = false;
    private static final String THR_DATA_DEFAULT_SW = "INH";
    private static final String THR_DATA_DEFAULT_THR_CUT_SW = "INH";
    private static final int THR_DATA_DEFAULT_THR_CUT_VALUE1 = 0;
    private static final int THR_DATA_DEFAULT_THR_CUT_VALUE2 = 50;
    private static MyProgressDialog mProgressDialog;
    private BrokenLineView mBlv;
    private int mCurrentSWstate = 0;
    private Switch mExpo_sw;
    private long mModel_id;
    private ButtonSeekBar mSeekPos1;
    private ButtonSeekBar mSeekPos2;
    private ButtonSeekBar mSeekPos3;
    private ButtonSeekBar mSeekPos4;
    private ButtonSeekBar mSeekPos5;
    private ServoData mServoData;
    private SplineView mSpv;
    private int mStickLastPosValue = -100;
    private ButtonPicker mSwitch_bp;
    private ButtonPicker mThrCut_bp;
    private ThrottleData mThrData = new ThrottleData();

    private class SendDataTask extends AsyncTask<MixedData, Void, Boolean> {
        private SendDataTask() {
        }

        protected Boolean doInBackground(MixedData... params) {
            boolean result = true;
            UARTController controller = ((ChannelSettings) ThrottleCurveFragment.this.getActivity()).getUARTController();
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    if (!controller.syncMixingData(true, params[i], 1)) {
                        Log.e(ThrottleCurveFragment.TAG, "Failed to send Throttle data at " + i);
                        result = false;
                    }
                }
            }
            return Boolean.valueOf(result);
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            ThrottleCurveFragment.mProgressDialog.dismiss();
            if (result.booleanValue()) {
                MyToast.makeText(ThrottleCurveFragment.this.getActivity(), ThrottleCurveFragment.this.getString(R.string.str_save_successed), 0, 1).show();
            } else {
                ThrottleCurveFragment.this.showFailedToSendDialog();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initThrottleData();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cs_fragment_throttle_curve, null);
        initiation(v);
        return v;
    }

    public void onResume() {
        super.onResume();
        refreshScreen();
    }

    public void onPause() {
        super.onPause();
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            this.mServoData = ServoSetupFragment.getServoSetup(DataProviderHelper.readServoDataFromDatabase(getActivity(), this.mModel_id, 0), "Thr");
        } else {
            this.mStickLastPosValue = -100;
        }
    }

    private int getSwitchState() {
        this.mThrData.sw.equals("INH");
        return 0;
    }

    private void refreshScreen() {
        setCurve(this.mThrData.expo);
        int state = getSwitchState();
        this.mSeekPos1.setProgress((int) (this.mThrData.thrCurve[state].curvePoints[0] * 10.0f));
        this.mSeekPos2.setProgress((int) (this.mThrData.thrCurve[state].curvePoints[1] * 10.0f));
        this.mSeekPos3.setProgress((int) (this.mThrData.thrCurve[state].curvePoints[2] * 10.0f));
        this.mSeekPos4.setProgress((int) (this.mThrData.thrCurve[state].curvePoints[3] * 10.0f));
        this.mSeekPos5.setProgress((int) (this.mThrData.thrCurve[state].curvePoints[4] * 10.0f));
        this.mExpo_sw.setChecked(this.mThrData.expo);
        this.mSwitch_bp.setStringValue(this.mThrData.sw);
        this.mThrCut_bp.setStringValue(this.mThrData.cut_sw);
    }

    private void initiation(View v) {
        this.mBlv = (BrokenLineView) v.findViewById(R.id.brokenlineView);
        this.mBlv.setOnCurveTouchListener(new OnCurveTouchListener() {
            public void onCurveValueChanged(int pointIndex) {
                ButtonSeekBar bsb = null;
                ThrottleCurveFragment.this.mThrData.thrCurve[ThrottleCurveFragment.this.mCurrentSWstate].curvePoints[pointIndex] = ThrottleCurveFragment.this.mBlv.getPointValue(pointIndex);
                switch (pointIndex) {
                    case 0:
                        bsb = ThrottleCurveFragment.this.mSeekPos1;
                        break;
                    case 1:
                        bsb = ThrottleCurveFragment.this.mSeekPos2;
                        break;
                    case 2:
                        bsb = ThrottleCurveFragment.this.mSeekPos3;
                        break;
                    case 3:
                        bsb = ThrottleCurveFragment.this.mSeekPos4;
                        break;
                    case 4:
                        bsb = ThrottleCurveFragment.this.mSeekPos5;
                        break;
                    default:
                        Log.e(ThrottleCurveFragment.TAG, "Invalid point index");
                        break;
                }
                if (bsb != null) {
                    bsb.setProgress((int) (ThrottleCurveFragment.this.mThrData.thrCurve[ThrottleCurveFragment.this.mCurrentSWstate].curvePoints[pointIndex] * 10.0f));
                }
            }

            public void onCurveValueChanged(int coefficient, float value) {
            }
        });
        this.mSpv = (SplineView) v.findViewById(R.id.splineView);
        this.mSpv.setOnCurveTouchListener(new OnCurveTouchListener() {
            public void onCurveValueChanged(int pointIndex) {
                ThrottleCurveFragment.this.mThrData.thrCurve[ThrottleCurveFragment.this.mCurrentSWstate].curvePoints[pointIndex] = ThrottleCurveFragment.this.mSpv.getPointValue(pointIndex);
            }

            public void onCurveValueChanged(int coefficient, float value) {
            }
        });
        this.mSpv.setOnAllPointsUpdateListener(new onAllPointsUpdateListener() {
            public void onUpdated(int[] allPoints) {
            }
        });
        int max = getResources().getInteger(R.integer.def_channelsetting_thr_curve_y_Max);
        int min = getResources().getInteger(R.integer.def_channelsetting_thr_curve_y_Min);
        this.mBlv.setOutputValueMax(max);
        this.mBlv.setOutputValueMin(min);
        this.mSpv.setOutputValueMax(max);
        this.mSpv.setOutputValueMin(min);
        this.mSeekPos1 = (ButtonSeekBar) v.findViewById(R.id.btnseek1);
        this.mSeekPos1.setOnButtonSeekChangeListener(this);
        this.mSeekPos1.initiation(getString(R.string.str_pos_1), max * 10, min * 10);
        this.mSeekPos2 = (ButtonSeekBar) v.findViewById(R.id.btnseek2);
        this.mSeekPos2.setOnButtonSeekChangeListener(this);
        this.mSeekPos2.initiation(getString(R.string.str_pos_2), max * 10, min * 10);
        this.mSeekPos3 = (ButtonSeekBar) v.findViewById(R.id.btnseek3);
        this.mSeekPos3.setOnButtonSeekChangeListener(this);
        this.mSeekPos3.initiation(getString(R.string.str_pos_3), max * 10, min * 10);
        this.mSeekPos4 = (ButtonSeekBar) v.findViewById(R.id.btnseek4);
        this.mSeekPos4.setOnButtonSeekChangeListener(this);
        this.mSeekPos4.initiation(getString(R.string.str_pos_4), max * 10, min * 10);
        this.mSeekPos5 = (ButtonSeekBar) v.findViewById(R.id.btnseek5);
        this.mSeekPos5.setOnButtonSeekChangeListener(this);
        this.mSeekPos5.initiation(getString(R.string.str_pos_5), max * 10, min * 10);
        this.mExpo_sw = (Switch) v.findViewById(R.id.expo_switch);
        this.mExpo_sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ThrottleCurveFragment.this.mThrData.expo = isChecked;
                ThrottleCurveFragment.this.setCurve(ThrottleCurveFragment.this.mThrData.expo);
            }
        });
        this.mSwitch_bp = (ButtonPicker) v.findViewById(R.id.dr_sw_picker);
        this.mSwitch_bp.initiation(new String[]{"INH", "S2", "S3", "S4"}, "INH");
        this.mSwitch_bp.setOnPickerListener(new OnPickerListener() {
            public void onClicked(ButtonPicker picker, String value) {
                ThrottleCurveFragment.this.mThrData.sw = value;
                Log.d(ThrottleCurveFragment.TAG, "Switch = " + value);
            }
        });
        this.mThrCut_bp = (ButtonPicker) v.findViewById(R.id.thr_cut_picker);
        this.mThrCut_bp.initiation(new String[]{"INH", "B1", "B2"}, "INH");
        this.mThrCut_bp.setOnPickerListener(new OnPickerListener() {
            public void onClicked(ButtonPicker picker, String value) {
                ThrottleCurveFragment.this.mThrData.cut_sw = value;
                Log.d(ThrottleCurveFragment.TAG, "Switch = " + value);
            }
        });
        v.findViewById(R.id.reset).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ThrottleCurveFragment.this.resetData();
            }
        });
        v.findViewById(R.id.save).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ThrottleCurveFragment.this.saveData();
                ThrottleCurveFragment.this.sendData();
            }
        });
    }

    private void saveData() {
        DataProviderHelper.writeThrDataToDatabase(getActivity(), this.mThrData);
    }

    private void sendData() {
        sendDataToTranslator(!this.mThrData.cut_sw.equals("INH") ? new MixedData[]{getThrottleData(getActivity(), this.mModel_id, this.mThrData)} : new MixedData[]{getThrottleData(getActivity(), this.mModel_id, this.mThrData), getThrCutData(this.mThrData)});
    }

    private void resetData() {
        final TwoButtonPopDialog dialog = new TwoButtonPopDialog(getActivity());
        dialog.setTitle((CharSequence) "Reset");
        dialog.adjustHeight(380);
        dialog.setMessage(getResources().getString(R.string.str_data_reset_detail));
        dialog.setPositiveButton(R.string.str_ok, new OnClickListener() {
            public void onClick(View v) {
                ThrottleCurveFragment.this.formatThrottleData();
                ThrottleCurveFragment.this.refreshScreen();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.str_cancel, new OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void formatThrottleData() {
        for (int i = 0; i < 3; i++) {
            this.mThrData.thrCurve[i].curvePoints[0] = THR_DATA_DEFAULT_CURVE_POINTS[0];
            this.mThrData.thrCurve[i].curvePoints[1] = THR_DATA_DEFAULT_CURVE_POINTS[1];
            this.mThrData.thrCurve[i].curvePoints[2] = THR_DATA_DEFAULT_CURVE_POINTS[2];
            this.mThrData.thrCurve[i].curvePoints[3] = THR_DATA_DEFAULT_CURVE_POINTS[3];
            this.mThrData.thrCurve[i].curvePoints[4] = THR_DATA_DEFAULT_CURVE_POINTS[4];
        }
        this.mThrData.expo = false;
        this.mThrData.sw = "INH";
        this.mThrData.cut_sw = "INH";
        this.mThrData.cut_value1 = 0;
        this.mThrData.cut_value2 = 50;
    }

    private void initThrottleData() {
        this.mModel_id = getActivity().getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        this.mThrData = DataProviderHelper.readThrDataFromDatabase(getActivity(), this.mModel_id, Utilities.getFmodeState());
    }

    public void setHardwarePos(int stickPosValue, String func) {
        if (this.mServoData.reverse) {
            stickPosValue = 4095 - stickPosValue;
        }
        if (Math.abs(stickPosValue - this.mStickLastPosValue) <= 50) {
            if (stickPosValue > 2037 && stickPosValue <= 2057) {
                if (this.mStickLastPosValue > 2037 && this.mStickLastPosValue <= 2057) {
                    return;
                }
            }
            return;
        }
        if (this.mThrData.expo) {
            this.mSpv.refreshStick(stickPosValue);
        } else {
            this.mBlv.refreshStick(stickPosValue);
        }
        this.mStickLastPosValue = stickPosValue;
    }

    private float[] getCurrentCurvePoints(int sw_state) {
        float[] points = new float[5];
        if (sw_state < 0 || sw_state >= 3) {
            Log.e(TAG, "getCurrentCurvePoints ----Invalid switch state");
        } else {
            for (int index = 0; index < points.length; index++) {
                points[index] = this.mThrData.thrCurve[sw_state].curvePoints[index];
            }
        }
        return points;
    }

    private void setCurve(boolean expo) {
        if (expo) {
            this.mSpv.setVisibility(0);
            this.mSpv.setParams(CURVE_X, getCurrentCurvePoints(this.mCurrentSWstate));
            this.mBlv.setVisibility(8);
            return;
        }
        this.mBlv.setVisibility(0);
        this.mBlv.setParams(CURVE_X, getCurrentCurvePoints(this.mCurrentSWstate));
        this.mSpv.setVisibility(8);
    }

    public void onProgressChanged(ButtonSeekBar btnseek, int value, boolean fromUser) {
        int posIndex = -1;
        if (fromUser) {
            if (btnseek.equals(this.mSeekPos1)) {
                posIndex = 0;
            } else if (btnseek.equals(this.mSeekPos2)) {
                posIndex = 1;
            } else if (btnseek.equals(this.mSeekPos3)) {
                posIndex = 2;
            } else if (btnseek.equals(this.mSeekPos4)) {
                posIndex = 3;
            } else if (btnseek.equals(this.mSeekPos5)) {
                posIndex = 4;
            }
            if (posIndex != -1) {
                this.mThrData.thrCurve[this.mCurrentSWstate].curvePoints[posIndex] = ((float) value) / 10.0f;
                setCurve(this.mThrData.expo);
            }
        }
    }

    public static MixedData getThrottleData(Context context, long modelId, ThrottleData data) {
        ChannelMap[] cm = DataProviderHelper.readChannelMapFromDatabase(context, modelId);
        MixedData mix_data = new MixedData();
        int fmode = Utilities.getFmodeState();
        mix_data.mFmode = fmode;
        mix_data.mChannel = 1;
        mix_data.mhardware = Utilities.getHardwareIndexT(cm[0].hardware);
        mix_data.mHardwareType = Utilities.getHardwareType(cm[0].hardware);
        mix_data.mPriority = 1;
        mix_data.mDr_switch = Utilities.getHardwareIndexT(data.sw);
        mix_data.mMixedType = 0;
        mix_data.mCurvePoint = getCurvePoints(context, data.thrCurve, data.expo);
        ServoData sd = ServoSetupFragment.getServoSetup(DataProviderHelper.readServoDataFromDatabase(context, modelId, fmode), cm[0].function);
        if (sd != null) {
            mix_data.mSpeed = sd.speed;
            mix_data.mReverse = sd.reverse;
        }
        return mix_data;
    }

    public static ArrayList<Integer> getCurvePoints(Context context, ThrCurve[] curve, boolean expo) {
        ArrayList<Integer> pointsArray = new ArrayList();
        for (int i = 0; i < 3; i++) {
            int[] chValues;
            if (expo) {
                chValues = SplineView.getAllPointsChValue(context, 125.0f, -25.0f, CURVE_X, curve[i].curvePoints);
            } else {
                chValues = BrokenLineView.getAllPointsChValue(context, 125.0f, -25.0f, CURVE_X, curve[i].curvePoints);
            }
            pointsArray.addAll(Arrays.asList(Utilities.get17PointsValueForFightControl(125.0f, -25.0f, chValues)));
        }
        return pointsArray;
    }

    public static MixedData getThrCutData(ThrottleData data) {
        MixedData mix_data = new MixedData();
        mix_data.mFmode = Utilities.getFmodeState();
        mix_data.mChannel = 1;
        mix_data.mhardware = Utilities.getHardwareIndexT(data.cut_sw);
        mix_data.mHardwareType = Utilities.getHardwareType(data.cut_sw);
        mix_data.mPriority = 2;
        mix_data.mDr_switch = 0;
        mix_data.mMixedType = 3;
        mix_data.mSwitchStatus.add(0, Boolean.valueOf(true));
        mix_data.mSwitchStatus.add(1, Boolean.valueOf(false));
        mix_data.mSwitchValue.add(0, Integer.valueOf(data.cut_value1));
        mix_data.mSwitchValue.add(1, Integer.valueOf(data.cut_value2));
        mix_data.mSpeed = 10;
        mix_data.mReverse = false;
        return mix_data;
    }

    private void showFailedToSendDialog() {
        final OneButtonPopDialog dialog = new OneButtonPopDialog(getActivity());
        dialog.setTitle((CharSequence) "Failure");
        dialog.adjustHeight(380);
        dialog.setMessage(getResources().getString(R.string.str_send_data_failure));
        dialog.setPositiveButton(R.string.str_ok, new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void sendDataToTranslator(MixedData[] data) {
        mProgressDialog = MyProgressDialog.show(getActivity(), null, getResources().getString(R.string.str_sending_data_dailog), false, false);
        new SendDataTask().execute(data);
    }
}
