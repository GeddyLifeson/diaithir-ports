package immersive_melodies.client.sound;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public interface SoundManager {
    CancelableSoundInstance playSound(double x, double y, double z, SoundEvent event, SoundSource category, float volume, float pitch, long length, long sustain, long delay, Entity entity);

    boolean isFirstPerson(Entity entity);

    boolean audible(Entity entity);

    void suppressGameMusic();
}
