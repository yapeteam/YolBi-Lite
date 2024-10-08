package cn.yapeteam.yolbi.utils;

public class StopWatch {
    public long millis;

    public StopWatch() {
        reset();
    }

    public boolean finished(long delay) {
        return System.currentTimeMillis() - delay >= millis;
    }

    public void reset() {
        this.millis = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }
}
