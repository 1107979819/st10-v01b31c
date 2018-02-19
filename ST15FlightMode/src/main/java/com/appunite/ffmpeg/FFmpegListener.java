package com.appunite.ffmpeg;

public interface FFmpegListener {
    void onFFDataSourceLoaded(FFmpegError fFmpegError, FFmpegStreamInfo[] fFmpegStreamInfoArr);

    void onFFPause(NotPlayingException notPlayingException);

    void onFFResume(NotPlayingException notPlayingException);

    void onFFSeeked(NotPlayingException notPlayingException);

    void onFFStop();

    void onFFUpdateTime(long j, long j2, boolean z);
}
