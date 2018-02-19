package com.spreadtrum.android.eng;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
import java.lang.reflect.Method;

public class ApnActivepdpFilter extends PreferenceActivity {
    private String LOG_TAG = "ApnActivepdpFilter";
    private boolean mChecked = false;
    private CheckBoxPreference mFilterAll;
    boolean mFilterAllStatus;
    private CheckBoxPreference mFilterDefault;
    boolean mFilterDefaultStatus;
    private CheckBoxPreference mFilterDun;
    boolean mFilterDunStatus;
    private CheckBoxPreference mFilterHipri;
    boolean mFilterHipriStatus;
    private CheckBoxPreference mFilterMms;
    boolean mFilterMmsStatus;
    private CheckBoxPreference mFilterSupl;
    boolean mFilterSuplStatus;
    private ITelephony mTelephony;
    private TelephonyManager mTelephonyManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        this.mTelephony = getITelephony(this);
        addPreferencesFromResource(R.layout.apn_activepdp_filter);
        this.mFilterAll = (CheckBoxPreference) findPreference("all");
        this.mFilterDefault = (CheckBoxPreference) findPreference("default");
        this.mFilterMms = (CheckBoxPreference) findPreference("mms");
        this.mFilterSupl = (CheckBoxPreference) findPreference("supl");
        this.mFilterDun = (CheckBoxPreference) findPreference("dun");
        this.mFilterHipri = (CheckBoxPreference) findPreference("hipri");
        updateApnFilterState();
    }

    void updateApnFilterState() {
        try {
            this.mFilterAllStatus = this.mTelephony.getApnActivePdpFilter("*");
            this.mFilterDefaultStatus = getITelephony(this).getApnActivePdpFilter("default");
            this.mFilterMmsStatus = getITelephony(this).getApnActivePdpFilter("mms");
            this.mFilterSuplStatus = getITelephony(this).getApnActivePdpFilter("supl");
            this.mFilterDunStatus = getITelephony(this).getApnActivePdpFilter("dun");
            this.mFilterHipriStatus = getITelephony(this).getApnActivePdpFilter("hipri");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        }
        if (this.mFilterDefaultStatus) {
            this.mFilterDefault.setChecked(true);
            this.mFilterDefault.setSummary("enable filter");
        } else {
            this.mFilterDefault.setChecked(false);
            this.mFilterDefault.setSummary("disable filter");
        }
        if (this.mFilterMmsStatus) {
            this.mFilterMms.setChecked(true);
            this.mFilterMms.setSummary("enable filter");
        } else {
            this.mFilterMms.setChecked(false);
            this.mFilterMms.setSummary("disable filter");
        }
        if (this.mFilterSuplStatus) {
            this.mFilterSupl.setChecked(true);
            this.mFilterSupl.setSummary("enable filter");
        } else {
            this.mFilterSupl.setChecked(false);
            this.mFilterSupl.setSummary("disable filter");
        }
        if (this.mFilterDunStatus) {
            this.mFilterDun.setChecked(true);
            this.mFilterDun.setSummary("enable filter");
        } else {
            this.mFilterDun.setChecked(false);
            this.mFilterDun.setSummary("disable filter");
        }
        if (this.mFilterHipriStatus) {
            this.mFilterHipri.setChecked(true);
            this.mFilterHipri.setSummary("enable filter");
        } else {
            this.mFilterHipri.setChecked(false);
            this.mFilterHipri.setSummary("disable filter");
        }
        if (this.mFilterAllStatus) {
            this.mFilterAll.setChecked(true);
            this.mFilterAll.setSummary("enable filter");
            this.mFilterDefault.setEnabled(false);
            this.mFilterMms.setEnabled(false);
            this.mFilterSupl.setEnabled(false);
            this.mFilterDun.setEnabled(false);
            this.mFilterHipri.setEnabled(false);
            return;
        }
        this.mFilterAll.setChecked(false);
        this.mFilterAll.setSummary("disable filter");
        this.mFilterDefault.setEnabled(true);
        this.mFilterMms.setEnabled(true);
        this.mFilterSupl.setEnabled(true);
        this.mFilterDun.setEnabled(true);
        this.mFilterHipri.setEnabled(true);
    }

    protected void onResume() {
        super.onResume();
        updateApnFilterState();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if ("networkinfo".equals(key)) {
            Intent startIntent = new Intent();
            startIntent.setClassName("com.android.settings", "com.android.settings.RadioInfo");
            startActivity(startIntent);
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        if (preference instanceof CheckBoxPreference) {
            this.mChecked = ((CheckBoxPreference) preference).isChecked();
        }
        if ("all".equals(key)) {
            key = "*";
        }
        Log.i(this.LOG_TAG, "onPreferenceChange(), " + key + this.mChecked);
        try {
            this.mTelephony.setApnActivePdpFilter(key, this.mChecked);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        }
        updateApnFilterState();
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private static ITelephony getITelephony(Context context) {
        Method declaredMethod;
        SecurityException e;
        NoSuchMethodException e2;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        try {
            declaredMethod = TelephonyManager.class.getDeclaredMethod("getITelephony", (Class[]) null);
            try {
                declaredMethod.setAccessible(true);
            } catch (SecurityException e3) {
                e = e3;
                e.printStackTrace();
                return (ITelephony) declaredMethod.invoke(telephonyManager, (Object[]) null);
            } catch (NoSuchMethodException e4) {
                e2 = e4;
                e2.printStackTrace();
                return (ITelephony) declaredMethod.invoke(telephonyManager, (Object[]) null);
            }
        } catch (SecurityException e5) {
            e = e5;
            declaredMethod = null;
            e.printStackTrace();
            return (ITelephony) declaredMethod.invoke(telephonyManager, (Object[]) null);
        } catch (NoSuchMethodException e6) {
            e2 = e6;
            declaredMethod = null;
            e2.printStackTrace();
            return (ITelephony) declaredMethod.invoke(telephonyManager, (Object[]) null);
        }
        try {
            return (ITelephony) declaredMethod.invoke(telephonyManager, (Object[]) null);
        } catch (Exception e7) {
            e7.printStackTrace();
            return null;
        }
    }
}
