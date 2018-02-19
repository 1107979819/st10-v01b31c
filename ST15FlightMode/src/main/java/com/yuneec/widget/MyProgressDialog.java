package com.yuneec.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;

public class MyProgressDialog extends Dialog {
    private TextView mMessage = ((TextView) findViewById(R.id.message));
    private Object mTitle;

    public MyProgressDialog(Context context) {
        super(context);
        requestWindowFeature(1);
        setContentView(R.layout.my_progress_dialog_layout);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(Utilities.FLAG_HOMEKEY_DISPATCHED);
    }

    public void setMessage(CharSequence message) {
        this.mMessage.setText(message);
    }

    public static MyProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        MyProgressDialog dialog = new MyProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(cancelable);
        dialog.show();
        return dialog;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 3 || event.isCanceled()) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }
}
