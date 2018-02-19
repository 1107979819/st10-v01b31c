package com.spreadtrum.android.eng;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

public class LogSettingSlogUITabHostActivity extends TabActivity {
    public static ProgressDialog mProgressDialog;
    public static SHandler mTabHostHandler = new SHandler();

    static class SHandler extends Handler {
        static Context mContext;
        int countAndroidLogBranchComplete;
        boolean isAndroidLog;

        SHandler() {
        }

        public static void setContext(Context context) {
            mContext = context;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 15:
                    if (!this.isAndroidLog) {
                        try {
                            LogSettingSlogUITabHostActivity.mProgressDialog.show();
                            return;
                        } catch (Exception error) {
                            Log.e("LogSettingSlogUITabHostActivity", "failed show progress dialog, maybe the activity was finished." + error.getMessage());
                            return;
                        }
                    }
                    return;
                case 16:
                    if (this.isAndroidLog) {
                        int i = this.countAndroidLogBranchComplete + 1;
                        this.countAndroidLogBranchComplete = i;
                        if (i == 4) {
                            this.isAndroidLog = false;
                        }
                    }
                    LogSettingSlogUITabHostActivity.mProgressDialog.cancel();
                    return;
                case 17:
                    LogSettingSlogUITabHostActivity.mProgressDialog.setCancelable(false);
                    try {
                        LogSettingSlogUITabHostActivity.mProgressDialog.show();
                        return;
                    } catch (Exception error2) {
                        Log.e("LogSettingSlogUITabHostActivity", "failed show progress dialog, maybe the activity was finished." + error2.getMessage());
                        return;
                    }
                case 18:
                    if (mContext != null) {
                        Toast.makeText(mContext, R.string.slog_dump_ok, 1).show();
                    }
                    LogSettingSlogUITabHostActivity.mProgressDialog.cancel();
                    LogSettingSlogUITabHostActivity.mProgressDialog.setCancelable(true);
                    return;
                case 19:
                    LogSettingSlogUITabHostActivity.mProgressDialog.setCancelable(false);
                    try {
                        LogSettingSlogUITabHostActivity.mProgressDialog.show();
                        return;
                    } catch (Exception error22) {
                        Log.e("LogSettingSlogUITabHostActivity", "failed show progress dialog, maybe the activity was finished." + error22.getMessage());
                        return;
                    }
                case 20:
                    LogSettingSlogUITabHostActivity.mProgressDialog.setCancelable(true);
                    LogSettingSlogUITabHostActivity.mProgressDialog.cancel();
                    if (mContext != null) {
                        Toast.makeText(mContext, R.string.slog_clear_ok, 1).show();
                        return;
                    }
                    return;
                case 21:
                    if (mContext == null) {
                        Log.e("SlogUI", "No context here, can't show toast");
                        return;
                    } else {
                        Toast.makeText(mContext, mContext.getText(R.string.toast_snap_success), 0).show();
                        return;
                    }
                case 22:
                    if (mContext != null) {
                        Toast.makeText(mContext, mContext.getText(R.string.toast_snap_failed), 0).show();
                        return;
                    }
                    return;
                case 23:
                    LogSettingSlogUITabHostActivity.mProgressDialog.cancel();
                    LogSettingSlogUITabHostActivity.mProgressDialog.setCancelable(true);
                    if (mContext != null) {
                        Toast.makeText(mContext, R.string.slog_dump_failed, 1).show();
                        return;
                    }
                    return;
                case 24:
                    LogSettingSlogUITabHostActivity.mProgressDialog.cancel();
                    LogSettingSlogUITabHostActivity.mProgressDialog.setCancelable(true);
                    if (mContext != null) {
                        Toast.makeText(mContext, R.string.slog_dump_failed, 1).show();
                        return;
                    }
                    return;
                case 25:
                    LogSettingSlogUITabHostActivity.mProgressDialog.setCancelable(true);
                    LogSettingSlogUITabHostActivity.mProgressDialog.cancel();
                    if (mContext != null) {
                        Toast.makeText(mContext, R.string.slog_clear_failed, 1).show();
                        return;
                    }
                    return;
                case 101:
                    this.isAndroidLog = true;
                    this.countAndroidLogBranchComplete = 1;
                    try {
                        LogSettingSlogUITabHostActivity.mProgressDialog.show();
                        return;
                    } catch (Exception error222) {
                        Log.e("LogSettingSlogUITabHostActivity", "failed show progress dialog, maybe the activity was finished." + error222.getMessage());
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("\n").setIndicator(getText(R.string.tabtag_tabhost_tabgeneral)).setContent(new Intent(this, LogSettingSlogUICommonControl.class)));
        tabHost.addTab(tabHost.newTabSpec("android").setIndicator(getText(R.string.tabtag_tabhost_tabandroid)).setContent(new Intent(this, LogSettingSlogUIAndroidPage.class)));
        tabHost.addTab(tabHost.newTabSpec("stream\tmodem\t").setIndicator(getText(R.string.tabtag_tabhost_tabmodem)).setContent(new Intent(this, LogSettingSlogUIModemPage.class)));
        SlogAction.contextMainActivity = this;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getText(R.string.progressdialog_tabhost_prompt));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        SHandler sHandler = mTabHostHandler;
        SHandler.setContext(getApplicationContext());
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onStop();
    }
}
