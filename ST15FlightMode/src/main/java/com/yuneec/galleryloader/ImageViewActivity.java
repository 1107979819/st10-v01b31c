package com.yuneec.galleryloader;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.yuneec.flightmode15.R;

public class ImageViewActivity extends Activity {
    private static final long AUTO_HIDE_ACTIONBAR_DURATION = 2000;
    private BitmapDrawable mBitmapDrawable;
    private Handler mHandler = new Handler();
    private Runnable mHideActionBarRunnable = new Runnable() {
        public void run() {
            ImageViewActivity.this.getActionBar().hide();
        }
    };
    private ImageView mImageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_img_view_layout);
        String url = getIntent().getStringExtra("url");
        if (url == null) {
            finish();
            return;
        }
        this.mBitmapDrawable = new BitmapDrawable(getResources(), url);
        this.mImageView = (ImageView) findViewById(R.id.imageView);
        this.mImageView.setImageDrawable(this.mBitmapDrawable);
        this.mImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ImageViewActivity.this.getActionBar().isShowing()) {
                    ImageViewActivity.this.mHandler.removeCallbacks(ImageViewActivity.this.mHideActionBarRunnable);
                    ImageViewActivity.this.mHideActionBarRunnable.run();
                    return;
                }
                ImageViewActivity.this.getActionBar().show();
                ImageViewActivity.this.mHandler.postDelayed(ImageViewActivity.this.mHideActionBarRunnable, ImageViewActivity.AUTO_HIDE_ACTIONBAR_DURATION);
            }
        });
        this.mHandler.postDelayed(this.mHideActionBarRunnable, AUTO_HIDE_ACTIONBAR_DURATION);
        getActionBar().setTitle("");
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mImageView.setImageDrawable(null);
        if (this.mBitmapDrawable.getBitmap() != null) {
            this.mBitmapDrawable.getBitmap().recycle();
        }
        this.mBitmapDrawable = null;
    }
}
