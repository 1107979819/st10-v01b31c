package com.appunite.ffmpeg;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JniReader {
    private static final String TAG = JniReader.class.getCanonicalName();
    private int position;
    private byte[] value = new byte[16];

    public JniReader(String url, int flags) {
        Log.d(TAG, String.format("Reading: %s", new Object[]{url}));
        try {
            byte[] key = "dupadupadupadupa".getBytes("UTF-8");
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(key);
            System.arraycopy(m.digest(), 0, this.value, 0, 16);
            this.position = 0;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e2) {
            throw new RuntimeException(e2);
        }
    }

    public int read(byte[] buffer) {
        int end = this.position + buffer.length;
        if (end >= this.value.length) {
            end = this.value.length;
        }
        int length = end - this.position;
        System.arraycopy(this.value, this.position, buffer, 0, length);
        this.position += length;
        return length;
    }

    public int write(byte[] buffer) {
        return 0;
    }

    public int check(int mask) {
        return 0;
    }

    public long seek(long pos, int whence) {
        return -1;
    }
}
