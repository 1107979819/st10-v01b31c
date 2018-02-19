package com.yuneec.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.KeyEvent;
import com.yuneec.flightmode15.Utilities;

public class BaseDialog extends Dialog {
    public BaseDialog(Context context) {
        super(context);
    }

    public BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(Utilities.FLAG_HOMEKEY_DISPATCHED);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 3 || event.isCanceled()) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }
}
