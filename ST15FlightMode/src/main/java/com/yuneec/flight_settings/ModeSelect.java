package com.yuneec.flight_settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flightmode15.R;
import com.yuneec.widget.TwoButtonPopDialog;

public class ModeSelect extends Activity implements OnCheckedChangeListener {
    private static final String TAG = "ModeSelect";
    private int current_mode;
    private TwoButtonPopDialog mConfirmDialog;
    private int mLastMode;
    private SharedPreferences mPrefs;
    private RadioGroup mode_select;
    private boolean shouldConfirm = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.mode_select_main);
        this.mode_select = (RadioGroup) findViewById(R.id.mode_select);
        this.mPrefs = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        this.current_mode = this.mPrefs.getInt(FlightSettings.FLIGHT_SETTINGS_MODE, 2);
        this.mLastMode = this.current_mode;
        initSelectedMode();
        this.mode_select.setOnCheckedChangeListener(this);
    }

    private void initSelectedMode() {
        if (this.current_mode == 1) {
            this.mode_select.check(R.id.mode_select_1);
        } else if (this.current_mode == 2) {
            this.mode_select.check(R.id.mode_select_2);
        } else if (this.current_mode == 3) {
            this.mode_select.check(R.id.mode_select_3);
        } else if (this.current_mode == 4) {
            this.mode_select.check(R.id.mode_select_4);
        }
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        this.mLastMode = this.current_mode;
        if (checkedId == R.id.mode_select_1) {
            this.current_mode = 1;
        } else if (checkedId == R.id.mode_select_2) {
            this.current_mode = 2;
        } else if (checkedId == R.id.mode_select_3) {
            this.current_mode = 3;
        } else if (checkedId == R.id.mode_select_4) {
            this.current_mode = 4;
        } else {
            Log.e(TAG, "no mode selected !");
            return;
        }
        this.mPrefs.edit().putInt(FlightSettings.FLIGHT_SETTINGS_MODE, this.current_mode).commit();
        DataProviderHelper.changeMode(getApplicationContext(), this.current_mode);
        if (this.shouldConfirm) {
            showConfirmDialog();
        } else {
            this.shouldConfirm = true;
        }
    }

    private void showConfirmDialog() {
        if (this.mConfirmDialog == null) {
            this.mConfirmDialog = new TwoButtonPopDialog(this);
            this.mConfirmDialog.adjustHeight(380);
            this.mConfirmDialog.setTitle((int) R.string.str_confirm);
            this.mConfirmDialog.setMessage((int) R.string.str_confirm_select_mode_message);
            this.mConfirmDialog.setPositiveButton(17039379, new OnClickListener() {
                public void onClick(View v) {
                    ModeSelect.this.mConfirmDialog.dismiss();
                }
            });
            this.mConfirmDialog.setNegativeButton(17039369, new OnClickListener() {
                public void onClick(View v) {
                    ModeSelect.this.mConfirmDialog.dismiss();
                    ModeSelect.this.shouldConfirm = false;
                    ModeSelect.this.current_mode = ModeSelect.this.mLastMode;
                    if (ModeSelect.this.current_mode == 1) {
                        ((RadioButton) ModeSelect.this.findViewById(R.id.mode_select_1)).setChecked(true);
                    } else if (ModeSelect.this.current_mode == 2) {
                        ((RadioButton) ModeSelect.this.findViewById(R.id.mode_select_2)).setChecked(true);
                    } else if (ModeSelect.this.current_mode == 3) {
                        ((RadioButton) ModeSelect.this.findViewById(R.id.mode_select_3)).setChecked(true);
                    } else if (ModeSelect.this.current_mode == 4) {
                        ((RadioButton) ModeSelect.this.findViewById(R.id.mode_select_4)).setChecked(true);
                    }
                }
            });
        }
        this.mConfirmDialog.show();
    }
}
