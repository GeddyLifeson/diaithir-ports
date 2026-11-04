package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import net.minecraft.world.entity.Entity;

public class PianoAnimator implements Animator {
    @Override
    public <T extends Entity> void setAngles(ModelAccessor<T> accessor, MelodyProgress progress, float time) {
        accessor.leftArmPitch(-1.4f - progress.getCurrent() * progress.getCurrentVolume() * 0.5f);
        accessor.leftArmYaw((progress.getCurrentPitch() - 0.5f) + 0.2f);

        accessor.rightArmPitch(-0.9f);
        accessor.rightArmYaw(-0.15f);
        accessor.rightArmRoll(0.0f);
    }
}
