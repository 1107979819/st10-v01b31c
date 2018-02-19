package com.spreadtrum.android.eng;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WifiEUT extends Activity implements OnClickListener, OnFocusChangeListener, OnItemSelectedListener {
    private Spinner amplitude;
    private Spinner band;
    private Spinner channel;
    private PtestCw cw;
    private Button cwBtn;
    private EditText destMacAddr;
    private AlertDialog dialog;
    private CheckBox enablelbCsTestMode;
    private EngWifieut engWifieut;
    private Map<String, String> errorMap;
    private CheckBox filteringEnable;
    private EditText frequency;
    private EditText frequencyOffset;
    private EditText frequencyRx;
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (WifiEUT.this.progressDialog != null) {
                WifiEUT.this.progressDialog.dismiss();
            }
            int what = msg.what;
            int arg = msg.arg1;
            String message = arg == 0 ? "Success" : "Fail";
            switch (what) {
                case 1:
                    WifiEUT.this.showDialog(message, WifiEUT.this.title);
                    return;
                case 2:
                    WifiEUT.this.showDialog(message, WifiEUT.this.title);
                    return;
                case 3:
                    WifiEUT.this.showDialog(message, WifiEUT.this.title);
                    return;
                case 4:
                    if (arg != 0) {
                        WifiEUT.this.showDialog("init fail", "init");
                        return;
                    }
                    WifiEUT.this.enableView();
                    WifiEUT.this.swicthBtn.setText(R.string.wifi_eut_stop);
                    WifiEUT.this.isTesting = true;
                    return;
                case 5:
                    if (arg == 0) {
                        WifiEUT.this.disableView();
                        WifiEUT.this.swicthBtn.setText(R.string.wifi_eut_start);
                        WifiEUT.this.isTesting = false;
                        return;
                    }
                    return;
                case 6:
                    WifiEUT.this.isTesting = false;
                    WifiEUT.this.finish();
                    return;
                default:
                    return;
            }
        }
    };
    private EditText interval;
    private boolean isTesting;
    private EditText length;
    private boolean mHaveFinish = true;
    private String notnum = " is not a num";
    private Spinner powerLevel;
    private Spinner preamble;
    private ProgressDialog progressDialog;
    private Spinner rate;
    private PtestRx rx;
    private Button rxBtn;
    private EditText sFactor;
    private Button swicthBtn;
    private String title = "Test Result";
    private PtestTx tx;
    private Button txBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_eut);
        this.mHaveFinish = false;
        getWindow().setSoftInputMode(32);
        this.errorMap = new HashMap();
        initui();
        initEvent();
        disableView();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SystemProperties.set("ctl.start", "enghardwaretest");
    }

    private void initui() {
        this.band = (Spinner) findViewById(R.id.wifi_eut_band);
        this.channel = (Spinner) findViewById(R.id.wifi_eut_channel);
        this.sFactor = (EditText) findViewById(R.id.wifi_eut_sfactor);
        this.frequency = (EditText) findViewById(R.id.wifi_eut_frequency);
        this.frequencyOffset = (EditText) findViewById(R.id.wifi_eut_frequencyOffset);
        this.amplitude = (Spinner) findViewById(R.id.wifi_eut_amplitude);
        this.rate = (Spinner) findViewById(R.id.wifi_eut_rate);
        this.powerLevel = (Spinner) findViewById(R.id.wifi_eut_powerLevel);
        this.length = (EditText) findViewById(R.id.wifi_eut_length);
        this.enablelbCsTestMode = (CheckBox) findViewById(R.id.wifi_eut_enablelbCsTestMode);
        this.interval = (EditText) findViewById(R.id.wifi_eut_interval);
        this.destMacAddr = (EditText) findViewById(R.id.wifi_eut_destMacAddr);
        this.preamble = (Spinner) findViewById(R.id.wifi_eut_preamble);
        this.filteringEnable = (CheckBox) findViewById(R.id.wifi_eut_filteringEnable);
        this.frequencyRx = (EditText) findViewById(R.id.wifi_eut_frequency_rx);
        this.cwBtn = (Button) findViewById(R.id.wifi_eut_cw);
        this.txBtn = (Button) findViewById(R.id.wifi_eut_tx);
        this.rxBtn = (Button) findViewById(R.id.wifi_eut_rx);
        this.swicthBtn = (Button) findViewById(R.id.wifi_eut_switch);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, 17367048, getResources().getStringArray(R.array.band_arr));
        adapter.setDropDownViewResource(17367049);
        this.band.setAdapter(adapter);
        this.band.setPrompt("band");
        ArrayAdapter<String> amplitudeAdapter = new ArrayAdapter(this, 17367048, getResources().getStringArray(R.array.amplitude_arr));
        amplitudeAdapter.setDropDownViewResource(17367049);
        this.amplitude.setAdapter(amplitudeAdapter);
        this.amplitude.setPrompt("amplitude");
        ArrayAdapter<String> powerLevelAdapter = new ArrayAdapter(this, 17367048, getResources().getStringArray(R.array.power_level_arr));
        powerLevelAdapter.setDropDownViewResource(17367049);
        this.powerLevel.setAdapter(powerLevelAdapter);
        this.powerLevel.setPrompt("power level");
        ArrayAdapter<String> preambleAdapter = new ArrayAdapter(this, 17367048, getResources().getStringArray(R.array.preamble_arr));
        preambleAdapter.setDropDownViewResource(17367049);
        this.preamble.setAdapter(preambleAdapter);
        this.preamble.setPrompt("preamble");
        ArrayAdapter<String> rateAdapter = new ArrayAdapter(this, 17367048, getResources().getStringArray(R.array.rate_str_arr));
        rateAdapter.setDropDownViewResource(17367049);
        this.rate.setAdapter(rateAdapter);
        this.rate.setPrompt("rate");
        ArrayAdapter<String> channelAdapter = new ArrayAdapter(this, 17367048, getResources().getStringArray(R.array.channel_arr));
        channelAdapter.setDropDownViewResource(17367049);
        this.channel.setAdapter(channelAdapter);
        this.channel.setPrompt("channnel");
        this.sFactor.setEnabled(false);
        this.swicthBtn.setOnClickListener(this);
        this.channel.setSelection(6);
        this.rate.setSelection(11);
        this.powerLevel.setSelection(6);
    }

    private void initEvent() {
        this.cwBtn.setOnClickListener(this);
        this.txBtn.setOnClickListener(this);
        this.rxBtn.setOnClickListener(this);
        this.band.setOnItemSelectedListener(this);
        this.channel.setOnFocusChangeListener(this);
        this.sFactor.setOnFocusChangeListener(this);
        this.frequency.setOnFocusChangeListener(this);
        this.frequencyOffset.setOnFocusChangeListener(this);
        this.length.setOnFocusChangeListener(this);
        this.interval.setOnFocusChangeListener(this);
        this.frequencyRx.setOnFocusChangeListener(this);
    }

    public void onClick(View v) {
        int i = 1;
        if (v == this.cwBtn) {
            if (haveNoErr()) {
                this.cw = new PtestCw();
                this.cw.band = this.band.getSelectedItemPosition() + 1;
                this.cw.channel = this.channel.getSelectedItemPosition() + 1;
                this.cw.sFactor = Utils.parseInt(this.sFactor.getText().toString().trim());
                this.cw.frequency = Utils.parseInt(this.frequency.getText().toString().trim());
                this.cw.frequencyOffset = Utils.parseInt(this.frequencyOffset.getText().toString().trim());
                this.cw.amplitude = this.amplitude.getSelectedItemPosition();
                this.progressDialog = ProgressDialog.show(this, "CW Test", "Testing...");
                new Thread(new Runnable() {
                    public void run() {
                        int re = WifiEUT.this.engWifieut.testCw(WifiEUT.this.cw);
                        Message msg = WifiEUT.this.handler.obtainMessage();
                        msg.what = 1;
                        msg.arg1 = re;
                        msg.sendToTarget();
                    }
                }).start();
            }
        } else if (v == this.txBtn) {
            int[] list = getResources().getIntArray(R.array.rate_int_arr);
            if (haveNoErr()) {
                this.tx = new PtestTx();
                this.tx.band = this.band.getSelectedItemPosition() + 1;
                this.tx.channel = this.channel.getSelectedItemPosition() + 1;
                this.tx.sFactor = Utils.parseInt(this.sFactor.getText().toString().trim());
                this.tx.rate = list[this.rate.getSelectedItemPosition()];
                this.tx.powerLevel = this.powerLevel.getSelectedItemPosition() + 1;
                this.tx.length = Utils.parseInt(this.length.getText().toString().trim());
                PtestTx ptestTx = this.tx;
                if (!this.enablelbCsTestMode.isChecked()) {
                    i = 0;
                }
                ptestTx.enablelbCsTestMode = i;
                this.tx.interval = Utils.parseInt(this.interval.getText().toString().trim());
                this.tx.destMacAddr = this.destMacAddr.getText().toString().trim().replace("-", "").replace(" ", "").replace(":", "");
                this.tx.preamble = this.preamble.getSelectedItemPosition();
                this.progressDialog = ProgressDialog.show(this, "TX Test", "Testing...");
                new Thread(new Runnable() {
                    public void run() {
                        int re = WifiEUT.this.engWifieut.testTx(WifiEUT.this.tx);
                        Message msg = WifiEUT.this.handler.obtainMessage();
                        msg.what = 2;
                        msg.arg1 = re;
                        msg.sendToTarget();
                    }
                }).start();
            }
        } else if (v == this.rxBtn) {
            if (haveNoErr()) {
                this.rx = new PtestRx();
                this.rx.band = this.band.getSelectedItemPosition() + 1;
                this.rx.channel = this.channel.getSelectedItemPosition() + 1;
                this.rx.sFactor = Utils.parseInt(this.sFactor.getText().toString().trim());
                this.rx.frequency = Utils.parseInt(this.frequencyRx.getText().toString().trim());
                PtestRx ptestRx = this.rx;
                if (!this.filteringEnable.isChecked()) {
                    i = 0;
                }
                ptestRx.filteringEnable = i;
                this.progressDialog = ProgressDialog.show(this, "RX Test", "Testing...");
                new Thread(new Runnable() {
                    public void run() {
                        int re = WifiEUT.this.engWifieut.testRx(WifiEUT.this.rx);
                        Message msg = WifiEUT.this.handler.obtainMessage();
                        msg.what = 3;
                        msg.arg1 = re;
                        msg.sendToTarget();
                    }
                }).start();
            }
        } else if (v != this.swicthBtn) {
        } else {
            if (this.isTesting) {
                deinitTest();
                return;
            }
            if (this.engWifieut == null) {
                this.engWifieut = new EngWifieut();
            }
            initTest();
        }
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 1) {
            this.sFactor.setEnabled(true);
        } else {
            this.sFactor.setEnabled(false);
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
        this.sFactor.setEnabled(false);
    }

    protected void onStart() {
        this.mHaveFinish = false;
        super.onStart();
    }

    protected void onStop() {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
            this.progressDialog = null;
        }
        if (this.dialog != null) {
            this.dialog.dismiss();
            this.dialog = null;
        }
        this.mHaveFinish = true;
        super.onStop();
    }

    protected void onDestroy() {
        deinitTestNoUi();
        SystemProperties.set("ctl.stop", "enghardwaretest");
        super.onDestroy();
    }

    private boolean haveNoErr() {
        Set<String> set = this.errorMap.keySet();
        if (set.isEmpty()) {
            return true;
        }
        StringBuffer err = new StringBuffer();
        for (String s : set) {
            err.append(((String) this.errorMap.get(s)) + "\n");
        }
        showMessage(err.toString());
        return false;
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, 1).show();
    }

    private void showDialog(String msg, String title) {
        if (this.mHaveFinish) {
            Log.d("WifiEUT", "activity have finished !");
            return;
        }
        this.dialog = new Builder(this).setTitle(title).setMessage(msg).setNegativeButton("Sure", null).create();
        this.dialog.show();
    }

    private void betweenNum(int a, int b, int v, String key, int resid) {
        if (v < a || v > b) {
            this.errorMap.put(key, getString(resid));
            Toast.makeText(this, getString(resid), 1).show();
        } else if (this.errorMap.containsKey(key)) {
            this.errorMap.remove(key);
        }
    }

    private boolean checkIsInt(String str, String key) {
        if ("".equals(str)) {
            if (!this.errorMap.containsKey(key)) {
                return false;
            }
            this.errorMap.remove(key);
            return false;
        } else if (Utils.isInt(str)) {
            return true;
        } else {
            Toast.makeText(this, key + this.notnum, 1).show();
            this.errorMap.put(key, key + this.notnum);
            return false;
        }
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String str = ((EditText) v).getText().toString().trim();
            int n;
            if (v == this.channel) {
                if (checkIsInt(str, "channel")) {
                    n = Integer.parseInt(str);
                    if (this.band.getSelectedItemPosition() == 0) {
                        betweenNum(1, 14, n, "channel", R.string.wifi_eut_band_err1);
                    } else if (this.band.getSelectedItemPosition() == 1) {
                        betweenNum(1, 200, n, "channel", R.string.wifi_eut_band_err2);
                    }
                }
            } else if (v == this.sFactor) {
                if (checkIsInt(str, "sFactor")) {
                    n = Integer.parseInt(str);
                    if (n % 500 == 0) {
                        betweenNum(8000, 10000, n, "sFactor", R.string.wifi_eut_sFactor_err);
                        return;
                    }
                    this.errorMap.put("sFactor", getString(R.string.wifi_eut_sFactor_err));
                    Toast.makeText(this, getString(R.string.wifi_eut_sFactor_err), 1).show();
                }
            } else if (v == this.frequency) {
                if (checkIsInt(str, "frequency")) {
                    n = Integer.parseInt(str);
                    if (n != 0) {
                        betweenNum(1790, 6000, n, "frequency", R.string.wifi_eut_frequency_err);
                    }
                }
            } else if (v == this.frequencyOffset) {
                if (checkIsInt(str, "frequencyOffset")) {
                    betweenNum(-20000, 20000, Integer.parseInt(str), "frequencyOffset", R.string.wifi_eut_frequencyOffset_err);
                }
            } else if (v == this.length) {
                if (checkIsInt(str, "length")) {
                    betweenNum(0, 2304, Integer.parseInt(str), "length", R.string.wifi_eut_length_err);
                }
            } else if (v == this.interval) {
                checkIsInt(str, "interval");
            } else if (v == this.frequencyRx && checkIsInt(str, "frequencyRx")) {
                n = Integer.parseInt(str);
                if (n != 0) {
                    betweenNum(1790, 6000, n, "frequency", R.string.wifi_eut_frequency_err);
                }
            }
        }
    }

    private void initTest() {
        this.progressDialog = ProgressDialog.show(this, "init", "please wait, initing...");
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int re = WifiEUT.this.engWifieut.testInit();
                Message msg = WifiEUT.this.handler.obtainMessage();
                msg.what = 4;
                msg.arg1 = re;
                msg.sendToTarget();
            }
        }).start();
    }

    private void deinitTest() {
        this.progressDialog = ProgressDialog.show(this, "stop", "please wait, stoping...");
        if (this.isTesting) {
            new Thread(new Runnable() {
                public void run() {
                    int re = WifiEUT.this.engWifieut.testDeinit();
                    Message msg = WifiEUT.this.handler.obtainMessage();
                    msg.what = 5;
                    msg.arg1 = re;
                    msg.sendToTarget();
                }
            }).start();
        }
    }

    private void deinitTestNoUi() {
        if (this.engWifieut != null) {
            this.engWifieut.testSetValue(0);
        }
        if (this.isTesting) {
            new Thread(new Runnable() {
                public void run() {
                    WifiEUT.this.engWifieut.testDeinit();
                }
            }).start();
        }
    }

    private void enableView() {
        this.band.setEnabled(true);
        this.channel.setEnabled(true);
        this.sFactor.setEnabled(true);
        this.frequency.setEnabled(true);
        this.frequencyOffset.setEnabled(true);
        this.amplitude.setEnabled(true);
        this.rate.setEnabled(true);
        this.powerLevel.setEnabled(true);
        this.length.setEnabled(true);
        this.enablelbCsTestMode.setEnabled(true);
        this.interval.setEnabled(true);
        this.destMacAddr.setEnabled(true);
        this.preamble.setEnabled(true);
        this.filteringEnable.setEnabled(true);
        this.frequencyRx.setEnabled(true);
        this.cwBtn.setEnabled(true);
        this.txBtn.setEnabled(true);
        this.rxBtn.setEnabled(true);
    }

    private void disableView() {
        this.band.setEnabled(false);
        this.channel.setEnabled(false);
        this.sFactor.setEnabled(false);
        this.frequency.setEnabled(false);
        this.frequencyOffset.setEnabled(false);
        this.amplitude.setEnabled(false);
        this.rate.setEnabled(false);
        this.powerLevel.setEnabled(false);
        this.length.setEnabled(false);
        this.enablelbCsTestMode.setEnabled(false);
        this.interval.setEnabled(false);
        this.destMacAddr.setEnabled(false);
        this.preamble.setEnabled(false);
        this.filteringEnable.setEnabled(false);
        this.frequencyRx.setEnabled(false);
        this.cwBtn.setEnabled(false);
        this.txBtn.setEnabled(false);
        this.rxBtn.setEnabled(false);
    }

    public void onBackPressed() {
        if (!this.isTesting) {
            super.onBackPressed();
        } else if (this.engWifieut != null) {
            this.progressDialog = ProgressDialog.show(this, "stop", "please don't force colse, stoping...");
            new Thread(new Runnable() {
                public void run() {
                    int re = WifiEUT.this.engWifieut.testDeinit();
                    Message msg = WifiEUT.this.handler.obtainMessage();
                    msg.what = 6;
                    msg.arg1 = re;
                    msg.sendToTarget();
                }
            }).start();
        }
    }
}
