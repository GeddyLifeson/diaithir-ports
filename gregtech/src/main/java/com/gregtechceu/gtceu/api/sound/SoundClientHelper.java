package com.gregtechceu.gtceu.api.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.function.BooleanSupplier;

/** Client-only helper, kept separate so {@link SoundEntry} can be verified on a dedicated server. */
final class SoundClientHelper {

    private SoundClientHelper() {}

    static Object play(SoundEntry entry, BooleanSupplier predicate, BlockPos pos, boolean loop, int delay,
                       float volume, float pitch) {
        var sound = new AutoReleasedSound(entry, predicate, pos, loop, delay, volume, pitch);
        Minecraft.getInstance().getSoundManager().play(sound);
        return sound;
    }
}
