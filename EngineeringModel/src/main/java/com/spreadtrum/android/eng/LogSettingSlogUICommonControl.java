package com.spreadtrum.android.eng;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.app.IMediaContainerService;
import com.android.internal.app.IMediaContainerService.Stub;
import java.util.regex.Pattern;

public class LogSettingSlogUICommonControl extends Activity {
    private static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName("com.android.defcontainer", "com.android.defcontainer.DefaultContainerService");
    private Button btnClear;
    private Button btnDump;
    private CheckBox chkAlwaysRun;
    private CheckBox chkAndroid;
    private CheckBox chkClearLogAuto;
    private CheckBox chkModem;
    private CheckBox chkSnap;
    private Intent intentMedia;
    private Intent intentSnap;
    private Intent intentSvc;
    private OnClickListener mDeleteListener;
    private MainThreadHandler mHandler;
    private IMediaContainerService mMediaContainer;
    private final ServiceConnection mMediaContainerConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogSettingSlogUICommonControl.this.mMediaContainer = Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private CursorAdapter mModeListAdapter;
    private Builder mModeListDialogBuilder;
    private AlertDialog mNewModeDialog;
    private EditText mNewModeNameEditText;
    private OnClickListener mSettingListener;
    private OnClickListener mUpdateListener;
    private RadioButton rdoGeneralLowPower;
    private RadioButton rdoGeneralOff;
    private RadioButton rdoGeneralOn;
    private RadioButton rdoNAND;
    private RadioButton rdoSDCard;

