// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** A stack on its way along a fixed route of pipe positions. idx is the pipe it is in; progress counts ticks inside it. */
public final class Traveller {
    public static final Codec<Traveller> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(t -> t.stack),
            Codec.LONG.listOf().fieldOf("path").forGetter(t -> t.path),
            Codec.INT.fieldOf("idx").forGetter(t -> t.idx),
            Codec.INT.fieldOf("progress").forGetter(t -> t.progress),
            Codec.INT.fieldOf("tries").forGetter(t -> t.tries)).apply(i, Traveller::new));

    public final ItemStack stack;
    public final List<Long> path;
    public int idx;
    public int progress;
    public int tries;
    long movedAt = -1;

    public Traveller(ItemStack stack, List<Long> path, int idx, int progress, int tries) {
        this.stack = stack;
        this.path = path;
        this.idx = idx;
        this.progress = progress;
        this.tries = tries;
    }

    public boolean atEnd() {
        return idx >= path.size() - 1;
    }

    public long next() {
        return path.get(idx + 1);
    }
}
