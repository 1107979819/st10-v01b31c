package com.yuneec.galleryloader;

import android.content.Context;
import android.util.Log;
import java.io.File;

public abstract class AbstractFileCache {
    private String dirString = getCacheDir();

    public abstract String getCacheDir();

    public abstract String getSavePath(String str);

    public AbstractFileCache(Context context) {
        Log.e("", "FileHelper.createDirectory:" + this.dirString + ", ret = " + FileHelper.createDirectory(this.dirString));
    }

    public File getFile(String url) {
        return new File(getSavePath(url));
    }

    public void clear() {
        FileHelper.deleteDirectory(this.dirString);
    }
}
