package com.spreadtrum.android.eng;

import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class networkmodeselect extends PreferenceActivity implements OnPreferenceChangeListener {
    private ListPreference mButtonPreferredNetworkMode;
    private engfetch mEf;
    private MyHandler mHandler;
    private Phone[] mPhone;
    private int mSocketID = 0;

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    handleGetPreferredNetworkTypeResponse(msg);
                    return;
                case 1:
                    handleSetPreferredNetworkTypeResponse(msg);
                    return;
                default:
                    return;
            }
        }

        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = msg.obj;
            networkmodeselect.log(" 3 ");
            if (ar.exception == null) {
                int i;
                int modemNetworkMode = ((int[]) ar.result)[0];
                networkmodeselect.log("handleGetPreferredNetworkTypeResponse: modemNetworkMode = " + modemNetworkMode);
                int settingsNetworkMode = 0;
                for (i = 0; i < 2; i++) {
                    if (networkmodeselect.this.mPhone[i] != null) {
                        settingsNetworkMode = Secure.getInt(networkmodeselect.this.mPhone[i].getContext().getContentResolver(), "preferred_network_mode", 0);
                    }
                }
                networkmodeselect.log("handleGetPreferredNetworkTypeReponse: settingsNetworkMode = " + settingsNetworkMode);
                if (modemNetworkMode == 0 || modemNetworkMode == 1 || modemNetworkMode == 2) {
                    networkmodeselect.log("handleGetPreferredNetworkTypeResponse: if 1: modemNetworkMode = " + modemNetworkMode);
                    if (modemNetworkMode != settingsNetworkMode) {
                        networkmodeselect.log("handleGetPreferredNetworkTypeResponse: if 2: modemNetworkMode != settingsNetworkMode");
                        settingsNetworkMode = modemNetworkMode;
                        networkmodeselect.log("handleGetPreferredNetworkTypeResponse: if 2: settingsNetworkMode = " + settingsNetworkMode);
                        for (i = 0; i < 2; i++) {
                            if (networkmodeselect.this.mPhone[i] != null) {
                                Secure.putInt(networkmodeselect.this.mPhone[i].getContext().getContentResolver(), "preferred_network_mode", settingsNetworkMode);
                            }
                        }
                    }
                    networkmodeselect.log(" 4 ");
                    networkmodeselect.this.UpdatePreferredNetworkModeSummary(modemNetworkMode);
                    networkmodeselect.this.mButtonPreferredNetworkMode.setValue(Integer.toString(modemNetworkMode));
                    return;
                }
                networkmodeselect.log("handleGetPreferredNetworkTypeResponse: else: reset to default");
                resetNetworkModeToDefault();
            }
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = msg.obj;
            networkmodeselect.log(" 5 ");
            int i;
            if (ar.exception == null) {
                int networkMode = Integer.valueOf(networkmodeselect.this.mButtonPreferredNetworkMode.getValue()).intValue();
                for (i = 0; i < 2; i++) {
                    if (networkmodeselect.this.mPhone[i] != null) {
                        Secure.putInt(networkmodeselect.this.mPhone[i].getContext().getContentResolver(), "preferred_network_mode", networkMode);
                    }
                }
                return;
            }
            for (i = 0; i < 2; i++) {
                if (networkmodeselect.this.mPhone[i] != null) {
                    networkmodeselect.this.mPhone[i].getPreferredNetworkType(obtainMessage(0));
                }
            }
        }

        private void resetNetworkModeToDefault() {
            networkmodeselect.log(" 6 ");
            networkmodeselect.this.mButtonPreferredNetworkMode.setValue(Integer.toString(0));
            for (int i = 0; i < 2; i++) {
                if (networkmodeselect.this.mPhone[i] != null) {
                    Secure.putInt(networkmodeselect.this.mPhone[i].getContext().getContentResolver(), "preferred_network_mode", 0);
                    networkmodeselect.this.mPhone[i].setPreferredNetworkType(0, obtainMessage(1));
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TelephonyManager.getDefault().getModemType() == 1) {
            addPreferencesFromResource(R.layout.networkselect);
        } else {
            addPreferencesFromResource(R.layout.networkselect_gsm_only);
        }
        log(" 11 ");
        this.mButtonPreferredNetworkMode = (ListPreference) findPreference("preferred_network_mode_key");
        log(" 12 ");
        this.mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);
        log(" 13 ");
        this.mHandler = new MyHandler(Looper.myLooper());
        this.mPhone = new Phone[2];
        if (PhoneFactory.isCardReady(0)) {
            this.mPhone[0] = PhoneFactory.getPhone(0);
            if (PhoneFactory.isCardReady(1)) {
                this.mPhone[1] = PhoneFactory.getPhone(1);
            } else {
                this.mPhone[1] = null;
            }
        } else {
            this.mPhone[0] = null;
            if (PhoneFactory.isCardReady(1)) {
                this.mPhone[1] = PhoneFactory.getPhone(1);
            } else {
                this.mPhone[1] = null;
            }
        }
        for (int i = 0; i < 2; i++) {
            if (this.mPhone[i] != null) {
                this.mPhone[i].getPreferredNetworkType(this.mHandler.obtainMessage(0));
            }
        }
        log(" 14 ");
        log(" 2 ");
        this.mEf = new engfetch();
        this.mSocketID = this.mEf.engopen();
    }

    private void UpdatePreferredNetworkModeSummary(int NetworkMode) {
        switch (NetworkMode) {
            case 0:
                this.mButtonPreferredNetworkMode.setSummary("Preferred network mode: Auto");
                return;
            case 1:
                this.mButtonPreferredNetworkMode.setSummary("Preferred network mode: GSM only");
                return;
            case 2:
                this.mButtonPreferredNetworkMode.setSummary("Preferred network mode: TD-SCDMA only");
                return;
            default:
                this.mButtonPreferredNetworkMode.setSummary("Preferred network mode: Auto");
                return;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == this.mButtonPreferredNetworkMode) {
            int i;
            log(" 7 ");
            this.mButtonPreferredNetworkMode.setValue((String) objValue);
            int buttonNetworkMode = Integer.valueOf((String) objValue).intValue();
            int settingsNetworkMode = 0;
            for (i = 0; i < 2; i++) {
                if (this.mPhone[i] != null) {
                    settingsNetworkMode = Secure.getInt(this.mPhone[i].getContext().getContentResolver(), "preferred_network_mode", 0);
                }
            }
            if (buttonNetworkMode != settingsNetworkMode) {
                if (setNetworkMode(buttonNetworkMode)) {
                    int modemNetworkMode;
                    switch (buttonNetworkMode) {
                        case 0:
                            modemNetworkMode = 0;
                            break;
                        case 1:
                            modemNetworkMode = 1;
                            break;
                        case 2:
                            modemNetworkMode = 2;
                            break;
                        default:
                            modemNetworkMode = 0;
                            break;
                    }
                    UpdatePreferredNetworkModeSummary(buttonNetworkMode);
                    for (i = 0; i < 2; i++) {
                        if (this.mPhone[i] != null) {
                            Secure.putInt(this.mPhone[i].getContext().getContentResolver(), "preferred_network_mode", buttonNetworkMode);
                            this.mPhone[i].setPreferredNetworkType(modemNetworkMode, this.mHandler.obtainMessage(1));
                        }
                    }
                } else {
                    log("!!!! setNetworkMode fail  !!!");
                }
            }
            if (this.mButtonPreferredNetworkMode.getDialog() != null) {
                this.mButtonPreferredNetworkMode.getDialog().dismiss();
            }
            finish();
        }
        return true;
    }

    private static void log(String msg) {
        Log.d("networkmodeselect", msg);
    }

    private boolean setNetworkMode(int mode) {
        Log.d("networkmodeselect", "setNetworkMode mode=" + mode);
        switch (mode) {
            case 0:
                if (!changeNvitemValue(2118, 2)) {
                    DisplayToast("Failed and try again[2118]");
                    return false;
                } else if (!changeNvitemValue(2124, 1)) {
                    DisplayToast("Failed and try again[2124]");
                    return false;
                } else if (!changeNvitemValue(2138, 1)) {
                    DisplayToast("Failed and try again[2138]");
                    return false;
                }
                break;
            case 1:
                if (!changeNvitemValue(2118, 0)) {
                    DisplayToast("Failed and try again[2118]");
                    return false;
                } else if (!changeNvitemValue(2124, 0)) {
                    DisplayToast("Failed and try again[2124]");
                    return false;
                } else if (!changeNvitemValue(2138, 2)) {
                    DisplayToast("Failed and try again[2138]");
                    return false;
                }
                break;
            case 2:
                if (!changeNvitemValue(2118, 1)) {
                    DisplayToast("Failed and try again[2118]");
                    return false;
                } else if (!changeNvitemValue(2124, 1)) {
                    DisplayToast("Failed and try again[2124]");
                    return false;
                } else if (!changeNvitemValue(2138, 2)) {
                    DisplayToast("Failed and try again[2138]");
                    return false;
                }
                break;
            default:
                return false;
        }
        if (!sendDataToModem(252, 1)) {
            DisplayToast("Please restart the phone!");
            Log.e("networkmodeselect", "AT+RESET response error!");
        }
        return true;
    }

    private boolean changeNvitemValue(int nvitem_id, int value) {
        if (!sendDataToModem(250, nvitem_id)) {
            Log.e("networkmodeselect", "AT+SNVM response error!");
            return false;
        } else if (sendDataToModem(251, value)) {
            return true;
        } else {
            Log.e("networkmodeselect", "value x1a response error!");
            return false;
        }
    }

    private boolean sendDataToModem(int cmd, int value) {
        ByteArrayOutputStream ob = new ByteArrayOutputStream();
        DataOutputStream obs = new DataOutputStream(ob);
        Log.w("networkmodeselect", "sendDataToModem socket_id:" + this.mSocketID);
        String atLine = String.format("%d,%d,%d", new Object[]{Integer.valueOf(cmd), Integer.valueOf(1), Integer.valueOf(value)});
        Log.d("networkmodeselect", "atLine:" + atLine);
        try {
            obs.writeBytes(atLine);
            this.mEf.engwrite(this.mSocketID, ob.toByteArray(), ob.toByteArray().length);
            byte[] inputBytes = new byte[32];
            String response = new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 32));
            Log.d("networkmodeselect", "AT response:" + response);
            if (response.equals("OK")) {
                return true;
            }
            Log.e("networkmodeselect", "sendDataToModem response error!");
            return false;
        } catch (IOException e) {
            Log.e("networkmodeselect", "writeBytes() error!");
            return false;
        }
    }

    private void DisplayToast(String str) {
        Toast mToast = Toast.makeText(this, str, 0);
        mToast.setGravity(48, 0, 100);
        mToast.show();
    }

    protected void onDestroy() {
        if (this.mEf != null && this.mSocketID >= 0) {
            this.mEf.engclose(this.mSocketID);
        }
        super.onDestroy();
        Log.d("networkmodeselect", "onDestroy");
    }
}
