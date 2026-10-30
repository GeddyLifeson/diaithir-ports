package com.github.alexthe666.citadel.client.render.legacy;

import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Ambient recording buffer for legacy code that reached for Minecraft.getInstance().renderBuffers().bufferSource().
 * Valid only while a legacy render callback runs.
 */
public final class LegacyBuffers {
    private static RecordingBufferSource current;

    private LegacyBuffers() {
    }

    public static void set(RecordingBufferSource source) {
        current = source;
    }

    static RecordingBufferSource peek() {
        return current;
    }

    public static RecordingBufferSource current() {
        if (current == null) throw new IllegalStateException("No legacy render in progress");
        return current;
    }

    public static MultiBufferSource source() {
        return current();
    }

    /** Old code called endBatch() after drawing; recording needs nothing. */
    public static void endBatch(MultiBufferSource ignored) {
    }
}
