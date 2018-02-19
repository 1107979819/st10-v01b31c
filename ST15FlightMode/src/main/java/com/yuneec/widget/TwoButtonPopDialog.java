package com.yuneec.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;

public class TwoButtonPopDialog extends Dialog {
    private TextView mMessage = ((TextView) findViewById(R.id.message));
    private Button mNegativeButton = ((Button) findViewById(R.id.cancel));
    private Button mPositiveButton = ((Button) findViewById(R.id.confirm));
    private TextView mTitle = ((TextView) findViewById(R.id.title));

    public TwoButtonPopDialog(Context context) {
        super(context, R.style.dialog_style);
        setContentView(R.layout.two_button_pop_dialog_layout);
        adjustHeight(520);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(Utilities.FLAG_HOMEKEY_DISPATCHED);
    }

    public void setTitle(CharSequence title) {
        this.mTitle.setText(title);
    }

    public void setTitle(int titleResId) {
        this.mTitle.setText(titleResId);
    }

    public void setMessage(CharSequence message) {
        this.mMessage.setText(message);
    }

    public void setMessage(int messageResId) {
        this.mMessage.setText(messageResId);
    }

    public void setMessageGravity(int gravity) {
        this.mMessage.setGravity(gravity);
    }

    public void setPositiveButton(int resId, OnClickListener listener) {
        this.mPositiveButton.setText(resId);
        this.mPositiveButton.setOnClickListener(listener);
    }

    public void setNegativeButton(int resId, OnClickListener listener) {
        this.mNegativeButton.setText(resId);
        this.mNegativeButton.setOnClickListener(listener);
    }

    public void adjustHeight(int newHeight) {
        LayoutParams params = getWindow().getAttributes();
        params.height = newHeight;
        getWindow().setAttributes(params);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 3 || event.isCanceled()) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }
}
