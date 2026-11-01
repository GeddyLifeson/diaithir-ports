package com.github.alexmodguy.alexscaves.client.sound;

import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.sounds.Music;

public class ACMusics {
    /** Boss music; the sound event is bound lazily because the registry holder is not available at class load. */
    public static Music luxtructosaurusBossMusic() {
        return new Music(ACSoundRegistry.LUXTRUCTOSAURUS_BOSS_MUSIC, 0, 0, true);
    }
}
