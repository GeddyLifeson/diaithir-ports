// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok removed; equals/hashCode written by hand (identity of sound + pos + id).
package net.diaithir.audio.soundphysics.data;

import java.util.Objects;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.phys.Vec3;

public class SoundData {
    public final SoundInstance sound;
    public final Vec3 position;
    public final String soundId;

    public SoundData(SoundInstance sound, Vec3 position, String soundId) {
        this.sound = sound;
        this.position = position;
        this.soundId = soundId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        SoundData other = (SoundData) o;
        return Objects.equals(sound, other.sound) && Objects.equals(position, other.position) && Objects.equals(soundId, other.soundId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sound, position, soundId);
    }
}
