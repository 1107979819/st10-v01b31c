package org.videolan.libvlc;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.Surface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class LibVLC {
    public static final int AOUT_AUDIOTRACK = 1;
    public static final int AOUT_AUDIOTRACK_JAVA = 0;
    public static final int AOUT_OPENSLES = 2;
    private static final String TAG = "VLC/LibVLC";
    private static LibVLC sInstance;
    private int aout;
    private String chroma;
    private boolean iomx = false;
    private Aout mAout;
    private StringBuffer mDebugLogBuffer;
    private long mInternalMediaPlayerInstance = 0;
    private boolean mIsBufferingLog = false;
    private boolean mIsInitialized;
    private long mLibVlcInstance = 0;
    private long mMediaListInstance = 0;
    private long mMediaListPlayerInstance = 0;
    private String subtitlesEncoding = "";
    private boolean timeStretching;
    private boolean verboseMode;

    private native void detachEventHandler();

    private native long getLengthFromLocation(long j, String str);

    private native byte[] getThumbnail(long j, String str, int i, int i2);

    private native boolean hasVideoTrack(long j, String str);

    private native void nativeDestroy();

    private native void nativeInit() throws LibVlcException;

    public static native boolean nativeIsPathDirectory(String str);

    public static native void nativeReadDirectory(String str, ArrayList<String> arrayList);

    public static native String nativeToURI(String str);

    private native void playIndex(long j, int i);

    private native int readMedia(long j, String str, boolean z);

    private native String[] readMediaMeta(long j, String str);

    private native TrackInfo[] readTracksInfo(long j, String str);

    private native void setEventHandler(EventHandler eventHandler);

    public native void attachSurface(Surface surface, IVideoPlayer iVideoPlayer, int i, int i2);

    public native boolean canRecord();

    public native String changeset();

    public native String compiler();

    public native void detachSurface();

    public native int getAudioTrack();

    public native Map<Integer, String> getAudioTrackDescription();

    public native int getAudioTracksCount();

    public native long getLength();

    public native void getMediaListItems(ArrayList<String> arrayList);

    public native float getPosition();

    public native float getRate();

    public native int getSpuTrack();

    public native Map<Integer, String> getSpuTrackDescription();

    public native int getSpuTracksCount();

    public native long getTime();

    public native int getVideoTracksCount();

    public native int getVolume();

    public native boolean hasMediaPlayer();

    public native boolean isPlaying();

    public native boolean isRecording();

    public native boolean isSeekable();

    public native void next();

    public native void pause();

    public native void play();

    public native void previous();

    public native TrackInfo[] readTracksInfoPosition(int i);

    public native int setAudioTrack(int i);

    public native void setPosition(float f);

    public native void setRate(float f);

    public native int setSpuTrack(int i);

    public native void setSurface(Surface surface);

    public native long setTime(long j);

    public native int setVolume(int i);

    public native int snapShot(int i, String str, int i2, int i3);

    public native void startDebugBuffer();

    public native int startRecord(String str);

    public native void stop();

    public native void stopDebugBuffer();

    public native int stopRecord();

    public native String version();

    static {
        try {
            if (VERSION.SDK_INT <= 10) {
                System.loadLibrary("iomx-gingerbread");
            } else if (VERSION.SDK_INT <= 13) {
                System.loadLibrary("iomx-hc");
            } else {
                System.loadLibrary("iomx-ics");
            }
        } catch (Throwable t) {
            Log.w(TAG, "Unable to load the iomx library: " + t);
        }
        try {
            System.loadLibrary("vlcjni");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Can't load vlcjni library: " + ule);
            System.exit(1);
        } catch (SecurityException se) {
            Log.e(TAG, "Encountered a security issue when loading vlcjni library: " + se);
            System.exit(1);
        }
    }

    public static LibVLC getInstance() throws LibVlcException {
        synchronized (LibVLC.class) {
            if (sInstance == null) {
                sInstance = new LibVLC();
            }
        }
        return sInstance;
    }

    public static LibVLC getExistingInstance() {
        LibVLC libVLC;
        synchronized (LibVLC.class) {
            libVLC = sInstance;
        }
        return libVLC;
    }

    private LibVLC() {
        this.aout = LibVlcUtil.isGingerbreadOrLater() ? 2 : 0;
        this.timeStretching = false;
        this.chroma = "";
        this.verboseMode = true;
        this.mIsInitialized = false;
        this.mAout = new Aout();
    }

    public void finalize() {
        if (this.mLibVlcInstance != 0) {
            Log.d(TAG, "LibVLC is was destroyed yet before finalize()");
            destroy();
        }
    }

    public static synchronized void restart(Context context) {
        synchronized (LibVLC.class) {
            if (sInstance != null) {
                try {
                    sInstance.destroy();
                    sInstance.init(context);
                } catch (LibVlcException lve) {
                    Log.e(TAG, "Unable to reinit libvlc: " + lve);
                }
            }
        }
    }

    public boolean useIOMX() {
        return this.iomx;
    }

    public void setIomx(boolean iomx) {
        this.iomx = iomx;
    }

    public String getSubtitlesEncoding() {
        return this.subtitlesEncoding;
    }

    public void setSubtitlesEncoding(String subtitlesEncoding) {
        this.subtitlesEncoding = subtitlesEncoding;
    }

    public int getAout() {
        return this.aout;
    }

    public void setAout(int aout) {
        if (aout < 0) {
            this.aout = LibVlcUtil.isGingerbreadOrLater() ? 2 : 0;
        } else {
            this.aout = aout;
        }
    }

    public boolean timeStretchingEnabled() {
        return this.timeStretching;
    }

    public void setTimeStretching(boolean timeStretching) {
        this.timeStretching = timeStretching;
    }

    public String getChroma() {
        return this.chroma;
    }

    public void setChroma(String chroma) {
        if (chroma.equals("YV12") && !LibVlcUtil.isGingerbreadOrLater()) {
            chroma = "";
        }
        this.chroma = chroma;
    }

    public boolean isVerboseMode() {
        return this.verboseMode;
    }

    public void setVerboseMode(boolean verboseMode) {
        this.verboseMode = verboseMode;
    }

    public void init(Context context) throws LibVlcException {
        Log.v(TAG, "Initializing LibVLC");
        this.mDebugLogBuffer = new StringBuffer();
        if (!this.mIsInitialized) {
            if (LibVlcUtil.hasCompatibleCPU(context)) {
                nativeInit();
                setEventHandler(EventHandler.getInstance());
                this.mIsInitialized = true;
                return;
            }
            Log.e(TAG, LibVlcUtil.getErrorMsg());
            throw new LibVlcException();
        }
    }

    public void destroy() {
        Log.v(TAG, "Destroying LibVLC instance");
        nativeDestroy();
        detachEventHandler();
        this.mIsInitialized = false;
    }

    public void initAout(int sampleRateInHz, int channels, int samples) {
        Log.d(TAG, "Opening the java audio output");
        this.mAout.init(sampleRateInHz, channels, samples);
    }

    public void playAudio(byte[] audioData, int bufferSize) {
        this.mAout.playBuffer(audioData, bufferSize);
    }

    public void pauseAout() {
        Log.d(TAG, "Pausing the java audio output");
        this.mAout.pause();
    }

    public void closeAout() {
        Log.d(TAG, "Closing the java audio output");
        this.mAout.release();
    }

    public void readMedia(String mrl) {
        readMedia(this.mLibVlcInstance, mrl, false);
    }

    public int readMedia(String mrl, boolean novideo) {
        Log.v(TAG, "Reading " + mrl);
        return readMedia(this.mLibVlcInstance, mrl, novideo);
    }

    public void playIndex(int position) {
        playIndex(this.mLibVlcInstance, position);
    }

    public String[] readMediaMeta(String mrl) {
        return readMediaMeta(this.mLibVlcInstance, mrl);
    }

    public TrackInfo[] readTracksInfo(String mrl) {
        return readTracksInfo(this.mLibVlcInstance, mrl);
    }

    public byte[] getThumbnail(String mrl, int i_width, int i_height) {
        return getThumbnail(this.mLibVlcInstance, mrl, i_width, i_height);
    }

    public boolean hasVideoTrack(String mrl) throws IOException {
        return hasVideoTrack(this.mLibVlcInstance, mrl);
    }

    public long getLengthFromLocation(String mrl) {
        return getLengthFromLocation(this.mLibVlcInstance, mrl);
    }

    public String getBufferContent() {
        return this.mDebugLogBuffer.toString();
    }

    public void clearBuffer() {
        this.mDebugLogBuffer.setLength(0);
    }

    public boolean isDebugBuffering() {
        return this.mIsBufferingLog;
    }

    public static String PathToURI(String path) {
        if (path != null) {
            return nativeToURI(path);
        }
        throw new NullPointerException("Cannot convert null path!");
    }
}
