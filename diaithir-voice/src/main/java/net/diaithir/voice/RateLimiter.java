package net.diaithir.voice;

/** Token bucket so one client cannot make the server relay more than a talker could produce. */
public final class RateLimiter {
    private final double perSecond;
    private final double burst;
    private double tokens;
    private long lastNanos;

    public RateLimiter(double perSecond, double burst) {
        this.perSecond = perSecond;
        this.burst = burst;
        this.tokens = burst;
    }

    public boolean tryAcquire(long nowNanos) {
        if (lastNanos != 0) tokens = Math.min(burst, tokens + (nowNanos - lastNanos) / 1e9 * perSecond);
        lastNanos = nowNanos;
        if (tokens < 1) return false;
        tokens -= 1;
        return true;
    }
}
