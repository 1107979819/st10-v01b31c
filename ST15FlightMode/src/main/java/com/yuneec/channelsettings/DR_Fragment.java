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
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.yuneec.channelsettings.DRData.CurveParams;
import com.yuneec.channelsettings.DualPickerGroup.OnDualPickerListener;
import com.yuneec.curve.CurveView.OnCurveTouchListener;
import com.yuneec.curve.Expo2LineView;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flight_settings.ChannelMap;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.MixedData;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import com.yuneec.widget.ButtonPicker;
import com.yuneec.widget.ButtonPicker.OnPickerListener;
import com.yuneec.widget.MyProgressDialog;
import com.yuneec.widget.MyToast;
import com.yuneec.widget.OneButtonPopDialog;
import com.yuneec.widget.TwoButtonPopDialog;
import java.util.ArrayList;
import java.util.Arrays;

public class DR_Fragment extends Fragment {
    private static final float DR_DATA_DEFAULT_EXPO1 = 0.0f;
    private static final float DR_DATA_DEFAULT_EXPO2 = 0.0f;
    private static final float DR_DATA_DEFAULT_OFFSET = 0.0f;
    private static final float DR_DATA_DEFAULT_RATE1 = -100.0f;
    private static final float DR_DATA_DEFAULT_RATE2 = 100.0f;
    private static final String[] FUNCS = new String[]{"Ail", "Ele", "Rud"};
    private static final String TAG = "DR_Fragment";
    private int mCurrentSWstate;
    private DRData[] mDRData;
    private DualPickerGroup mDR_pg;
    private Expo2LineView mElv;
    private DualPickerGroup mExpo_pg;
    private RadioGroup mFuncGrp;
    private int mFuncIndex = 0;
    private long mModel_id;
    private MyProgressDialog mProgressDialog;
    private ServoData mServoData;
    private int mStickLastPosValue = -100;
    private ButtonPicker mSwitch_bp;

    private class SendDataTask extends AsyncTask<MixedData, Void, Boolean> {
        private SendDataTask() {
        }

        protected Boolean doInBackground(MixedData... params) {
            boolean result = true;
            UARTController controller = ((ChannelSettings) DR_Fragment.this.getActivity()).getUARTController();
            for (MixedData syncMixingData : params) {
                if (!controller.syncMixingData(true, syncMixingData, 0)) {
                    result = false;
                }
            }
            return Boolean.valueOf(result);
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            DR_Fragment.this.mProgressDialog.dismiss();
            if (result.booleanValue()) {
                MyToast.makeText(DR_Fragment.this.getActivity(), DR_Fragment.this.getString(R.string.str_save_successed), 0, 1).show();
            } else {
                DR_Fragment.this.showFailedToSendDialog();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDRData();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cs_fragment_dr, null);
        initiation(v);
        return v;
    }

    public void onResume() {
        super.onResume();
        refreshScreen(this.mFuncIndex);
    }

    public void onPause() {
        super.onPause();
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            this.mServoData = ServoSetupFragment.getServoSetup(DataProviderHelper.readServoDataFromDatabase(getActivity(), this.mModel_id, 0), FUNCS[this.mFuncIndex]);
        } else {
            this.mStickLastPosValue = -100;
        }
    }

    private void refreshScreen(int index) {
        setCurve(index);
        this.mExpo_pg.setValue1(this.mDRData[index].curveparams[this.mCurrentSWstate].expo1);
        this.mExpo_pg.setValue2(this.mDRData[index].curveparams[this.mCurrentSWstate].expo2);
        this.mDR_pg.setValue1(this.mDRData[index].curveparams[this.mCurrentSWstate].rate1);
        this.mDR_pg.setValue2(this.mDRData[index].curveparams[this.mCurrentSWstate].rate2);
        this.mSwitch_bp.setStringValue(this.mDRData[index].sw);
    }

