package com.yuneec.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.yuneec.flightmode15.R;

public class WhiteBalanceListItem extends LinearLayout {
    private static final String TAG = "WhiteBalanceListItem";
    private ImageView mImg;
    private TextView mText;

    public WhiteBalanceListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteBalanceListItem(Context context) {
        this(context, null);
    }

    public WhiteBalanceListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.whitebalance_list_item, this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mText = (TextView) findViewById(R.id.text);
        this.mImg = (ImageView) findViewById(R.id.img);
    }

    public void setText(String str) {
        this.mText.setText(str);
    }

    public void setImage(Drawable drawable) {
        this.mImg.setImageDrawable(drawable);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.list_selector_bg));
        } else {
            setBackgroundColor(getResources().getColor(R.color.color_list_item_normal));
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
