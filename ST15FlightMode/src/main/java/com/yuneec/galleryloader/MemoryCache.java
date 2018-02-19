package com.yuneec.galleryloader;

import android.graphics.Bitmap;
import android.util.Log;
import com.yuneec.flightmode15.Utilities;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MemoryCache {
    private static final String TAG = "MemoryCache";
    private Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap(10, Utilities.RIGHT_K_MAX, true));
    private long limit = 1000000;
    private long size = 0;

    public MemoryCache() {
        setLimit((long) (((double) Runtime.getRuntime().maxMemory()) * 0.25d));
    }

    public void setLimit(long new_limit) {
        this.limit = new_limit;
        Log.i(TAG, "MemoryCache will use up to " + ((((double) this.limit) / 1024.0d) / 1024.0d) + "MB");
    }

    public Bitmap get(String id) {
        try {
            if (this.cache.containsKey(id)) {
                return (Bitmap) this.cache.get(id);
            }
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void put(String id, Bitmap bitmap) {
        try {
            if (this.cache.containsKey(id)) {
                this.size -= getSizeInBytes((Bitmap) this.cache.get(id));
            }
            this.cache.put(id, bitmap);
            this.size += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void checkSize() {
        Log.i(TAG, "cache size=" + this.size + " length=" + this.cache.size());
        if (this.size > this.limit) {
            Iterator<Entry<String, Bitmap>> iter = this.cache.entrySet().iterator();
            while (iter.hasNext()) {
                this.size -= getSizeInBytes((Bitmap) ((Entry) iter.next()).getValue());
                iter.remove();
                if (this.size <= this.limit) {
                    break;
                }
            }
            Log.i(TAG, "Clean cache. New size " + this.cache.size());
        }
    }

    public void clear() {
        this.cache.clear();
    }

    long getSizeInBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        return (long) (bitmap.getRowBytes() * bitmap.getHeight());
    }
}
