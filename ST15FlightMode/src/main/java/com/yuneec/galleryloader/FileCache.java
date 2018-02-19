package com.yuneec.galleryloader;

import android.content.Context;

public class FileCache extends AbstractFileCache {
    public FileCache(Context context) {
        super(context);
    }

    public String getSavePath(String url) {
        return getCacheDir() + String.valueOf(url.hashCode());
    }

    public String getCacheDir() {
        return FileManager.getCacheFilePath();
    }
}
