package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class LogSettingSlogUIAndroidPage extends Activity {
    private CheckBox chkGeneral;
    private CheckBox chkKernel;
    private CheckBox chkMain;
    private CheckBox chkRadio;
    private CheckBox chkSystem;

    class ClkListenner implements OnClickListener {
        ClkListenner() {
        }

        public void onClick(View onClickView) {
            switch (onClickView.getId()) {
                case R.id.chk_android_general:
                    SlogAction.SetState(101, LogSettingSlogUIAndroidPage.this.chkGeneral.isChecked());
                    LogSettingSlogUIAndroidPage.this.syncState();
                    return;
                case R.id.chk_android_kernel:
                    SlogAction.SetState("stream\tkernel\t", LogSettingSlogUIAndroidPage.this.chkKernel.isChecked(), false);
                    return;
                case R.id.chk_android_system:
                    SlogAction.SetState("stream\tsystem\t", LogSettingSlogUIAndroidPage.this.chkSystem.isChecked(), false);
                    return;
                case R.id.chk_android_radio:
                    SlogAction.SetState("stream\tradio\t", LogSettingSlogUIAndroidPage.this.chkRadio.isChecked(), false);
                    return;
                case R.id.chk_android_main:
                    SlogAction.SetState("stream\tmain\t", LogSettingSlogUIAndroidPage.this.chkMain.isChecked(), false);
                    return;
                default:
                    Log.w("Slog->AndroidPage", "Wrong id given.");
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android);
        this.chkGeneral = (CheckBox) findViewById(R.id.chk_android_general);
        this.chkSystem = (CheckBox) findViewById(R.id.chk_android_system);
        this.chkRadio = (CheckBox) findViewById(R.id.chk_android_radio);
        this.chkKernel = (CheckBox) findViewById(R.id.chk_android_kernel);
        this.chkMain = (CheckBox) findViewById(R.id.chk_android_main);
        ClkListenner chklisten = new ClkListenner();
        this.chkGeneral.setOnClickListener(chklisten);
        this.chkSystem.setOnClickListener(chklisten);
        this.chkRadio.setOnClickListener(chklisten);
        this.chkKernel.setOnClickListener(chklisten);
        this.chkMain.setOnClickListener(chklisten);
    }

    protected void onResume() {
        super.onResume();
        syncState();
    }

    public void syncState() {
        boolean tempHostGen = true;
        boolean tempHost = SlogAction.GetState(101);
        this.chkGeneral.setEnabled(true);
        boolean tempHostOn = SlogAction.GetState("\n", true).equals("enable");
        boolean tempHostLowPower = SlogAction.GetState("\n", true).equals("low_power");
        if (!(tempHostOn || tempHostLowPower)) {
            tempHostGen = false;
        }
        if (!tempHostGen) {
            this.chkGeneral.setEnabled(false);
            tempHost = false;
        }
        this.chkGeneral.setChecked(tempHost);
        SlogAction.SetCheckBoxBranchState(this.chkSystem, tempHost, SlogAction.GetState("stream\tsystem\t"));
        SlogAction.SetCheckBoxBranchState(this.chkRadio, tempHost, SlogAction.GetState("stream\tradio\t"));
        SlogAction.SetCheckBoxBranchState(this.chkKernel, tempHost, SlogAction.GetState("stream\tkernel\t"));
        SlogAction.SetCheckBoxBranchState(this.chkMain, tempHost, SlogAction.GetState("stream\tmain\t"));
    }
}
