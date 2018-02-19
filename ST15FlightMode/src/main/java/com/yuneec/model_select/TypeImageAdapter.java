package com.yuneec.model_select;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import java.util.ArrayList;

public class TypeImageAdapter extends ArrayAdapter<SubTypeInfo> {
    private ImageView image_item;
    private int mHightlightPos;
    private ArrayList<SubTypeInfo> mTypeImageInfo;
    private RelativeLayout rl;
    private TextView text_item;

    public TypeImageAdapter(Context context, ArrayList<SubTypeInfo> element, int hightlight_pos) {
        super(context, 0, element);
        this.mTypeImageInfo = element;
        this.mHightlightPos = hightlight_pos;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SubTypeInfo info = (SubTypeInfo) this.mTypeImageInfo.get(position);
        if (convertView == null) {
            convertView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.model_select_element, parent, false);
        }
        this.image_item = (ImageView) convertView.findViewById(R.id.image_item);
        this.text_item = (TextView) convertView.findViewById(R.id.text_item);
        this.rl = (RelativeLayout) convertView.findViewById(R.id.element_frame);
        if (position + 1 == this.mHightlightPos) {
            this.rl.setBackgroundResource(R.drawable.model_type_selected);
        }
        this.image_item.setImageResource(TypeImageResource.typeTransformToImageId(getContext(), info.type));
        this.text_item.setText(info.name);
        return convertView;
    }
}
