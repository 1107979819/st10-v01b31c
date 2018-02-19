package com.yuneec.widget;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.yuneec.flightmode15.R;

public class MyToast extends Toast {
    public MyToast(Context context) {
        super(context);
    }

    public static Toast makeText(Context context, CharSequence text, int custLayout, int duration) {
        View v;
        Toast result = new Toast(context);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService("layout_inflater");
        if (custLayout == 0) {
            v = inflate.inflate(R.layout.my_toast_default, null);
        } else {
            v = inflate.inflate(custLayout, null);
        }
        ((TextView) v.findViewById(R.id.my_toast_text)).setText(text);
        result.setView(v);
        result.setDuration(duration);
        return result;
    }

    public static Toast makeText(Context context, int resId, int custLayout, int duration) throws NotFoundException {
        return makeText(context, context.getResources().getText(resId), custLayout, duration);
    }
}
