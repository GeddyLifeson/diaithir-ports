// Ported from Extreme Sound Muffler (LGPL-3.0-only) https://github.com/LeoBeliik/ExtremeSoundMuffler
// Modifications: Wrapper SoundInstance that scales volume, replacing the SoundEngine#calculateVolume mixin.
package net.diaithir.audio.soundmuffler.client;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

public class ScaledSound implements SoundInstance {
    protected final SoundInstance delegate;
    protected final float scale;

    public ScaledSound(SoundInstance delegate, float scale) {
        this.delegate = delegate;
        this.scale = scale;
    }

    @Override public Identifier getIdentifier() { return delegate.getIdentifier(); }
    @Override public @Nullable WeighedSoundEvents resolve(SoundManager manager) { return delegate.resolve(manager); }
    @Override public Sound getSound() { return delegate.getSound(); }
    @Override public SoundSource getSource() { return delegate.getSource(); }
    @Override public boolean isLooping() { return delegate.isLooping(); }
    @Override public boolean isRelative() { return delegate.isRelative(); }
    @Override public int getDelay() { return delegate.getDelay(); }
    @Override public float getVolume() { return delegate.getVolume() * scale; }
    @Override public float getPitch() { return delegate.getPitch(); }
    @Override public double getX() { return delegate.getX(); }
    @Override public double getY() { return delegate.getY(); }
    @Override public double getZ() { return delegate.getZ(); }
    @Override public Attenuation getAttenuation() { return delegate.getAttenuation(); }
    @Override public boolean canStartSilent() { return delegate.canStartSilent(); }
    @Override public boolean canPlaySound() { return delegate.canPlaySound(); }
}
