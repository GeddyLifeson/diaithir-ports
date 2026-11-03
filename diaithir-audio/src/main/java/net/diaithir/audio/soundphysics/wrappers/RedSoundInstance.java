// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok @Delegate replaced by explicit delegation to the wrapped SoundInstance.
package net.diaithir.audio.soundphysics.wrappers;

import java.util.Objects;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

public class RedSoundInstance implements SoundInstance {
    public final SoundInstance original;

    public RedSoundInstance(SoundInstance original) {
        this.original = Objects.requireNonNull(original);
    }

    public SoundInstance getOriginal() {
        return original;
    }

    @Override public Identifier getIdentifier() { return original.getIdentifier(); }
    @Override public WeighedSoundEvents resolve(SoundManager manager) { return original.resolve(manager); }
    @Override public Sound getSound() { return original.getSound(); }
    @Override public SoundSource getSource() { return original.getSource(); }
    @Override public boolean isLooping() { return original.isLooping(); }
    @Override public boolean isRelative() { return original.isRelative(); }
    @Override public int getDelay() { return original.getDelay(); }
    @Override public float getVolume() { return original.getVolume(); }
    @Override public float getPitch() { return original.getPitch(); }
    @Override public double getX() { return original.getX(); }
    @Override public double getY() { return original.getY(); }
    @Override public double getZ() { return original.getZ(); }
    @Override public Attenuation getAttenuation() { return original.getAttenuation(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedSoundInstance r)) return false;
        return original.equals(r.original);
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }

    @Override
    public String toString() {
        return "RedSoundInstance(original=" + original + ")";
    }
}