    protected class ClkListenner implements View.OnClickListener {
        protected ClkListenner() {
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rdo_general_on:
                    SlogAction.SetState("\n", "enable", true);
                    LogSettingSlogUICommonControl.this.syncState();
                    return;
                case R.id.rdo_general_off:
                    SlogAction.SetState("\n", "disable", true);
                    if (LogSettingSlogUICommonControl.this.rdoGeneralOff.isChecked()) {
                        LogSettingSlogUICommonControl.this.requestDumpLog();
                    }
                    LogSettingSlogUICommonControl.this.syncState();
                    return;
                case R.id.rdo_general_lowpower:
                    SlogAction.SetState("\n", "low_power", true);
                    LogSettingSlogUICommonControl.this.syncState();
                    return;
                case R.id.chk_general_android_switch:
                    SlogAction.SetState(101, LogSettingSlogUICommonControl.this.chkAndroid.isChecked());
                    return;
                case R.id.chk_general_modem_switch:
                    SlogAction.SetState("stream\tmodem\t", LogSettingSlogUICommonControl.this.chkModem.isChecked(), false);
                    return;
                case R.id.rdo_general_sdcard:
                    if (SlogAction.IsHaveSDCard()) {
                        Toast.makeText(LogSettingSlogUICommonControl.this, LogSettingSlogUICommonControl.this.getText(R.string.toast_freespace_sdcard) + String.valueOf(SlogAction.GetFreeSpace(LogSettingSlogUICommonControl.this.mMediaContainer, "external")) + "MB", 0).show();
                    }
                    SlogAction.SetState("logpath\t", LogSettingSlogUICommonControl.this.rdoSDCard.isChecked(), true);
                    LogSettingSlogUICommonControl.this.btnDump.setEnabled(true);
                    return;
                case R.id.rdo_general_nand:
                    Toast.makeText(LogSettingSlogUICommonControl.this, LogSettingSlogUICommonControl.this.getText(R.string.toast_freespace_nand) + String.valueOf(SlogAction.GetFreeSpace(LogSettingSlogUICommonControl.this.mMediaContainer, "internal")) + "MB", 0).show();
                    SlogAction.SetState("logpath\t", LogSettingSlogUICommonControl.this.rdoSDCard.isChecked(), true);
                    LogSettingSlogUICommonControl.this.btnDump.setEnabled(false);
                    return;
                case R.id.btn_general_dump:
                    LogSettingSlogUICommonControl.this.dumpLog();
                    return;
                case R.id.btn_general_clearall:
                    LogSettingSlogUICommonControl.this.clearLog();
                    return;
                case R.id.chk_general_alwaysrun:
                    SlogAction.setAlwaysRun("slogsvc.conf", LogSettingSlogUICommonControl.this.chkAlwaysRun.isChecked());
                    if (LogSettingSlogUICommonControl.this.chkAlwaysRun.isChecked()) {
                        if (LogSettingSlogUICommonControl.this.startService(LogSettingSlogUICommonControl.this.intentSvc) == null) {
                            Toast.makeText(LogSettingSlogUICommonControl.this, LogSettingSlogUICommonControl.this.getText(R.string.toast_service_slog_start_failed), 0).show();
                            return;
                        }
                        return;
                    } else if (!LogSettingSlogUICommonControl.this.stopService(LogSettingSlogUICommonControl.this.intentSvc)) {
                        Toast.makeText(LogSettingSlogUICommonControl.this, LogSettingSlogUICommonControl.this.getText(R.string.toast_service_slog_end_failed), 0).show();
                        return;
                    } else {
                        return;
                    }
                case R.id.chk_general_snapsvc:
                    if (LogSettingSlogUICommonControl.this.chkSnap.isChecked()) {
                        Toast.makeText(LogSettingSlogUICommonControl.this, LogSettingSlogUICommonControl.this.getText(R.string.toast_snap_prompt), 0).show();
                        if (LogSettingSlogUICommonControl.this.startService(LogSettingSlogUICommonControl.this.intentSnap) == null) {
                            Toast.makeText(LogSettingSlogUICommonControl.this, LogSettingSlogUICommonControl.this.getText(R.string.toast_service_snap_start_failed), 0).show();
                        }
                    } else if (!LogSettingSlogUICommonControl.this.stopService(LogSettingSlogUICommonControl.this.intentSnap)) {
                        Toast.makeText(LogSettingSlogUICommonControl.this, LogSettingSlogUICommonControl.this.getText(R.string.toast_service_snap_end_failed), 0).show();
                    }
                    SlogAction.setAlwaysRun("snapsvc.conf", LogSettingSlogUICommonControl.this.chkSnap.isChecked());
                    return;
                case R.id.chk_general_autoclear:
                    SlogAction.SetState("var\tslogsaveall\t", LogSettingSlogUICommonControl.this.chkClearLogAuto.isChecked(), true);
                    return;
                default:
                    return;
            }
        }
    }

    private class MainThreadHandler extends Handler {
        public MainThreadHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    LogSettingSlogUICommonControl.this.syncState();
                    return;
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_general_commoncontrol);
        this.mHandler = new MainThreadHandler(getMainLooper());
        this.rdoGeneralOn = (RadioButton) findViewById(R.id.rdo_general_on);
        this.rdoGeneralOff = (RadioButton) findViewById(R.id.rdo_general_off);
        this.rdoGeneralLowPower = (RadioButton) findViewById(R.id.rdo_general_lowpower);
        this.chkAndroid = (CheckBox) findViewById(R.id.chk_general_android_switch);
        this.chkModem = (CheckBox) findViewById(R.id.chk_general_modem_switch);
        this.chkAlwaysRun = (CheckBox) findViewById(R.id.chk_general_alwaysrun);
        this.chkSnap = (CheckBox) findViewById(R.id.chk_general_snapsvc);
        this.chkClearLogAuto = (CheckBox) findViewById(R.id.chk_general_autoclear);
        this.rdoNAND = (RadioButton) findViewById(R.id.rdo_general_nand);
        this.rdoSDCard = (RadioButton) findViewById(R.id.rdo_general_sdcard);
        this.btnClear = (Button) findViewById(R.id.btn_general_clearall);
        this.btnDump = (Button) findViewById(R.id.btn_general_dump);
        this.intentSvc = new Intent("svcSlog");
        this.intentSvc.setClass(this, SlogService.class);
        this.intentSnap = new Intent("svcSnap");
        this.intentSnap.setClass(this, SlogUISnapService.class);
        this.intentMedia = new Intent().setComponent(DEFAULT_CONTAINER_COMPONENT);
        if (!getApplicationContext().bindService(this.intentMedia, this.mMediaContainerConn, 1)) {
            Log.e("SlogUI", "Unable to bind MediaContainerService!");
        }
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService("layout_inflater");
        final LayoutInflater layoutInflater2 = layoutInflater;
        this.mModeListAdapter = new CursorAdapter(this, getContentResolver().query(SlogProvider.URI_MODES, null, null, null, null), true) {
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return layoutInflater2.inflate(17367043, parent, false);
            }

            public void bindView(View view, Context context, Cursor cursor) {
                ((TextView) view.findViewById(16908308)).setText(cursor.getString(cursor.getColumnIndex("name")));
            }
        };
        prepareModeListDialogs();
        this.chkAlwaysRun.setChecked(SlogAction.isAlwaysRun("slogsvc.conf"));
        this.chkSnap.setChecked(SlogAction.isAlwaysRun("snapsvc.conf"));
        if (this.chkAlwaysRun.isChecked()) {
            startService(this.intentSvc);
        }
        if (this.chkSnap.isChecked()) {
            startService(this.intentSnap);
        }
        View.OnClickListener clkListenner = new ClkListenner();
        this.rdoGeneralOn.setOnClickListener(clkListenner);
        this.rdoGeneralOff.setOnClickListener(clkListenner);
        this.rdoGeneralLowPower.setOnClickListener(clkListenner);
        this.chkAndroid.setOnClickListener(clkListenner);
        this.chkModem.setOnClickListener(clkListenner);
        this.rdoNAND.setOnClickListener(clkListenner);
        this.rdoSDCard.setOnClickListener(clkListenner);
        this.btnClear.setOnClickListener(clkListenner);
        this.btnDump.setOnClickListener(clkListenner);
        this.chkAlwaysRun.setOnClickListener(clkListenner);
        this.chkSnap.setOnClickListener(clkListenner);
        this.chkClearLogAuto.setOnClickListener(clkListenner);
    }

    private void prepareModeListDialogs() {
        this.mNewModeNameEditText = new EditText(this);
        if (this.mNewModeNameEditText != null) {
            this.mNewModeNameEditText.setSingleLine(true);
            this.mNewModeDialog = new Builder(this).setTitle(R.string.mode_dialog_add_title).setView(this.mNewModeNameEditText).setPositiveButton(R.string.alert_dump_dialog_ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (LogSettingSlogUICommonControl.this.mNewModeNameEditText.getText() != null) {
                        String modeName = LogSettingSlogUICommonControl.this.mNewModeNameEditText.getText().toString();
                        if (!"".equals(modeName)) {
                            Cursor cursor = LogSettingSlogUICommonControl.this.getContentResolver().query(SlogProvider.URI_MODES, null, null, null, null);
                            while (cursor.moveToNext()) {
                                if (modeName.equals(cursor.getString(cursor.getColumnIndex("name")))) {
                                    LogSettingSlogUICommonControl.this.showDuplicatedModeDialog(modeName, cursor.getInt(cursor.getColumnIndex("_id")));
                                    cursor.close();
                                    return;
                                }
                            }
                            cursor.close();
                            SlogAction.saveAsNewMode(modeName, LogSettingSlogUICommonControl.this);
                        }
                    }
                }
            }).setNegativeButton(R.string.alert_dump_dialog_cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).create();
            this.mModeListDialogBuilder = new Builder(this);
            this.mUpdateListener = new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SlogAction.updateMode(LogSettingSlogUICommonControl.this, (int) LogSettingSlogUICommonControl.this.mModeListAdapter.getItemId(which));
                }
            };
            this.mSettingListener = new OnClickListener() {
                public void onClick(DialogInterface dialog, final int which) {
                    new Thread() {
                        public void run() {
                            SlogAction.setAllStates(LogSettingSlogUICommonControl.this, (int) LogSettingSlogUICommonControl.this.mModeListAdapter.getItemId(which));
                            LogSettingSlogUICommonControl.this.mHandler.sendEmptyMessage(1);
                        }
                    }.start();
                }
            };
            this.mDeleteListener = new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SlogAction.deleteMode(LogSettingSlogUICommonControl.this, (int) LogSettingSlogUICommonControl.this.mModeListAdapter.getItemId(which));
                }
            };
        }
    }

    private void showDuplicatedModeDialog(String modeName, final int modeId) {
        OnClickListener listener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SlogAction.updateMode(LogSettingSlogUICommonControl.this, modeId);
            }
        };
        new Builder(this).setTitle(R.string.mode_dialog_duplicate_title).setMessage(R.string.mode_dialog_duplicate_message).setPositiveButton(R.string.mode_dialog_duplicate_positive, listener).setNegativeButton(R.string.mode_dialog_duplicate_negative, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LogSettingSlogUICommonControl.this.mNewModeDialog.show();
            }
        }).create().show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.slog_mode_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.slog_add_mode:
                this.mNewModeDialog.show();
                return true;
            case R.id.slog_select:
                this.mModeListAdapter.notifyDataSetChanged();
                this.mModeListDialogBuilder.setTitle(R.string.mode_dialog_select_title).setAdapter(this.mModeListAdapter, this.mSettingListener).create().show();
                return true;
            case R.id.slog_delete:
                this.mModeListAdapter.notifyDataSetChanged();
                this.mModeListDialogBuilder.setTitle(R.string.mode_dialog_delete_title).setAdapter(this.mModeListAdapter, this.mDeleteListener).create().show();
                return true;
            case R.id.slog_update:
                this.mModeListAdapter.notifyDataSetChanged();
                this.mModeListDialogBuilder.setTitle(R.string.mode_dialog_update_title).setAdapter(this.mModeListAdapter, this.mUpdateListener).create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume() {
        super.onResume();
        syncState();
    }

    public void syncState() {
        boolean tempHost;
        boolean z;
        boolean tempHostOn = SlogAction.GetState("\n", true).equals("enable");
        boolean tempHostLowPower = SlogAction.GetState("\n", true).equals("low_power");
        if (tempHostOn) {
            this.rdoGeneralOn.setChecked(true);
        } else if (tempHostLowPower) {
            this.rdoGeneralLowPower.setChecked(true);
        } else {
            this.rdoGeneralOff.setChecked(true);
        }
        if (tempHostOn || tempHostLowPower) {
            tempHost = true;
        } else {
            tempHost = false;
        }
        SlogAction.SetCheckBoxBranchState(this.chkAndroid, tempHost, SlogAction.GetState(101));
        SlogAction.SetCheckBoxBranchState(this.chkModem, tempHost, SlogAction.GetState("stream\tmodem\t"));
        SlogAction.SetCheckBoxBranchState(this.chkClearLogAuto, tempHost, SlogAction.GetState("var\tslogsaveall\t"));
        boolean isSDCard = SlogAction.GetState("logpath\t");
        RadioButton radioButton = this.rdoSDCard;
        if (SlogAction.IsHaveSDCard()) {
            z = true;
        } else {
            z = false;
        }
        radioButton.setEnabled(z);
        this.rdoSDCard.setChecked(isSDCard);
        radioButton = this.rdoNAND;
        if (isSDCard) {
            z = false;
        } else {
            z = true;
        }
        radioButton.setChecked(z);
        if (tempHost) {
            this.btnClear.setEnabled(false);
        } else {
            this.btnClear.setEnabled(true);
        }
    }

    void dumpLog() {
        final EditText edtDump = new EditText(this);
        if (edtDump != null) {
            edtDump.setSingleLine(true);
            new Builder(this).setIcon(17301543).setTitle(R.string.alert_dump_title).setView(edtDump).setPositiveButton(R.string.alert_dump_dialog_ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (edtDump.getText() != null) {
                        String fileName = edtDump.getText().toString();
                        if (!Pattern.compile("[0-9a-zA-Z]*").matcher(fileName).matches() || "".equals(fileName)) {
                            Toast.makeText(LogSettingSlogUICommonControl.this, LogSettingSlogUICommonControl.this.getText(R.string.toast_dump_filename_error), 1).show();
                        } else {
                            SlogAction.dump(edtDump.getText().toString());
                        }
                    }
                }
            }).setNegativeButton(R.string.alert_dump_dialog_cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).create().show();
        }
    }

    void clearLog() {
        new Builder(this).setIcon(17301543).setTitle(R.string.alert_clear_title).setMessage(R.string.alert_clear_string).setPositiveButton(R.string.alert_clear_dialog_ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SlogAction.ClearLog();
            }
        }).setNegativeButton(R.string.alert_clear_dialog_cancel, null).create().show();
    }

    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    void requestDumpLog() {
        new Builder(this).setIcon(17301543).setTitle(R.string.alert_request_dump_title).setMessage(R.string.alert_request_dump_prompt).setPositiveButton(R.string.alert_dump_dialog_ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (LogSettingSlogUICommonControl.this.rdoSDCard.isChecked()) {
                    LogSettingSlogUICommonControl.this.dumpLog();
                }
            }
        }).setNegativeButton(R.string.alert_dump_dialog_cancel, null).create().show();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
