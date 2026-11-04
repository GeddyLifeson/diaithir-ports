package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import net.minecraft.world.entity.Entity;

public class TinyDrumAnimator implements Animator {
    @Override
    public <T extends Entity> void setAngles(ModelAccessor<T> accessor, MelodyProgress progress, float time) {
        float hit = Math.min(progress.delta(), 300) / 300.0f * progress.getCurrentVolume() * 0.4f;
        accessor.leftArmPitch(-1.2f - hit);
        accessor.leftArmYaw(0.2f - hit);
        accessor.leftArmRoll((progress.getCurrentPitch() - 0.5f) * 0.5f);

        accessor.rightArmPitch(-0.6f);
        accessor.rightArmYaw(-0.8f + progress.getCurrentPitch() * 0.15f);
        accessor.rightArmRoll(0.2f + progress.getCurrentPitch() * 0.15f);
    }
}
