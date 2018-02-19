package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SlogUISnapAction extends Activity {
    protected void onCreate(Bundle snap) {
        super.onCreate(snap);
        try {
            SlogAction.snap(this);
        } catch (ExceptionInInitializerError e) {
            Log.e("SlogUISnapAction", "Illegal state because the activity was uninitialized. Need improve");
        }
        finish();
    }
}
