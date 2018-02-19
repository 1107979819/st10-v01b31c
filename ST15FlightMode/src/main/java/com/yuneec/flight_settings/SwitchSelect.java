package com.yuneec.flight_settings;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.widget.ButtonPicker;
import com.yuneec.widget.ButtonPicker.OnPickerListener;
import java.util.ArrayList;

public class SwitchSelect extends Activity {
    private static final String[] AUX_LIST = new String[]{"INH", "S1", "S2", "S3", "S4", "B1", "B2"};
    private static final String[] GO_HOME_LIST = new String[]{"INH", "S2", "S3", "B1", "B2"};
    private static final String TAG = "SwitchSelect";
    private static ArrayList<String> auxArray = new ArrayList();
    private static ArrayList<String> gohomeArray = new ArrayList();
    private ButtonPicker mAUX10;
    private ButtonPicker mAUX11;
    private ButtonPicker mAUX2;
    private ButtonPicker mAUX3;
    private ButtonPicker mAUX4;
    private ButtonPicker mAUX5;
    private ButtonPicker mAUX6;
    private ButtonPicker mAUX7;
    private ButtonPicker mAUX8;
    private ButtonPicker mAUX9;
    private OnPickerListener mAUXPickListener10 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[9] = value;
            SwitchSelect.this.setArrayList(9);
        }
    };
    private OnPickerListener mAUXPickListener11 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[10] = value;
            SwitchSelect.this.setArrayList(10);
        }
    };
    private OnPickerListener mAUXPickListener2 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[1] = value;
            SwitchSelect.this.setArrayList(1);
        }
    };
    private OnPickerListener mAUXPickListener3 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[2] = value;
            SwitchSelect.this.setArrayList(2);
        }
    };
    private OnPickerListener mAUXPickListener4 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[3] = value;
            SwitchSelect.this.setArrayList(3);
        }
    };
    private OnPickerListener mAUXPickListener5 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[4] = value;
            SwitchSelect.this.setArrayList(4);
        }
    };
    private OnPickerListener mAUXPickListener6 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[5] = value;
            SwitchSelect.this.setArrayList(5);
        }
    };
    private OnPickerListener mAUXPickListener7 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[6] = value;
            SwitchSelect.this.setArrayList(6);
        }
    };
    private OnPickerListener mAUXPickListener8 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[7] = value;
            SwitchSelect.this.setArrayList(7);
        }
    };
    private OnPickerListener mAUXPickListener9 = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[8] = value;
            SwitchSelect.this.setArrayList(8);
        }
    };
    private ChannelMap[] mChannelMaps;
    private UARTController mController;
    private long mCurrentModelId;
    private ButtonPicker mGoHome;
    private OnPickerListener mGoHomeListener = new OnPickerListener() {
        public void onClicked(ButtonPicker picker, String value) {
            SwitchSelect.this.str_select[0] = value;
            SwitchSelect.this.setArrayList(0);
        }
    };
    private String[] str_select = new String[11];

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.switch_select_main);
        this.mCurrentModelId = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getLong("current_model_id", -2);
        initValue();
        this.mGoHome.setOnPickerListener(this.mGoHomeListener);
        this.mAUX2.setOnPickerListener(this.mAUXPickListener2);
        this.mAUX3.setOnPickerListener(this.mAUXPickListener3);
        this.mAUX4.setOnPickerListener(this.mAUXPickListener4);
        this.mAUX5.setOnPickerListener(this.mAUXPickListener5);
        this.mAUX6.setOnPickerListener(this.mAUXPickListener6);
        this.mAUX7.setOnPickerListener(this.mAUXPickListener7);
        this.mAUX8.setOnPickerListener(this.mAUXPickListener8);
        this.mAUX9.setOnPickerListener(this.mAUXPickListener9);
        this.mAUX10.setOnPickerListener(this.mAUXPickListener10);
        this.mAUX11.setOnPickerListener(this.mAUXPickListener11);
    }

    private void initValue() {
        this.mGoHome = (ButtonPicker) findViewById(R.id.bp_go_home);
        this.mAUX2 = (ButtonPicker) findViewById(R.id.bp_aux2);
        this.mAUX3 = (ButtonPicker) findViewById(R.id.bp_aux3);
        this.mAUX4 = (ButtonPicker) findViewById(R.id.bp_aux4);
        this.mAUX5 = (ButtonPicker) findViewById(R.id.bp_aux5);
        this.mAUX6 = (ButtonPicker) findViewById(R.id.bp_aux6);
        this.mAUX7 = (ButtonPicker) findViewById(R.id.bp_aux7);
        this.mAUX8 = (ButtonPicker) findViewById(R.id.bp_aux8);
        this.mAUX9 = (ButtonPicker) findViewById(R.id.bp_aux9);
        this.mAUX10 = (ButtonPicker) findViewById(R.id.bp_aux10);
        this.mAUX11 = (ButtonPicker) findViewById(R.id.bp_aux11);
        readDataToDB();
        setGoHomeArrayList();
        setAUXArrayList();
        this.mGoHome.initiationArray(gohomeArray, this.str_select[0]);
        this.mAUX2.initiationArray(auxArray, this.str_select[1]);
        this.mAUX3.initiationArray(auxArray, this.str_select[2]);
        this.mAUX4.initiationArray(auxArray, this.str_select[3]);
        this.mAUX5.initiationArray(auxArray, this.str_select[4]);
        this.mAUX6.initiationArray(auxArray, this.str_select[5]);
        this.mAUX7.initiationArray(auxArray, this.str_select[6]);
        this.mAUX8.initiationArray(auxArray, this.str_select[7]);
        this.mAUX9.initiationArray(auxArray, this.str_select[8]);
        this.mAUX10.initiationArray(auxArray, this.str_select[9]);
        this.mAUX11.initiationArray(auxArray, this.str_select[10]);
    }

    protected void onResume() {
        super.onResume();
        this.mController = UARTController.getInstance();
        this.mController.startReading();
        if (!Utilities.ensureSimState(this.mController)) {
            Log.e(TAG, "fails to enter sim state");
        }
    }

    protected void onPause() {
        super.onPause();
        Utilities.sendAllDataToFlightControl(this, this.mCurrentModelId, this.mController);
        if (!Utilities.ensureAwaitState(this.mController)) {
            Log.e(TAG, "fails to enter await state");
        }
        Utilities.UartControllerStandBy(this.mController);
        this.mController = null;
    }

    private void setArrayList(int position) {
        switch (position) {
            case 0:
                setAUXArrayList();
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                setGoHomeArrayList();
                break;
        }
        wirteDataToDB();
    }

    private void wirteDataToDB() {
        for (int i = 0; i < this.str_select.length; i++) {
            this.mChannelMaps[i + 4].hardware = this.str_select[i];
        }
        DataProviderHelper.writeChannelMapFromDatabase(this, this.mChannelMaps);
    }

    private void readDataToDB() {
        this.mChannelMaps = DataProviderHelper.readChannelMapFromDatabase(this, this.mCurrentModelId);
        int i;
        if (this.mChannelMaps != null) {
            for (i = 4; i < this.mChannelMaps.length; i++) {
                this.str_select[i - 4] = this.mChannelMaps[i].hardware;
            }
            return;
        }
        for (i = 0; i < this.str_select.length; i++) {
            this.str_select[i] = "INH";
        }
    }

    private void initGoHomeArrayList() {
        gohomeArray.clear();
        for (Object add : GO_HOME_LIST) {
            gohomeArray.add(add);
        }
    }

    private void initAUXArrayList() {
        auxArray.clear();
        for (Object add : AUX_LIST) {
            auxArray.add(add);
        }
    }

    private void setGoHomeArrayList() {
        initGoHomeArrayList();
        for (int i = 1; i < this.str_select.length; i++) {
            if (!"INH".equals(this.str_select[i])) {
                for (int j = 0; j < gohomeArray.size(); j++) {
                    if (this.str_select[i].equals(gohomeArray.get(j))) {
                        gohomeArray.remove(j);
                    }
                }
            }
        }
    }

    private void setAUXArrayList() {
        initAUXArrayList();
        for (int i = 0; i < auxArray.size(); i++) {
            if (this.str_select[0].equals(auxArray.get(i)) && !this.str_select[0].equals("INH")) {
                auxArray.remove(i);
            }
        }
    }
}
