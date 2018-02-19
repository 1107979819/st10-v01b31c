package com.yuneec.widget;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.yuneec.flightmode15.R;

public class TextWidgetPreference extends ListPreference {
    private static final String TAG = TextWidgetPreference.class.getSimpleName();
    private TextView mTextView;
    private String textString = "";

    public TextWidgetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "TextWidgetPreference");
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.mTextView = (TextView) view.findViewById(R.id.widget_text);
        this.mTextView.setText(this.textString);
    }

    public void setWidgetText(String text) {
        this.textString = text;
        if (this.mTextView != null) {
            this.mTextView.setText(this.textString);
        }
    }
}
