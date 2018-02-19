package com.appunite.ffmpeg;

public class FFmpegError extends Throwable {
    private static final long serialVersionUID = 1;

    public FFmpegError(int err) {
        super(String.format("FFmpegPlayer error %d", new Object[]{Integer.valueOf(err)}));
    }
}
