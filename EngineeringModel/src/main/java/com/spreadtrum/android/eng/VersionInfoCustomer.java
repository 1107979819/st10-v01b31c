package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemProperties;
import android.widget.TextView;

public class VersionInfoCustomer extends Activity {
    public static final String CUSTOMINFO = SystemProperties.get("ro.build.display.spid", "4G_W4_TD_MocoDroid2.2_W11.xx");
    private TextView tv = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.versioninfocustomer);
        this.tv = (TextView) findViewById(R.id.sprdversioninfo);
        this.tv.setText(CUSTOMINFO);
    }
}
