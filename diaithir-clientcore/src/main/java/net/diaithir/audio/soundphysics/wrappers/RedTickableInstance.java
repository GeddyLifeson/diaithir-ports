// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok removed, explicit delegation, config via ModConfigSpec.
package net.diaithir.audio.soundphysics.wrappers;

import java.util.List;
import net.diaithir.audio.soundphysics.RaycastingHelper;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig.AttenuationType;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class RedTickableInstance implements TickableSoundInstance {
    private final Identifier location;
    private final Sound sound;
    private final SoundSource source;
    protected float attenuationMultiplier;
    protected SoundInstance wrapped;
    protected double x;
    protected double y;
    protected double z;
    protected boolean stopped;
    protected float volume;
    protected float pitch;
    protected int tickCount;
    protected Vec3 targetPosition;
    protected float targetVolume;
    private final boolean isBlacklisted;
    protected float originalVolume;
    protected Vec3 originalPosition;

    public RedTickableInstance(Identifier location, Sound sound, SoundSource source, Vec3 position, float volume, float pitch, SoundInstance wrapped, float attenuationMultiplier) {
        this.location = location;
        this.sound = sound;
        this.source = source;
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.stopped = false;
        this.volume = volume;
        this.pitch = pitch;
        this.wrapped = wrapped;
        this.tickCount = 1;
        this.targetPosition = position;
        this.targetVolume = volume;
        this.isBlacklisted = isSoundTickBlacklisted(sound.toString());
        this.originalVolume = wrapped.getVolume();
        this.originalPosition = new Vec3(wrapped.getX(), wrapped.getY(), wrapped.getZ());
        this.attenuationMultiplier = attenuationMultiplier;
    }

    public float getOriginalVolume() { return originalVolume; }
    public Vec3 getOriginalPosition() { return originalPosition; }
    public SoundInstance getWrapped() { return wrapped; }
    public void setAttenuationMultiplier(float m) { this.attenuationMultiplier = m; }
    public void setTargetPosition(Vec3 p) { this.targetPosition = p; }
    public void setTargetVolume(float v) { this.targetVolume = v; }
    public void setVolume(float v) { this.volume = v; }
    public void setStopped(boolean s) { this.stopped = s; }

    @Override
    public boolean isStopped() {
        if (wrapped instanceof TickableSoundInstance t) {
            stopped = t.isStopped();
        }
        return stopped;
    }

    @Override
    public void tick() {
        updateWrapped();
        if (isBlacklisted) return;
        tickCount++;
        int rate = SoundPhysicsConfig.TICK_RATE.get();
        if (stopped || rate == 0) return;
        if (tickCount % rate == 0 && !RaycastingHelper.tickQueue.contains(this)) {
            RaycastingHelper.tickQueue.add(this);
        }
        updatePos();
        updateVolume();
        updatePitch();
    }

    public void updateWrapped() {
        if (wrapped instanceof TickableSoundInstance t) {
            t.tick();
            originalVolume = wrapped.getVolume();
            if (wrapped.getVolume() == 0.0f) {
                volume = 0;
                targetVolume = 0;
            }
            double dx = wrapped.getX() - originalPosition.x;
            double dy = wrapped.getY() - originalPosition.y;
            double dz = wrapped.getZ() - originalPosition.z;
            originalPosition = new Vec3(wrapped.getX(), wrapped.getY(), wrapped.getZ());
            x += dx;
            y += dy;
            z += dz;
            targetPosition = new Vec3(x, y, z);
        }
    }

    @Override
    public Attenuation getAttenuation() {
        return SoundPhysicsConfig.EXPERIMENTAL_REVERB.get() ? Attenuation.LINEAR : Attenuation.NONE;
    }

    public void stop() {
        this.stopped = true;
    }

    public void updatePos() {
        double deltaX = targetPosition.x() - x;
        double deltaY = targetPosition.y() - y;
        double deltaZ = targetPosition.z() - z;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double maxSpeed = 17.15;

        if (distance <= 0.001 || distance > maxSpeed * SoundPhysicsConfig.TICK_RATE.get()) {
            x = targetPosition.x();
            y = targetPosition.y();
            z = targetPosition.z();
            return;
        }

        double moveDistance = Math.min(maxSpeed, distance);
        x += (deltaX / distance) * moveDistance;
        y += (deltaY / distance) * moveDistance;
        z += (deltaZ / distance) * moveDistance;
    }

    public void updateVolume() {
        float deltaVolume = targetVolume - volume;
        if (Math.abs(deltaVolume) <= 0.001f) {
            volume = targetVolume;
            return;
        }
        float ticksToTarget = Math.max(1, SoundPhysicsConfig.TICK_RATE.get());
        float deltaTime = 1.0f / 20.0f;
        float timeToTarget = ticksToTarget * deltaTime;
        float smoothingFactor = 1.0f - (float) Math.pow(0.001, deltaTime / timeToTarget);
        volume += deltaVolume * smoothingFactor;
        if (Math.abs(targetVolume - volume) < 0.001f) {
            volume = targetVolume;
        }
    }

    protected void updatePitch() {
        pitch = wrapped.getPitch();
    }

    @Override
    public Identifier getIdentifier() { return location; }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) { return wrapped.resolve(manager); }

    @Override
    public Sound getSound() { return sound; }

    @Override
    public SoundSource getSource() { return source; }

    @Override
    public boolean isLooping() { return wrapped.isLooping(); }

    @Override
    public boolean isRelative() { return wrapped.isRelative(); }

    @Override
    public int getDelay() { return wrapped.getDelay(); }

    @Override
    public float getPitch() { return pitch; }

    @Override
    public double getX() { return x; }

    @Override
    public double getY() { return y; }

    @Override
    public double getZ() { return z; }

    @Override
    public float getVolume() {
        float vol = volume * SoundPhysicsConfig.VOLUME_MULTIPLIER.get().floatValue();
        if (!SoundPhysicsConfig.EXPERIMENTAL_REVERB.get()) return vol;
        if (attenuationMultiplier == 0) return vol;
        AttenuationType t = SoundPhysicsConfig.ATTENUATION_TYPE.get();
        if (t == AttenuationType.VERCIDIUM_LINEAR || t == AttenuationType.VERCIDIUM_INVERSE_SQUARE) {
            return vol * (1.0f / attenuationMultiplier);
        }
        return vol;
    }

    private static boolean isSoundTickBlacklisted(String soundName) {
        if (soundName == null || soundName.isEmpty()) return false;
        List<? extends String> blacklist = SoundPhysicsConfig.SOUND_TICK_BLACKLIST.get();
        if (blacklist == null || blacklist.isEmpty()) return false;
        String lower = soundName.toLowerCase();
        return blacklist.stream()
                .filter(e -> e != null && !e.trim().isEmpty())
                .anyMatch(e -> lower.contains(e.toLowerCase().trim()));
    }
}
