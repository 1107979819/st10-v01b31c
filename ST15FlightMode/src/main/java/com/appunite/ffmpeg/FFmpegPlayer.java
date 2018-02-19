package com.appunite.ffmpeg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.view.Surface;
import java.util.HashMap;
import java.util.Map;

public class FFmpegPlayer {
    public static final int NO_STREAM = -2;
    public static final int UNKNOWN_STREAM = -1;
    private final Activity activity;
    private long mCurrentTimeUs;
    private boolean mIsFinished = false;
    private int mNativePlayer;
    private final RenderedFrame mRenderedFrame = new RenderedFrame();
    private FFmpegStreamInfo[] mStreamsInfos = null;
    private long mVideoDurationUs;
    private FFmpegListener mpegListener = null;
    private String previousUrl;
    private SetDataSourceTask setDataSourceTask;
    private Runnable updateTimeRunnable = new Runnable() {
        public void run() {
            if (FFmpegPlayer.this.mpegListener != null) {
                FFmpegPlayer.this.mpegListener.onFFUpdateTime(FFmpegPlayer.this.mCurrentTimeUs, FFmpegPlayer.this.mVideoDurationUs, FFmpegPlayer.this.mIsFinished);
            }
            if (FFmpegPlayer.this.mIsFinished) {
                FFmpegPlayer.this.stop();
            }
        }
    };

    private static class PauseTask extends AsyncTask<Void, Void, NotPlayingException> {
        private final FFmpegPlayer player;

        public PauseTask(FFmpegPlayer player) {
            this.player = player;
        }

        protected NotPlayingException doInBackground(Void... params) {
            try {
                this.player.pauseNative();
                return null;
            } catch (NotPlayingException e) {
                return e;
            }
        }

        protected void onPostExecute(NotPlayingException result) {
            if (this.player.mpegListener != null) {
                this.player.mpegListener.onFFPause(result);
            }
        }
    }

    static class RenderedFrame {
        public Bitmap bitmap;
        public int height;
        public int width;

        RenderedFrame() {
        }
    }

    private static class ResumeTask extends AsyncTask<Void, Void, NotPlayingException> {
        private final FFmpegPlayer player;

        public ResumeTask(FFmpegPlayer player) {
            this.player = player;
        }

        protected NotPlayingException doInBackground(Void... params) {
            try {
                this.player.resumeNative();
                return null;
            } catch (NotPlayingException e) {
                return e;
            }
        }

        protected void onPostExecute(NotPlayingException result) {
            if (this.player.mpegListener != null) {
                this.player.mpegListener.onFFResume(result);
            }
        }
    }

    private static class SeekTask extends AsyncTask<Long, Void, NotPlayingException> {
        private final FFmpegPlayer player;

        public SeekTask(FFmpegPlayer player) {
            this.player = player;
        }

        protected NotPlayingException doInBackground(Long... params) {
            try {
                this.player.seekNative(params[0].longValue());
                return null;
            } catch (NotPlayingException e) {
                return e;
            }
        }

        protected void onPostExecute(NotPlayingException result) {
            if (this.player.mpegListener != null) {
                this.player.mpegListener.onFFSeeked(result);
            }
        }
    }

    private static class SetDataSourceTask extends AsyncTask<Object, Void, SetDataSourceTaskResult> {
        private final FFmpegPlayer player;

        public SetDataSourceTask(FFmpegPlayer player) {
            this.player = player;
        }

        protected SetDataSourceTaskResult doInBackground(Object... params) {
            int subtitleStreamNo = -1;
            String url = params[0];
            Map<String, String> map = params[1];
            Integer videoStream = params[2];
            Integer audioStream = params[3];
            Integer subtitleStream = params[4];
            int videoStreamNo = videoStream == null ? -1 : videoStream.intValue();
            int audioStreamNo = audioStream == null ? -1 : audioStream.intValue();
            if (subtitleStream != null) {
                subtitleStreamNo = subtitleStream.intValue();
            }
            int err = this.player.setDataSourceNative(url, map, videoStreamNo, audioStreamNo, subtitleStreamNo);
            SetDataSourceTaskResult result = new SetDataSourceTaskResult();
            if (err < 0) {
                result.error = new FFmpegError(err);
                result.streams = null;
            } else {
                result.error = null;
                result.streams = this.player.getStreamsInfo();
            }
            return result;
        }

        protected void onPostExecute(SetDataSourceTaskResult result) {
            if (this.player.mpegListener != null) {
                this.player.mpegListener.onFFDataSourceLoaded(result.error, result.streams);
            }
        }
    }

    private static class SetDataSourceTaskResult {
        FFmpegError error;
        FFmpegStreamInfo[] streams;

        private SetDataSourceTaskResult() {
        }
    }

    private static class StopTask extends AsyncTask<Void, Void, Void> {
        private final FFmpegPlayer player;

        public StopTask(FFmpegPlayer player) {
            this.player = player;
        }

        protected Void doInBackground(Void... params) {
            this.player.stopNative();
            return null;
        }

