package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import net.minecraft.world.entity.Entity;

public interface Animator {
    default <T extends Entity> void setAngles(ModelAccessor<T> accessor, MelodyProgress progress, float time) {
        // no-op
    }
}
