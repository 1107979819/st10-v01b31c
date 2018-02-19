package com.yuneec.galleryloader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.yuneec.flightmode15.R;

public class GalleryItem extends RelativeLayout implements Checkable {
    private ImageView mCheckImg;
    private boolean mChecked;

    public GalleryItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.gallery_item, this);
        this.mCheckImg = (ImageView) findViewById(R.id.image_check);
        this.mChecked = false;
    }

    public GalleryItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GalleryItem(Context context) {
        this(context, null, 0);
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
        if (checked) {
            this.mCheckImg.setVisibility(0);
        } else {
            this.mCheckImg.setVisibility(4);
        }
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public void toggle() {
        setChecked(!this.mChecked);
    }
}