        protected void onPostExecute(Void result) {
            if (this.player.mpegListener != null) {
                this.player.mpegListener.onFFStop();
            }
        }
    }

    private native void deallocNative();

    private native long getVideoDurationNative();

    private native int initNative();

    private native int isPlayingNative();

    private native int isRecordingNative();

    private native void pauseNative() throws NotPlayingException;

    private native void resumeNative() throws NotPlayingException;

    private native void seekNative(long j) throws NotPlayingException;

    private native int setDataSourceNative(String str, Map<String, String> map, int i, int i2, int i3);

    private native int startRecordNative(String str);

    private native void stopNative();

    private native int stopRecordNative();

    public native void render(Surface surface);

    public native void renderFrameStart();

    public native void renderFrameStop();

    static {
        if (new NativeTester().isNeon()) {
            System.loadLibrary("ffmpeg-neon");
            System.loadLibrary("ffmpeg-jni-neon");
            return;
        }
        System.loadLibrary("ffmpeg");
        System.loadLibrary("ffmpeg-jni");
    }

    public FFmpegPlayer(FFmpegDisplay videoView, Activity activity) {
        this.activity = activity;
        if (initNative() != 0) {
            throw new RuntimeException(String.format("Could not initialize player: %d", new Object[]{Integer.valueOf(initNative())}));
        } else if (videoView != null) {
            videoView.setMpegPlayer(this);
        }
    }

    protected void finalize() throws Throwable {
        deallocNative();
        super.finalize();
    }

    private void setStreamsInfo(FFmpegStreamInfo[] streamsInfos) {
        this.mStreamsInfos = streamsInfos;
    }

    protected FFmpegStreamInfo[] getStreamsInfo() {
        return this.mStreamsInfos;
    }

    public void stop() {
        new StopTask(this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Void[0]);
    }

    public void pause() {
        new PauseTask(this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Void[0]);
    }

    public void seek(long positionUs) {
        new SeekTask(this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Long[]{Long.valueOf(positionUs)});
    }

    public void resume() {
        new ResumeTask(this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Void[0]);
    }

    public boolean isPlaying() {
        return isPlayingNative() == 1;
    }

    public boolean isRecording() {
        return isRecordingNative() == 1;
    }

    public int startRecord(String file_path) {
        return startRecordNative(file_path);
    }

    public int stopRecord() {
        return stopRecordNative();
    }

    private Bitmap prepareFrame(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        this.mRenderedFrame.height = height;
        this.mRenderedFrame.width = width;
        return bitmap;
    }

    private void onUpdateTime(long currentUs, long maxUs, boolean isFinished) {
        this.mCurrentTimeUs = currentUs;
        this.mVideoDurationUs = maxUs;
        this.mIsFinished = isFinished;
        this.activity.runOnUiThread(this.updateTimeRunnable);
    }

    private AudioTrack prepareAudioTrack(int sampleRateInHz, int numberOfChannels) {
        int channelConfig;
        while (true) {
            if (numberOfChannels == 1) {
                channelConfig = 4;
            } else if (numberOfChannels == 2) {
                channelConfig = 12;
            } else if (numberOfChannels == 3) {
                channelConfig = 28;
            } else if (numberOfChannels == 4) {
                channelConfig = 204;
            } else if (numberOfChannels == 5) {
                channelConfig = 236;
            } else if (numberOfChannels == 6) {
                channelConfig = 252;
            } else if (numberOfChannels == 8) {
                channelConfig = 1020;
            } else {
                channelConfig = 12;
            }
            try {
                break;
            } catch (IllegalArgumentException e) {
                if (numberOfChannels > 2) {
                    numberOfChannels = 2;
                } else if (numberOfChannels > 1) {
                    numberOfChannels = 1;
                } else {
                    throw e;
                }
            }
        }
        return new AudioTrack(3, sampleRateInHz, channelConfig, 2, AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, 2), 1);
    }

    private void setVideoListener(FFmpegListener mpegListener) {
        setMpegListener(mpegListener);
    }

    public void setDataSource(String url) {
        Map<String, String> dictionaryMap = new HashMap();
        dictionaryMap.put("stimeout", "3000000");
        setDataSource(url, dictionaryMap, -1, -1, -2);
    }

    public void setDataSource(String url, Map<String, String> dictionary, int videoStream, int audioStream, int subtitlesStream) {
        if (this.setDataSourceTask != null) {
            if (!url.equals(this.previousUrl)) {
                this.setDataSourceTask.cancel(true);
            } else if (this.setDataSourceTask.getStatus() == Status.RUNNING) {
                return;
            }
        }
        this.previousUrl = url;
        this.setDataSourceTask = new SetDataSourceTask(this);
        this.setDataSourceTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object[]{url, dictionary, Integer.valueOf(videoStream), Integer.valueOf(audioStream), Integer.valueOf(subtitlesStream)});
    }

    public FFmpegListener getMpegListener() {
        return this.mpegListener;
    }

    public void setMpegListener(FFmpegListener mpegListener) {
        this.mpegListener = mpegListener;
    }
}