    private void initiation(View v) {
        this.mFuncGrp = (RadioGroup) v.findViewById(R.id.func_group);
        this.mFuncGrp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.func_ail:
                        DR_Fragment.this.mFuncIndex = 0;
                        break;
                    case R.id.func_ele:
                        DR_Fragment.this.mFuncIndex = 1;
                        break;
                    case R.id.func_rud:
                        DR_Fragment.this.mFuncIndex = 2;
                        break;
                    default:
                        DR_Fragment.this.mFuncIndex = 0;
                        break;
                }
                DR_Fragment.this.refreshScreen(DR_Fragment.this.mFuncIndex);
            }
        });
        this.mElv = (Expo2LineView) v.findViewById(R.id.expo2lineView);
        this.mElv.setOnCurveTouchListener(new OnCurveTouchListener() {
            public void onCurveValueChanged(int pointIndex) {
            }

            public void onCurveValueChanged(int coefficient, float value) {
                float textValue;
                switch (coefficient) {
                    case 1:
                        textValue = DR_Fragment.this.mElv.convertCurveValueToUserValueL(value);
                        DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].curveparams[DR_Fragment.this.mCurrentSWstate].rate1 = textValue;
                        DR_Fragment.this.mDR_pg.setValue1(textValue);
                        return;
                    case 2:
                        textValue = DR_Fragment.this.mElv.convertCurveValueToUserValueR(value);
                        DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].curveparams[DR_Fragment.this.mCurrentSWstate].rate2 = textValue;
                        DR_Fragment.this.mDR_pg.setValue2(textValue);
                        return;
                    case 3:
                    case 4:
                    case 5:
                        textValue = DR_Fragment.this.mElv.getLeftExpoValue(value);
                        CurveParams curveParams = DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].curveparams[DR_Fragment.this.mCurrentSWstate];
                        DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].curveparams[DR_Fragment.this.mCurrentSWstate].expo2 = textValue;
                        curveParams.expo1 = textValue;
                        DR_Fragment.this.mExpo_pg.setValue1(textValue);
                        DR_Fragment.this.mExpo_pg.setValue2(textValue);
                        return;
                    default:
                        return;
                }
            }
        });
        this.mExpo_pg = (DualPickerGroup) v.findViewById(R.id.expo_dual_picker);
        this.mExpo_pg.initation(DR_DATA_DEFAULT_RATE2, DR_DATA_DEFAULT_RATE1, 0.0f, 0.0f, 0.1f);
        this.mExpo_pg.setOnDualPickerListener(new OnDualPickerListener() {
            public void onClicked(float value1, float value2) {
                DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].curveparams[DR_Fragment.this.mCurrentSWstate].expo1 = value1;
                DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].curveparams[DR_Fragment.this.mCurrentSWstate].expo2 = value2;
                DR_Fragment.this.setCurve(DR_Fragment.this.mFuncIndex);
            }
        });
        this.mDR_pg = (DualPickerGroup) v.findViewById(R.id.dr_dual_picker);
        this.mDR_pg.initation(150.0f, -150.0f, 0.0f, 0.0f, 0.1f);
        this.mDR_pg.setOnDualPickerListener(new OnDualPickerListener() {
            public void onClicked(float value1, float value2) {
                DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].curveparams[DR_Fragment.this.mCurrentSWstate].rate1 = value1;
                DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].curveparams[DR_Fragment.this.mCurrentSWstate].rate2 = value2;
                DR_Fragment.this.setCurve(DR_Fragment.this.mFuncIndex);
            }
        });
        this.mSwitch_bp = (ButtonPicker) v.findViewById(R.id.dr_sw_picker);
        this.mSwitch_bp.initiation(new String[]{"INH", "S2", "S3", "S4"}, "INH");
        this.mSwitch_bp.setOnPickerListener(new OnPickerListener() {
            public void onClicked(ButtonPicker picker, String value) {
                DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex].sw = value;
            }
        });
        v.findViewById(R.id.reset).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DR_Fragment.this.resetData();
            }
        });
        v.findViewById(R.id.save).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DR_Fragment.this.saveData();
                DR_Fragment.this.sendData();
            }
        });
    }

    private void saveData() {
        DataProviderHelper.writeDRDataToDatabase(getActivity(), this.mDRData);
    }

    private void sendData() {
        sendDataToTranslator(getDRDatas(getActivity(), this.mModel_id, this.mDRData));
    }

    public static MixedData[] getDRDatas(Context context, long modelId, DRData[] data) {
        int i;
        ChannelMap[] cm = DataProviderHelper.readChannelMapFromDatabase(context, modelId);
        ArrayList<MixedData> dataList = new ArrayList();
        int fmode = Utilities.getFmodeState();
        int index = 0;
        for (i = 0; i < cm.length; i++) {
            MixedData mix_data = new MixedData();
            if (cm[i].alias.contains(FUNCS[0])) {
                mix_data.mDr_switch = Utilities.getHardwareIndexT(data[0].sw);
                mix_data.mCurvePoint = getCurvePoints(context, data, 0);
            } else if (cm[i].alias.contains(FUNCS[1])) {
                mix_data.mDr_switch = Utilities.getHardwareIndexT(data[1].sw);
                mix_data.mCurvePoint = getCurvePoints(context, data, 1);
            } else if (cm[i].alias.contains(FUNCS[2])) {
                mix_data.mDr_switch = Utilities.getHardwareIndexT(data[2].sw);
                mix_data.mCurvePoint = getCurvePoints(context, data, 2);
            } else {
            }
            mix_data.mFmode = fmode;
            mix_data.mChannel = cm[i].channel;
            mix_data.mhardware = Utilities.getHardwareIndexT(cm[i].hardware);
            mix_data.mHardwareType = Utilities.getHardwareType(cm[i].hardware);
            mix_data.mPriority = 1;
            ServoData sd = ServoSetupFragment.getServoSetup(DataProviderHelper.readServoDataFromDatabase(context, modelId, fmode), cm[i].function);
            if (sd != null) {
                mix_data.mSpeed = sd.speed;
                mix_data.mReverse = sd.reverse;
            }
            dataList.add(index, mix_data);
            index++;
        }
        MixedData[] datas = new MixedData[dataList.size()];
        for (i = 0; i < datas.length; i++) {
            datas[i] = (MixedData) dataList.get(i);
        }
        return datas;
    }

    private static ArrayList<Integer> getCurvePoints(Context context, DRData[] data, int func_index) {
        ArrayList<Integer> pointsArray = new ArrayList();
        for (int i = 0; i < 3; i++) {
            pointsArray.addAll(Arrays.asList(Utilities.get17PointsValueForFightControl(150.0f, -150.0f, Expo2LineView.getAllPointsChValue(context, 150.0f, -150.0f, data[func_index].curveparams[i].rate1, data[func_index].curveparams[i].rate2, data[func_index].curveparams[i].expo1, data[func_index].curveparams[i].expo2, data[func_index].curveparams[i].offset))));
        }
        return pointsArray;
    }

    private void resetData() {
        final TwoButtonPopDialog dialog = new TwoButtonPopDialog(getActivity());
        dialog.setTitle((CharSequence) "Reset");
        dialog.adjustHeight(380);
        dialog.setMessage(getResources().getString(R.string.str_data_reset_detail));
        dialog.setPositiveButton(R.string.str_ok, new OnClickListener() {
            public void onClick(View v) {
                DR_Fragment.this.formatDRData(DR_Fragment.this.mDRData[DR_Fragment.this.mFuncIndex]);
                DR_Fragment.this.refreshScreen(DR_Fragment.this.mFuncIndex);
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

    private void formatDRData(DRData drData) {
        drData.curveparams[this.mCurrentSWstate].rate1 = DR_DATA_DEFAULT_RATE1;
        drData.curveparams[this.mCurrentSWstate].rate2 = DR_DATA_DEFAULT_RATE2;
        drData.curveparams[this.mCurrentSWstate].expo1 = 0.0f;
        drData.curveparams[this.mCurrentSWstate].expo2 = 0.0f;
        drData.curveparams[this.mCurrentSWstate].offset = 0.0f;
    }

    private void initDRData() {
        this.mModel_id = getActivity().getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        this.mDRData = DataProviderHelper.readDRDataFromDatabase(getActivity(), this.mModel_id, Utilities.getFmodeState());
    }

    public void setHardwarePos(Channel chMsg, ChannelMap[] cm) {
        int hw_index = ChannelSettings.getHardwareIndex(getActivity(), cm[this.mFuncIndex + 1].hardware);
        if (hw_index < 0 || hw_index >= chMsg.channels.size()) {
            Log.e(TAG, "Can't get hardware value");
            return;
        }
        int stickPosValue = ((Float) chMsg.channels.get(hw_index)).intValue();
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
        this.mElv.refreshStick(stickPosValue);
        this.mStickLastPosValue = stickPosValue;
    }

    private void setCurve(int index) {
        this.mElv.setParams(this.mDRData[index].curveparams[this.mCurrentSWstate].rate1, this.mDRData[index].curveparams[this.mCurrentSWstate].rate2, this.mDRData[index].curveparams[this.mCurrentSWstate].expo1, this.mDRData[index].curveparams[this.mCurrentSWstate].expo2, this.mDRData[index].curveparams[this.mCurrentSWstate].offset);
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
