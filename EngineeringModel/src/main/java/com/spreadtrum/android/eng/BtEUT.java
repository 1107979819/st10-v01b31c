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
import android.widget.Button;

public class BtEUT extends Activity implements OnClickListener {
    private AlertDialog dialog;
    private EngWifieut engWifieut;
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (BtEUT.this.progressDialog != null) {
                BtEUT.this.progressDialog.dismiss();
            }
            int what = msg.what;
            int arg = msg.arg1;
            String message;
            if (arg == 0) {
                message = "Success";
            } else {
                message = "Fail";
            }
            switch (what) {
                case 1:
                    if (arg != 0) {
                        BtEUT.this.showDialog("init fail", "init");
                        return;
                    }
                    BtEUT.this.swicthBtn.setText(R.string.wifi_eut_stop);
                    BtEUT.this.isTesting = true;
                    return;
                case 2:
                    if (arg != 0) {
                        BtEUT.this.showDialog("stop fail", "stop");
                        return;
                    }
                    BtEUT.this.swicthBtn.setText(R.string.wifi_eut_start);
                    BtEUT.this.isTesting = false;
                    return;
                case 3:
                    BtEUT.this.isTesting = false;
                    BtEUT.this.finish();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean isTesting;
    private boolean mHaveFinish = true;
    private ProgressDialog progressDialog;
    private Button swicthBtn;
    private String title = "Test Result";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_eut);
        this.mHaveFinish = false;
        getWindow().setSoftInputMode(32);
        initui();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SystemProperties.set("ctl.start", "enghardwaretest");
    }

    private void initui() {
        setTitle(R.string.btut_test);
        this.swicthBtn = (Button) findViewById(R.id.bt_eut_switch);
        this.swicthBtn.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v != this.swicthBtn) {
            return;
        }
        if (this.isTesting) {
            stopTest();
            return;
        }
        if (this.engWifieut == null) {
            this.engWifieut = new EngWifieut();
        }
        startTest();
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
        super.onDestroy();
        SystemProperties.set("ctl.stop", "enghardwaretest");
    }

    private void showDialog(String msg, String title) {
        if (this.mHaveFinish) {
            Log.d("BtEUT", "activity have finished !");
            return;
        }
        this.dialog = new Builder(this).setTitle(title).setMessage(msg).setNegativeButton("Sure", null).create();
        this.dialog.show();
    }

    private void startTest() {
        this.progressDialog = ProgressDialog.show(this, "init", "please wait, initing...");
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int re = BtEUT.this.engWifieut.testBtStart();
                Message msg = BtEUT.this.handler.obtainMessage();
                msg.what = 1;
                msg.arg1 = re;
                msg.sendToTarget();
            }
        }).start();
    }

    private void stopTest() {
        this.progressDialog = ProgressDialog.show(this, "stop", "please wait, stoping...");
        if (this.isTesting) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int re = BtEUT.this.engWifieut.testBtStop();
                    Message msg = BtEUT.this.handler.obtainMessage();
                    msg.what = 2;
                    msg.arg1 = re;
                    msg.sendToTarget();
                }
            }).start();
        }
    }

    private void deinitTestNoUi() {
        if (this.isTesting) {
            new Thread(new Runnable() {
                public void run() {
                    BtEUT.this.engWifieut.testBtStop();
                }
            }).start();
        }
    }

    public void onBackPressed() {
        if (!this.isTesting) {
            super.onBackPressed();
        } else if (this.engWifieut != null) {
            this.progressDialog = ProgressDialog.show(this, "stop", "please don't force colse, stoping...");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int re = BtEUT.this.engWifieut.testBtStop();
                    Message msg = BtEUT.this.handler.obtainMessage();
                    msg.what = 3;
                    msg.arg1 = re;
                    msg.sendToTarget();
                }
            }).start();
        }
    }
}
