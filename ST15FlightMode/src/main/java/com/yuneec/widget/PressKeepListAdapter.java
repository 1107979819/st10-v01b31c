package com.yuneec.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import com.yuneec.flightmode15.R;
import java.util.List;
import java.util.Map;

public class PressKeepListAdapter extends SimpleAdapter {
    private int pressPosition = -1;

    public PressKeepListAdapter(Context context, List<? extends Map<String, ?>> data, int resourceId, String[] from, int[] to) {
        super(context, data, resourceId, from, to);
    }

    public void setPressPosition(int pressPosition) {
        this.pressPosition = pressPosition;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        if (this.pressPosition != position) {
            convertView.setBackgroundResource(0);
        } else {
            convertView.setBackgroundResource(R.drawable.list_selector_bg);
        }
        return convertView;
    }
}
