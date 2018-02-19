package com.yuneec.flightmode15;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ResolutionSelection extends Dialog {
    private RadioGroup mRadioGroup = ((RadioGroup) findViewById(R.id.message));

    public ResolutionSelection(Context context) {
        super(context, R.style.dialog_style);
        setContentView(R.layout.resolution_selection_layout);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setSelectionListener(OnCheckedChangeListener listener) {
        this.mRadioGroup.setOnCheckedChangeListener(listener);
    }

    public void setValue(String value) {
        if (value == null) {
            return;
        }
        if ("48P".equals(value)) {
            this.mRadioGroup.check(R.id.rln_item1);
        } else if ("50P".equals(value)) {
            this.mRadioGroup.check(R.id.rln_item2);
        } else if ("60P".equals(value)) {
            this.mRadioGroup.check(R.id.rln_item3);
        }
    }

    public String getValue() {
        int btnId = this.mRadioGroup.getCheckedRadioButtonId();
        if (btnId == R.id.rln_item1) {
            return "48P";
        }
        if (btnId == R.id.rln_item2) {
            return "50P";
        }
        if (btnId == R.id.rln_item3) {
            return "60P";
        }
        return null;
    }

    public void setMessageGravity(int gravity) {
        this.mRadioGroup.setGravity(gravity);
    }

    public void adjustHeight(int newHeight) {
        LayoutParams params = getWindow().getAttributes();
        params.height = newHeight;
        getWindow().setAttributes(params);
    }
}
