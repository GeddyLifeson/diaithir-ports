// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

/** What a pipe does besides carrying items. Hop time is ticks per block. */
public enum PipeKind {
    PLAIN(20), EXTRACTOR(20), SINK(20), SPEED(5);

    private final int hopTicks;

    PipeKind(int hopTicks) {
        this.hopTicks = hopTicks;
    }

    public int hopTicks() {
        return hopTicks;
    }
}
