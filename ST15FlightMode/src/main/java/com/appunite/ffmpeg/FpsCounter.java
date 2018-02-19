package com.appunite.ffmpeg;

public class FpsCounter {
    private int counter = 0;
    private final int frameCount;
    boolean start = true;
    private long startTime = 0;
    private String tick = "- fps";

    public FpsCounter(int frameCount) {
        this.frameCount = frameCount;
    }

    public String tick() {
        if (this.start) {
            this.start = false;
            this.startTime = System.nanoTime();
        }
        int i = this.counter;
        this.counter = i + 1;
        if (i < this.frameCount) {
            return this.tick;
        }
        long stopTime = System.nanoTime();
        double fps = (((double) this.frameCount) * 1.0E9d) / ((double) (stopTime - this.startTime));
        this.startTime = stopTime;
        this.counter = 0;
        this.tick = String.format("%.2f fps", new Object[]{Double.valueOf(fps)});
        return this.tick;
    }
}
