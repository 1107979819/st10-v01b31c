package com.spreadtrum.android.eng;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;

public class FastbootSettings extends PreferenceActivity {
    private CheckBoxPreference fastboot;
    PowerManager pm;
    boolean state = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.fastbootsetting);
        this.fastboot = (CheckBoxPreference) findPreference("fastboot_enable");
        this.fastboot.setChecked(Build.SUPPORT_FASTBOOT);
        this.pm = (PowerManager) getSystemService("power");
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == this.fastboot) {
            this.state = this.fastboot.isChecked();
            String title = getResources().getString(R.string.confirm);
            new Builder(this).setTitle(title).setMessage(getResources().getString(R.string.reboot_message)).setPositiveButton(17039379, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SystemProperties.set("persist.sys.support.fastboot", FastbootSettings.this.state ? "true" : "false");
                    FastbootSettings.this.pm.reboot(null);
                }
            }).setNegativeButton(17039369, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    FastbootSettings.this.fastboot.setChecked(!FastbootSettings.this.state);
                }
            }).setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 4) {
                        boolean z;
                        CheckBoxPreference access$000 = FastbootSettings.this.fastboot;
                        if (FastbootSettings.this.state) {
                            z = false;
                        } else {
                            z = true;
                        }
                        access$000.setChecked(z);
                    }
                    return false;
                }
            }).create().show();
        }
        return true;
    }
}
