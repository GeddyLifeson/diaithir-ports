package net.diaithir.voice.client;

import java.util.ArrayDeque;

/**
 * Holds decoded frames until a few have arrived, then releases one per 20 ms tick. TCP delivers in bursts after a stall,
 * so the queue is capped: when it grows past {@code maxDepth} the oldest frames are dropped and the speaker jumps forward
 * instead of falling further behind. Not thread safe on its own; callers synchronise.
 */
public final class Jitter<T> {
    private final ArrayDeque<T> queue = new ArrayDeque<>();
    private final int prefill;
    private final int maxDepth;
    private boolean playing;
    public int dropped;
    public int underruns;

    public Jitter(int prefill, int maxDepth) {
        this.prefill = prefill;
        this.maxDepth = Math.max(maxDepth, prefill + 1);
    }

    public void push(T frame) {
        queue.addLast(frame);
        while (queue.size() > maxDepth) {
            queue.removeFirst();
            dropped++;
        }
    }

    /** Next frame to play, or null for silence. */
    public T pop() {
        if (!playing) {
            if (queue.size() < prefill) return null;
            playing = true;
        }
        T f = queue.pollFirst();
        if (f == null) {
            playing = false;
            underruns++;
        }
        return f;
    }

    public int depth() {
        return queue.size();
    }
}
