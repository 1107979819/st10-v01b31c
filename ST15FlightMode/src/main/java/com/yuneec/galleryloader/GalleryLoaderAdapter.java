package com.yuneec.galleryloader;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.yuneec.flightmode15.R;

public class GalleryLoaderAdapter extends BaseAdapter {
    private static final String TAG = "GalleryLoaderAdapter";
    private boolean mBusy = false;
    private Context mContext;
    private int mCount;
    private ImageLoader mImageLoader;
    private boolean mSelectionMode = false;
    private String[] urlArrays;

    static class ViewHolder {
        ImageView mImageView;
        TextView mTextView;

        ViewHolder() {
        }
    }

    public void setFlagBusy(boolean busy) {
        this.mBusy = busy;
    }

    public void setSelectionMode(boolean enable) {
        this.mSelectionMode = enable;
    }

    public GalleryLoaderAdapter(Context context, String[] url) {
        this.mCount = url.length;
        this.mContext = context;
        this.urlArrays = url;
        this.mImageLoader = new ImageLoader(context);
    }

    public ImageLoader getImageLoader() {
        return this.mImageLoader;
    }

    public int getCount() {
        return this.mCount;
    }

    public Object getItem(int position) {
        return this.urlArrays[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        String url;
        if (convertView == null) {
            convertView = new GalleryItem(this.mContext);
            convertView.setLayoutParams(new LayoutParams(-2, -2));
            viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) convertView.findViewById(R.id.item_text);
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mImageView.setTag(Integer.valueOf(position));
        if (position > this.urlArrays.length) {
            Log.w(TAG, "UI position > url arrays");
            url = this.urlArrays[0];
        } else {
            url = this.urlArrays[position];
        }
        if (!this.mSelectionMode) {
            convertView.findViewById(R.id.image_check).setVisibility(4);
        }
        if (this.mBusy) {
            this.mImageLoader.DisplayImage(Gallery.mCurrentFlieGetUrl + url, viewHolder.mImageView, true);
            viewHolder.mTextView.setText(url);
        } else {
            this.mImageLoader.DisplayImage(Gallery.mCurrentFlieGetUrl + url, viewHolder.mImageView, false);
            viewHolder.mTextView.setText(url);
        }
        return convertView;
    }
}
