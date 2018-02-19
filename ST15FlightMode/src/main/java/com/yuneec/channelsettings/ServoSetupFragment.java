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

public class ServoSetupFragment extends Fragment implements OnPickerListener {
    private static ServoData SER_DATA_DEFAULT = new ServoData();
    private static final String TAG = "ServoSetupFragment";
    private String[] mAllFunctions;
    private ButtonPicker mFunc_bp;
    private int mIndex;
    private long mModel_id;
    private MyProgressDialog mProgressDialog;
    private Switch mReverse_sw;
    private ServoData[] mServoData;
    private ButtonPicker mSpeed_bp;
    private ButtonSeekBar mSubTrim_bp;
    private ButtonPicker mTravelL_bp;
    private ButtonPicker mTravelR_bp;

    private class SendDataTask extends AsyncTask<MixedData, Void, Boolean> {
        private SendDataTask() {
        }

        protected Boolean doInBackground(MixedData... params) {
            boolean result = true;
            UARTController controller = ((ChannelSettings) ServoSetupFragment.this.getActivity()).getUARTController();
            for (int i = 0; i < params.length; i++) {
                if (!controller.syncMixingData(true, params[i], 1)) {
                    Log.e(ServoSetupFragment.TAG, "Failed to send Servo data at " + i);
                    result = false;
                }
            }
            return Boolean.valueOf(result);
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            ServoSetupFragment.this.mProgressDialog.dismiss();
            if (result.booleanValue()) {
                MyToast.makeText(ServoSetupFragment.this.getActivity(), ServoSetupFragment.this.getString(R.string.str_save_successed), 0, 1).show();
            } else {
                ServoSetupFragment.this.showFailedToSendDialog();
            }
        }
    }

