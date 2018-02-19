package com.spreadtrum.android.eng;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class SlogUILowStorage extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Builder(this).setTitle(getString(R.string.low_free_space_title)).setMessage(getString(R.string.low_free_space_message)).setPositiveButton(getString(17039370), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SlogUILowStorage.this.finish();
            }
        }).setCancelable(false).create().show();
    }
}
