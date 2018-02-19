package com.spreadtrum.android.eng;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import java.io.File;

public class EngInstallActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        if (data != null) {
            int requestCode = data.getIntExtra("action", 0);
            String extra = data.getStringExtra("name");
            if (extra == null || requestCode == 0) {
                finish();
            }
            if (requestCode == 10) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setDataAndType(Uri.fromFile(new File(extra)), "application/vnd.android.package-archive");
                intent.putExtra("android.intent.extra.RETURN_RESULT", true);
                startActivityForResult(intent, requestCode);
            } else if (requestCode == 12) {
                startActivityForResult(new Intent("android.intent.action.DELETE", Uri.parse("package:" + extra)), requestCode);
            } else {
                finish();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            Log.e("EngInstallActivity", "data is null, return");
            finish();
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (requestCode == 10) {
            EngInstallHelperService.onResult(data.getIntExtra("android.intent.extra.INSTALL_RESULT", -255));
        } else if (requestCode == 12) {
            EngInstallHelperService.onResult(data.getIntExtra("android.intent.extra.INSTALL_RESULT", -255));
        } else {
            Log.e("EngInstallActivity", "Unknow request code.");
            finish();
        }
        finish();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
