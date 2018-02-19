package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

public class LogSettingSlogUIModemPage extends Activity {
    private CheckBox chkBlueTooth;
    private CheckBox chkMisc;
    private CheckBox chkModem;
    private CheckBox chkTcp;

    protected class ClkListenner implements OnClickListener {
        protected ClkListenner() {
        }

        public void onClick(View onClickView) {
            switch (onClickView.getId()) {
                case R.id.chk_modem_branch:
                    SlogAction.SetState("stream\tmodem\t", LogSettingSlogUIModemPage.this.chkModem.isChecked(), false);
                    new Thread() {
                        public void run() {
                            SlogAction.sendATCommand(4, LogSettingSlogUIModemPage.this.chkModem.isChecked());
                        }
                    }.start();
                    return;
                case R.id.chk_modem_bluetooth:
                    SlogAction.SetState("stream\tbt\t", LogSettingSlogUIModemPage.this.chkBlueTooth.isChecked(), false);
                    return;
                case R.id.chk_modem_tcp:
                    SlogAction.SetState("stream\ttcp\t", LogSettingSlogUIModemPage.this.chkTcp.isChecked(), false);
                    new Thread() {
                        public void run() {
                            SlogAction.sendATCommand(58, LogSettingSlogUIModemPage.this.chkTcp.isChecked());
                        }
                    }.start();
                    return;
                case R.id.chk_modem_misc:
                    SlogAction.SetState("misc\tmisc\t", LogSettingSlogUIModemPage.this.chkMisc.isChecked(), false);
                    return;
                default:
                    return;
            }
        }
    }

    protected class VersionListener implements OnLongClickListener {
        protected VersionListener() {
        }

        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.chk_modem_misc:
                    Toast.makeText(LogSettingSlogUIModemPage.this, R.string.slog_version_info, 1).show();
                    break;
            }
            return false;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modem);
        this.chkModem = (CheckBox) findViewById(R.id.chk_modem_branch);
        this.chkBlueTooth = (CheckBox) findViewById(R.id.chk_modem_bluetooth);
        this.chkTcp = (CheckBox) findViewById(R.id.chk_modem_tcp);
        this.chkMisc = (CheckBox) findViewById(R.id.chk_modem_misc);
        ClkListenner clickListen = new ClkListenner();
        VersionListener verListen = new VersionListener();
        this.chkModem.setOnClickListener(clickListen);
        this.chkBlueTooth.setOnClickListener(clickListen);
        this.chkTcp.setOnClickListener(clickListen);
        this.chkMisc.setOnClickListener(clickListen);
        this.chkMisc.setOnLongClickListener(verListen);
    }

    protected void onResume() {
        super.onResume();
        syncState();
    }

    public void syncState() {
        boolean tempHost;
        boolean z;
        boolean z2 = true;
        boolean tempHostOn = SlogAction.GetState("\n", true).equals("enable");
        boolean tempHostLowPower = SlogAction.GetState("\n", true).equals("low_power");
        if (tempHostOn || tempHostLowPower) {
            tempHost = true;
        } else {
            tempHost = false;
        }
        SlogAction.SetCheckBoxBranchState(this.chkModem, tempHost, SlogAction.GetState("stream\tmodem\t"));
        CheckBox checkBox = this.chkBlueTooth;
        if (tempHost && SlogAction.GetState("logpath\t")) {
            z = true;
        } else {
            z = false;
        }
        SlogAction.SetCheckBoxBranchState(checkBox, z, SlogAction.GetState("stream\tbt\t"));
        CheckBox checkBox2 = this.chkTcp;
        if (!(tempHost && SlogAction.GetState("logpath\t"))) {
            z2 = false;
        }
        SlogAction.SetCheckBoxBranchState(checkBox2, z2, SlogAction.GetState("stream\ttcp\t"));
        SlogAction.SetCheckBoxBranchState(this.chkMisc, tempHost, SlogAction.GetState("misc\tmisc\t"));
    }
}
