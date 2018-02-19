package org.videolan.libvlc;

import android.media.AudioTrack;
import android.util.Log;

public class Aout {
    private static final String TAG = "LibVLC/aout";
    private AudioTrack mAudioTrack;

    public void init(int sampleRateInHz, int channels, int samples) {
        Log.d(TAG, new StringBuilder(String.valueOf(sampleRateInHz)).append(", ").append(channels).append(", ").append(samples).append("=>").append(channels * samples).toString());
        this.mAudioTrack = new AudioTrack(3, sampleRateInHz, 12, 2, Math.max(AudioTrack.getMinBufferSize(sampleRateInHz, 12, 2), (channels * samples) * 2), 1);
    }

    public void release() {
        if (this.mAudioTrack != null) {
            this.mAudioTrack.release();
        }
        this.mAudioTrack = null;
    }

    public void playBuffer(byte[] audioData, int bufferSize) {
        if (this.mAudioTrack.getState() != 0) {
            if (this.mAudioTrack.write(audioData, 0, bufferSize) != bufferSize) {
                Log.w(TAG, "Could not write all the samples to the audio device");
            }
            this.mAudioTrack.play();
        }
    }

    public void pause() {
        this.mAudioTrack.pause();
    }
}
