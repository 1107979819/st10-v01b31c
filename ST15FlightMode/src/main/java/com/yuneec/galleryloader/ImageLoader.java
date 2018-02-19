package com.yuneec.galleryloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.widget.ImageView;
import com.yuneec.flightmode15.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private ExecutorService executorService;
    private AbstractFileCache fileCache;
    private Map<Integer, String> imageViews = Collections.synchronizedMap(new WeakHashMap());
    private MemoryCache memoryCache = new MemoryCache();

    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            this.bitmap = b;
            this.photoToLoad = p;
        }

        public void run() {
            if (!ImageLoader.this.imageViewReused(this.photoToLoad) && this.bitmap != null) {
                ImageView imageView = this.photoToLoad.imageView;
                Drawable drawable = new BitmapDrawable(imageView.getResources(), this.bitmap);
                TransitionDrawable td = new TransitionDrawable(new Drawable[]{new ColorDrawable(17170445), drawable});
                imageView.setImageDrawable(td);
                td.startTransition(1000);
            }
        }
    }

    private class PhotoToLoad {
        public ImageView imageView;
        public String url;

        public PhotoToLoad(String u, ImageView i) {
            this.url = u;
            this.imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        public void run() {
            if (!ImageLoader.this.imageViewReused(this.photoToLoad)) {
                Bitmap bmp = ImageLoader.this.getBitmap(this.photoToLoad.url);
                ImageLoader.this.memoryCache.put(this.photoToLoad.url, bmp);
                if (!ImageLoader.this.imageViewReused(this.photoToLoad)) {
                    ((Activity) this.photoToLoad.imageView.getContext()).runOnUiThread(new BitmapDisplayer(bmp, this.photoToLoad));
                }
            }
        }
    }

    public ImageLoader(Context context) {
        this.fileCache = new FileCache(context);
        this.executorService = Executors.newFixedThreadPool(5);
    }

    public void DisplayImage(String url, ImageView imageView, boolean isLoadOnlyFromCache) {
        this.imageViews.put(Integer.valueOf(((Integer) imageView.getTag()).intValue()), url);
        Bitmap bitmap = this.memoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (!isLoadOnlyFromCache) {
            imageView.setImageResource(R.drawable.gallery_photo_loading);
            queuePhoto(url, imageView);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        this.executorService.submit(new PhotosLoader(new PhotoToLoad(url, imageView)));
    }

    public File getFileCache(String url) {
        File f = this.fileCache.getFile(url);
        return (f == null || !f.exists()) ? null : f;
    }

    private Bitmap getBitmap(String url) {
        File f = this.fileCache.getFile(url);
        Bitmap b = null;
        if (f != null && f.exists()) {
            b = decodeFile(f);
        }
        if (b != null) {
            return b;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            CopyStream(is, os);
            os.close();
            return decodeFile(f);
        } catch (Exception ex) {
            Log.e("", "getBitmap catch Exception...\nmessage = " + ex.getMessage());
            return null;
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            Options o = new Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            int width_tmp = o.outWidth;
            int height_tmp = o.outHeight;
            int scale = 1;
            while (width_tmp / 2 >= 100 && height_tmp / 2 >= 100) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            Options o2 = new Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String load_url = (String) this.imageViews.get(Integer.valueOf(((Integer) photoToLoad.imageView.getTag()).intValue()));
        if (load_url == null || !load_url.equals(photoToLoad.url)) {
            return true;
        }
        return false;
    }

    public void clearCache() {
        this.memoryCache.clear();
        this.fileCache.clear();
    }

    public void clearMemoryCache() {
        this.memoryCache.clear();
    }

    public void clearFileCache() {
        this.fileCache.clear();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        try {
            byte[] bytes = new byte[51200];
            while (true) {
                int count = is.read(bytes, 0, 51200);
                if (count != -1) {
                    os.write(bytes, 0, count);
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("", "CopyStream catch Exception...");
        }
    }
}
