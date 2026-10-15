package com.buuz135.industrial.proxy.client.sound;

import com.buuz135.industrial.entity.InfinityNukeEntity;
import com.buuz135.industrial.module.ModuleTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;

import java.util.Map;
import java.util.WeakHashMap;

/** The nuke's client sounds, kept out of the entity so a dedicated server never loads client sound classes. */
public final class NukeSounds {
    private static final Map<InfinityNukeEntity, TickeableSound[]> SOUNDS = new WeakHashMap<>();

    private NukeSounds() {}

    public static void tick(InfinityNukeEntity nuke) {
        TickeableSound[] sounds = SOUNDS.computeIfAbsent(nuke, n -> new TickeableSound[2]);
        if (sounds[0] == null && nuke.isExplodingSynced()) {
            Minecraft.getInstance().getSoundManager().play(sounds[0] = new TickeableSound(nuke.level(), nuke.blockPosition(), ModuleTool.NUKE_CHARGING.get(), nuke.getRadius(), 10, nuke.level().getRandom()));
        }
        if (sounds[0] != null) {
            sounds[0].setDistance(nuke.getRadius());
            sounds[0].increase();
            if (!Minecraft.getInstance().getSoundManager().isActive(sounds[0]) && sounds[1] == null) {
                sounds[1] = new TickeableSound(nuke.level(), nuke.blockPosition(), ModuleTool.NUKE_EXPLOSION.get(), nuke.getRadius(), 10, nuke.level().getRandom());
                sounds[1].setPitch(1);
                Minecraft.getInstance().getSoundManager().play(sounds[1]);
            }
        }
    }

    public static void arm(InfinityNukeEntity nuke) {
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(ModuleTool.NUKE_ARMING.get(), SoundSource.BLOCKS, 1, 1, nuke.level().getRandom(), nuke.blockPosition()));
    }
}
