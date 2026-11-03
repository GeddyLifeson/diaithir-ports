// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: package moved.
package net.diaithir.audio.soundphysics.data;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.phys.Vec3;

public class TickableSoundData extends SoundData {
    public TickableSoundData(SoundInstance sound, Vec3 position, String soundId) {
        super(sound, position, soundId);
    }
}
