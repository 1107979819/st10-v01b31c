package com.yuneec.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import com.yuneec.flightmode15.R;

public class WhiteBalanceScrollView extends ScrollView {
    private static final String TAG = "WhiteBalanceScrollView";
    private static String[] aa = new String[]{"Auto", "Lock", "Sunny", "Cloudy", "Fluorescent", "Incandescent", "Sunset/Sunrise"};
    private static int[] draw = new int[]{R.drawable.awb, R.drawable.lock, R.drawable.daylight, R.drawable.cloudy, R.drawable.fluorescent, R.drawable.incandescent, R.drawable.sunset};
    private int[] itemIds;
    public OnItemSelectedListener mItemSelectedListener;
    private OnClickListener mWBitemOnClickListener;
    private WhiteBalanceListItem[] mWBitems;

    public interface OnItemSelectedListener {
        void onItemSelected(int i);
    }

    public WhiteBalanceScrollView(Context context) {
        this(context, null);
    }

    public WhiteBalanceScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteBalanceScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.itemIds = new int[]{R.id.item_auto, R.id.item_lock, R.id.item_sunny, R.id.item_cloudy, R.id.item_fluorescent, R.id.item_incandescent, R.id.item_sunset};
        this.mWBitems = new WhiteBalanceListItem[this.itemIds.length];
        this.mWBitemOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                for (int i = 0; i < WhiteBalanceScrollView.this.itemIds.length; i++) {
                    if (v.equals(WhiteBalanceScrollView.this.mWBitems[i])) {
                        WhiteBalanceScrollView.this.mWBitems[i].setSelected(true);
                        WhiteBalanceScrollView.this.mItemSelectedListener.onItemSelected(i);
                    } else {
                        WhiteBalanceScrollView.this.mWBitems[i].setSelected(false);
                    }
                }
            }
        };
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.whitebalancescrollview, this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < this.mWBitems.length; i++) {
            this.mWBitems[i] = (WhiteBalanceListItem) findViewById(this.itemIds[i]);
            this.mWBitems[i].setImage(getResources().getDrawable(draw[i]));
            this.mWBitems[i].setText(aa[i]);
            this.mWBitems[i].setOnClickListener(this.mWBitemOnClickListener);
        }
    }

    public void setItemSelect(int position) {
        for (int i = 0; i < this.itemIds.length; i++) {
            if (position == i) {
                this.mWBitems[i].setSelected(true);
            } else {
                this.mWBitems[i].setSelected(false);
            }
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener itemSelectedListener) {
        this.mItemSelectedListener = itemSelectedListener;
    }
}