    static {
        SER_DATA_DEFAULT.subTrim = 0;
        SER_DATA_DEFAULT.reverse = false;
        SER_DATA_DEFAULT.speed = 10;
        SER_DATA_DEFAULT.travelL = -100;
        SER_DATA_DEFAULT.travelR = 100;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initServoData();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cs_fragment_servo_setup, null);
        initiation(v);
        return v;
    }

    public void onResume() {
        super.onResume();
        refreshScreen(this.mIndex);
    }

    public void onPause() {
        super.onPause();
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void refreshScreen(int index) {
        this.mSubTrim_bp.setProgress(this.mServoData[index].subTrim);
        this.mReverse_sw.setChecked(this.mServoData[index].reverse);
        this.mSpeed_bp.setIntegerValue(this.mServoData[index].speed);
        this.mTravelL_bp.setIntegerValue(this.mServoData[index].travelL);
        this.mTravelR_bp.setIntegerValue(this.mServoData[index].travelR);
    }

    private void initServoData() {
        this.mAllFunctions = getResources().getStringArray(R.array.servo_label_1);
        this.mModel_id = getActivity().getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        this.mServoData = DataProviderHelper.readServoDataFromDatabase(getActivity(), this.mModel_id, Utilities.getFmodeState());
        if (this.mServoData.length != this.mAllFunctions.length) {
            Log.e(TAG, "Exceptional servo data");
        }
    }

    private void initiation(View v) {
        this.mFunc_bp = (ButtonPicker) v.findViewById(R.id.func_picker);
        this.mFunc_bp.setOnPickerListener(this);
        this.mFunc_bp.initiation(this.mAllFunctions, "Thr");
        this.mSubTrim_bp = (ButtonSeekBar) v.findViewById(R.id.subtrim_bsb);
        this.mSubTrim_bp.setOnButtonSeekChangeListener(new onButtonSeekChangeListener() {
            public void onProgressChanged(ButtonSeekBar btnseek, int value, boolean fromUser) {
                ServoSetupFragment.this.mServoData[ServoSetupFragment.this.mIndex].subTrim = value;
            }
        });
        this.mSubTrim_bp.initiation(null, 10, -10);
        this.mReverse_sw = (Switch) v.findViewById(R.id.reverse_switch);
        this.mReverse_sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ServoSetupFragment.this.mServoData[ServoSetupFragment.this.mIndex].reverse = isChecked;
            }
        });
        this.mSpeed_bp = (ButtonPicker) v.findViewById(R.id.speed_picker);
        this.mSpeed_bp.setOnPickerListener(this);
        this.mSpeed_bp.initiation(30, 10, 10, 5);
        this.mTravelL_bp = (ButtonPicker) v.findViewById(R.id.travle_picker_l);
        this.mTravelL_bp.setOnPickerListener(this);
        this.mTravelL_bp.initiation(-30, (int) Utilities.OFFSET_SWITCH_MIN_2, -100, 1);
        this.mTravelR_bp = (ButtonPicker) v.findViewById(R.id.travle_picker_r);
        this.mTravelR_bp.setOnPickerListener(this);
        this.mTravelR_bp.initiation((int) Utilities.OFFSET_SWITCH_MAX_2, 30, 100, 1);
        v.findViewById(R.id.reset).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ServoSetupFragment.this.resetData();
            }
        });
        v.findViewById(R.id.save).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ServoSetupFragment.this.saveData();
                ServoSetupFragment.this.sendData();
            }
        });
    }

    private void saveData() {
        DataProviderHelper.writeServoDataToDatabase(getActivity(), this.mServoData);
    }

    private void sendData() {
        sendDataToTranslator(getAllServoSetup(getActivity()));
    }

    private void resetData() {
        final TwoButtonPopDialog dialog = new TwoButtonPopDialog(getActivity());
        dialog.setTitle((CharSequence) "Reset");
        dialog.adjustHeight(380);
        dialog.setMessage(getResources().getString(R.string.str_data_reset_detail));
        dialog.setPositiveButton(R.string.str_ok, new OnClickListener() {
            public void onClick(View v) {
                ServoSetupFragment.this.formatServoData(ServoSetupFragment.this.mServoData[ServoSetupFragment.this.mIndex], ServoSetupFragment.this.mIndex);
                ServoSetupFragment.this.refreshScreen(ServoSetupFragment.this.mIndex);
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

    private void formatServoData(ServoData servoData, int index) {
        servoData.func = this.mAllFunctions[index];
        servoData.subTrim = SER_DATA_DEFAULT.subTrim;
        servoData.reverse = SER_DATA_DEFAULT.reverse;
        servoData.speed = SER_DATA_DEFAULT.speed;
        servoData.travelL = SER_DATA_DEFAULT.travelL;
        servoData.travelR = SER_DATA_DEFAULT.travelR;
    }

    public void onClicked(ButtonPicker picker, String value) {
        if (picker.equals(this.mFunc_bp)) {
            this.mIndex = Arrays.asList(this.mAllFunctions).indexOf(value);
            refreshScreen(this.mIndex);
        } else if (picker.equals(this.mSpeed_bp)) {
            this.mServoData[this.mIndex].speed = Integer.valueOf(value).intValue();
        } else if (picker.equals(this.mTravelL_bp)) {
            this.mServoData[this.mIndex].travelL = Integer.valueOf(value).intValue();
        } else if (picker.equals(this.mTravelR_bp)) {
            this.mServoData[this.mIndex].travelR = Integer.valueOf(value).intValue();
        }
    }

    public static ServoData getServoSetup(ServoData[] datas, String func) {
        ArrayList<String> funcArray = new ArrayList();
        for (ServoData servoData : datas) {
            funcArray.add(servoData.func);
        }
        int index = funcArray.indexOf(func);
        if (index != -1) {
            return datas[index];
        }
        Log.e(TAG, "getServoSetup----Invalid functions");
        return null;
    }

    private MixedData[] getAllServoSetup(Context context) {
        int i;
        ChannelMap[] cm = DataProviderHelper.readChannelMapFromDatabase(context, this.mModel_id);
        int fmode = Utilities.getFmodeState();
        MixedData[] datas = new MixedData[this.mServoData.length];
        for (i = 0; i < datas.length; i++) {
            datas[i] = new MixedData();
            datas[i].mFmode = fmode;
            datas[i].mChannel = i + 1;
            datas[i].mhardware = Utilities.getHardwareIndexT(cm[i].hardware);
            datas[i].mHardwareType = Utilities.getHardwareType(cm[i].hardware);
            datas[i].mPriority = 1;
            ServoData sd = getServoSetup(this.mServoData, cm[i].function);
            if (sd != null) {
                datas[i].mSpeed = sd.speed;
                datas[i].mReverse = sd.reverse;
            }
        }
        datas[0] = ThrottleCurveFragment.getThrottleData(context, this.mModel_id, DataProviderHelper.readThrDataFromDatabase(context, this.mModel_id, fmode));
        MixedData[] drDatas = DR_Fragment.getDRDatas(context, this.mModel_id, DataProviderHelper.readDRDataFromDatabase(context, this.mModel_id, fmode));
        for (i = 0; i < drDatas.length; i++) {
            datas[drDatas[i].mChannel - 1] = drDatas[i];
        }
        return datas;
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
        this.mProgressDialog = MyProgressDialog.show(getActivity(), null, getResources().getString(R.string.str_sending_data_dailog), false, false);
        new SendDataTask().execute(data);
    }
}
