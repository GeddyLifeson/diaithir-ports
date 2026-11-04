package immersive_melodies.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class NoteSoundInstance extends AbstractTickableSoundInstance implements CancelableSoundInstance {
    private final Entity entity;
    private final boolean listenerRelative;
    long age;
    long length;
    long sustain;
    long fallOff;

    public NoteSoundInstance(SoundEvent sound, SoundSource category, float volume, float pitch, long length, long sustain, Entity entity, boolean listenerRelative) {
        super(sound, category, RandomSource.create(1));

        this.volume = volume;
        this.pitch = pitch;
        this.entity = entity;
        this.listenerRelative = listenerRelative;
        this.length = length + sustain;
        this.sustain = sustain;
        this.fallOff = Math.max(50, sustain);

        if (listenerRelative) {
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.relative = true;
        }

        updatePosition();
    }

    @Override
    public boolean canPlaySound() {
        return !entity.isSilent();
    }

    @Override
    public void tick() {
        if (entity.isRemoved()) {
            this.stop();
            return;
        }

        updatePosition();

        // Fade out
        age += 50;
        if (age >= length) {
            this.stop();
        }

        // todo Pitch bend
    }

    @Override
    public float getVolume() {
        float f = Math.min(1.0f, ((float) (length - age)) / fallOff);
        return volume * f;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public void cancel() {
        this.age = length - fallOff;
    }

    private void updatePosition() {
        if (listenerRelative) {
            this.x = 0.0;
            this.y = 0.0;
            this.z = 0.0;
        } else {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }
    }
}
