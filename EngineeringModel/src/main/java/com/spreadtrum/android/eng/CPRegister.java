package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.System;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CPRegister extends Activity implements OnClickListener {
    Button close;
    Button open;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setcpreg);
        this.open = (Button) findViewById(R.id.setopenbutton);
        this.close = (Button) findViewById(R.id.setclosebutton);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.setopenbutton) {
            System.putString(getContentResolver(), "yulong.sms.background.register", "true");
            this.open.setEnabled(false);
            this.close.setEnabled(true);
        } else if (v.getId() == R.id.setclosebutton) {
            System.putString(getContentResolver(), "yulong.sms.background.register", "false");
            this.close.setEnabled(false);
            this.open.setEnabled(true);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
