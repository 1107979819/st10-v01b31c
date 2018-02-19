package com.hipai.st15tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button) findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.spreadst.validationtools", "com.spreadst.validationtools.VolidationToolsMainActivity");
                try {
                    Log.e("cui", "starting activity " + intent.toString());
                    MainActivity.this.startActivity(intent);
                } catch (Exception e) {
                    Log.e("cui", "Unable to start activity " + intent.toString());
                }
            }
        });
        ((Button) findViewById(R.id.button2)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.android.settings", "com.android.settings.DevelopmentSettings");
                try {
                    Log.e("cui", "starting activity " + intent.toString());
                    MainActivity.this.startActivity(intent);
                } catch (Exception e) {
                    Log.e("cui", "Unable to start activity " + intent.toString());
                }
            }
        });
        ((Button) findViewById(R.id.button3)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.yuneec.flightmode15", "com.yuneec.test.TestMain");
                try {
                    MainActivity.this.startActivity(intent);
                } catch (Exception e) {
                    Log.e("cui", "Unable to start activity " + intent.toString());
                }
            }
        });
        ((Button) findViewById(R.id.button4)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
                try {
                    MainActivity.this.startActivity(intent);
                } catch (Exception e) {
                    Log.e("cui", "Unable to start activity " + intent.toString());
                }
            }
        });
        ((Button) findViewById(R.id.button5)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.adbRoot(Boolean.valueOf(SystemProperties.get("persist.sys.secret_root", "0").equals("0")));
            }
        });
        ((Button) findViewById(R.id.button6)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.spreadtrum.android.eng", "com.spreadtrum.android.eng.AppSettings");
                try {
                    MainActivity.this.startActivity(intent);
                } catch (Exception e) {
                    Log.e("cui", "Unable to start activity " + intent.toString());
                }
            }
        });
        ((Button) findViewById(R.id.button7)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.hipad.wifitest", "com.hipad.wifitest.WifiTest");
                try {
                    MainActivity.this.startActivity(intent);
                } catch (Exception e) {
                    Log.e("cui", "Unable to start activity " + intent.toString());
                }
            }
        });
    }

    private void adbRoot(Boolean IsRoot) {
        SystemProperties.set("persist.sys.secret_root", IsRoot.booleanValue() ? "1" : "0");
        String usbConfig = SystemProperties.get("sys.usb.config", "mtp");
        SystemProperties.set("sys.usb.config", "none");
        SystemProperties.set("sys.usb.config", usbConfig);
        Toast.makeText(this, "Update adb to " + (IsRoot.booleanValue() ? "ROOT" : "USER") + " mode!", 1).show();
    }
}
