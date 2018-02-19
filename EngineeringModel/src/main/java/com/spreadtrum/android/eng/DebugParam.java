package com.spreadtrum.android.eng;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class DebugParam extends PreferenceActivity {
    private String mATline;
    private int mAssertMdoe;
    private boolean[] mBandCheckList = new boolean[]{false, false, false, false};
    private Preference mBandSelectPreference;
    private int[] mBandShowList = new int[]{1, 2, 4, 8};
    private engfetch mEf;
    private boolean mHaveFinish = true;
    private int mSocketID;
    private Handler mThread;
    private Handler mUiThread;
    private ByteArrayOutputStream outputBuffer;
    private DataOutputStream outputBufferStream;

    class AsyncnonizeHandler extends Handler {
        public AsyncnonizeHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Builder builder;
            int i;
            switch (msg.what) {
                case 0:
                    builder = new Builder(DebugParam.this);
                    int value = DebugParam.this.getSelectedBand();
                    if (value != -1) {
                        int param = BandSelectDecoder.getInstance().getBandsFromCmdParam(value);
                        for (i = 0; i < DebugParam.this.mBandShowList.length; i++) {
                            if ((DebugParam.this.mBandShowList[i] & param) != 0) {
                                DebugParam.this.mBandCheckList[i] = true;
                            }
                        }
                        builder.setMultiChoiceItems(R.array.band_select_choices, DebugParam.this.mBandCheckList, new OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                DebugParam.this.mBandCheckList[which] = isChecked;
                            }
                        });
                        builder.setPositiveButton("OK ", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DebugParam.this.mThread.obtainMessage(1).sendToTarget();
                            }
                        });
                        DebugParam.this.showDialog(builder, String.valueOf(msg.obj));
                        break;
                    }
                    Toast.makeText(DebugParam.this, "Error", 0).show();
                    break;
                case 1:
                    int selectBands = 0;
                    for (i = 0; i < DebugParam.this.mBandCheckList.length; i++) {
                        if (DebugParam.this.mBandCheckList[i]) {
                            selectBands |= DebugParam.this.mBandShowList[i];
                        }
                    }
                    int cmdFromBands = BandSelectDecoder.getInstance().getCmdParamFromBands(selectBands);
                    if (cmdFromBands != -1) {
                        final int select = selectBands;
                        if (DebugParam.this.setSelectedBand(cmdFromBands)) {
                            DebugParam.this.mUiThread.post(new Runnable() {
                                public void run() {
                                    DebugParam.this.mBandSelectPreference.setSummary(DebugParam.this.getBandSelectSummary(BandSelectDecoder.getInstance().getCmdParamFromBands(select)));
                                }
                            });
                            break;
                        }
                    }
                    Toast.makeText(DebugParam.this, R.string.set_bands_error, 0).show();
                    Log.e("DebugParam", "Error, cmdFromBands = -1");
                    return;
                    break;
                case 2:
                    builder = new Builder(DebugParam.this);
                    int mode = DebugParam.this.getAssertMode();
                    if (mode != -1) {
                        DebugParam.this.mAssertMdoe = mode;
                        builder.setSingleChoiceItems(R.array.assert_mode, DebugParam.this.mAssertMdoe, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DebugParam.this.mAssertMdoe = which;
                            }
                        });
                        builder.setPositiveButton("OK ", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Message msg = DebugParam.this.mThread.obtainMessage(3);
                                msg.arg1 = DebugParam.this.mAssertMdoe;
                                msg.sendToTarget();
                            }
                        });
                        DebugParam.this.showDialog(builder, String.valueOf(msg.obj));
                        break;
                    }
                    Toast.makeText(DebugParam.this, "Error", 0).show();
                    break;
                case 4:
                    DebugParam.this.setManualAssert();
                    break;
                case 5:
                    final String summary = DebugParam.this.getBandSelectSummary(DebugParam.this.getSelectedBand());
                    DebugParam.this.mUiThread.post(new Runnable() {
                        public void run() {
                            DebugParam.this.mBandSelectPreference.setSummary(summary);
                        }
                    });
                    break;
            }
            super.handleMessage(msg);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mEf = new engfetch();
        this.mSocketID = this.mEf.engopen();
        addPreferencesFromResource(R.layout.debugparam);
        this.mBandSelectPreference = findPreference("key_bandselect");
        this.mUiThread = new Handler();
        HandlerThread t = new HandlerThread("debugparam");
        t.start();
        this.mThread = new AsyncnonizeHandler(t.getLooper());
        this.mThread.obtainMessage(6).sendToTarget();
        this.mThread.obtainMessage(5).sendToTarget();
    }

    protected void onStart() {
        this.mHaveFinish = false;
        super.onStart();
    }

    protected void onStop() {
        this.mHaveFinish = true;
        super.onStop();
    }

    private void showDialog(final Builder builder, final String title) {
        this.mUiThread.post(new Runnable() {
            public void run() {
                if (!DebugParam.this.mHaveFinish) {
                    builder.setTitle(title);
                    builder.create().show();
                }
            }
        });
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        Message obtainMessage;
        if ("key_bandselect".equals(key)) {
            obtainMessage = this.mThread.obtainMessage(0);
            obtainMessage.obj = preference.getTitle();
            obtainMessage.sendToTarget();
        } else if ("key_assertmode".equals(key)) {
            obtainMessage = this.mThread.obtainMessage(2);
            obtainMessage.obj = preference.getTitle();
            obtainMessage.sendToTarget();
        } else if ("key_manualassert".equals(key)) {
            obtainMessage = this.mThread.obtainMessage(4);
            obtainMessage.obj = preference.getTitle();
            obtainMessage.sendToTarget();
        } else if ("key_forbidplmn".equals(key)) {
            startActivity(new Intent(this, TextInfo.class).putExtra("text_info", 1));
        } else if ("key_plmnselect".equals(key)) {
            startActivity(new Intent(this, TextInfo.class).putExtra("text_info", 2));
        }
        return true;
    }

    private boolean setSelectedBand(int bands) {
        this.outputBuffer = new ByteArrayOutputStream();
        this.outputBufferStream = new DataOutputStream(this.outputBuffer);
        this.mATline = 2 + "," + 1 + "," + bands;
        try {
            this.outputBufferStream.writeBytes(this.mATline);
            this.mEf.engwrite(this.mSocketID, this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
            byte[] inputBytes = new byte[128];
            if (new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 128), Charset.defaultCharset()).indexOf("ERROR") == -1) {
                return true;
            }
            Toast.makeText(this, R.string.set_bands_error, 1).show();
            return false;
        } catch (IOException e) {
            Log.e("DebugParam", "writeBytes() error!");
            return true;
        }
    }

    private int getSelectedBand() {
        this.outputBuffer = new ByteArrayOutputStream();
        this.outputBufferStream = new DataOutputStream(this.outputBuffer);
        this.mATline = 3 + "," + 0;
        try {
            this.outputBufferStream.writeBytes(this.mATline);
            this.mEf.engwrite(this.mSocketID, this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
            byte[] inputBytes = new byte[128];
            String mATResponse = new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 128), Charset.defaultCharset());
            Log.d("DebugParam", "getSelectedBand result : " + mATResponse);
            int value = -1;
            try {
                return Integer.parseInt(mATResponse);
            } catch (Exception e) {
                Log.e("DebugParam", "Format String " + mATResponse + " to Integer Error!");
                return value;
            }
        } catch (IOException e2) {
            Log.e("DebugParam", "writeBytes() error!");
            return -1;
        }
    }

    private int getAssertMode() {
        this.outputBuffer = new ByteArrayOutputStream();
        this.outputBufferStream = new DataOutputStream(this.outputBuffer);
        this.mATline = 108 + "," + 0;
        try {
            this.outputBufferStream.writeBytes(this.mATline);
            int size = this.mEf.engwrite(this.mSocketID, this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
            byte[] inputBytes = new byte[128];
            String mATResponse = new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 128), Charset.defaultCharset());
            Log.d("DebugParam", "getAssertMode result : " + mATResponse);
            if (mATResponse.indexOf("+SDRMOD: 1") != -1) {
                return 1;
            }
            if (mATResponse.indexOf("+SDRMOD: 0") == -1) {
                return -1;
            }
            return 0;
        } catch (IOException e) {
            Log.e("DebugParam", "writeBytes() error!");
            return -1;
        }
    }

    private void setManualAssert() {
        this.outputBuffer = new ByteArrayOutputStream();
        this.outputBufferStream = new DataOutputStream(this.outputBuffer);
        this.mATline = 110 + "," + 0;
        try {
            this.outputBufferStream.writeBytes(this.mATline);
        } catch (IOException e) {
            Log.e("DebugParam", "writeBytes() error!");
        }
        int datasize = this.outputBuffer.toByteArray().length;
        int iRet = this.mEf.engwrite(this.mSocketID, this.outputBuffer.toByteArray(), datasize);
        Log.d("DebugParam", "setManualAssert engwrite size: " + iRet);
        if (datasize == iRet) {
            Toast.makeText(this, "Success", 0).show();
        } else {
            Toast.makeText(this, "Error", 0).show();
        }
    }

    private String getBandSelectSummary(int band) {
        String bandString = null;
        if (band == -1) {
            return "";
        }
        int param = BandSelectDecoder.getInstance().getBandsFromCmdParam(band);
        for (int i = 0; i < this.mBandShowList.length; i++) {
            if ((this.mBandShowList[i] & param) != 0) {
                if (bandString == null) {
                    bandString = getResources().getStringArray(R.array.band_select_choices)[i];
                } else {
                    bandString = bandString + "|" + getResources().getStringArray(R.array.band_select_choices)[i];
                }
            }
        }
        return bandString;
    }
}
