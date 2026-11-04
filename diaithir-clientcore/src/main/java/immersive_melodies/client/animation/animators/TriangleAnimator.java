package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import net.minecraft.world.entity.Entity;

public class TriangleAnimator implements Animator {
    @Override
    public <T extends Entity> void setAngles(ModelAccessor<T> accessor, MelodyProgress progress, float time) {
        float delta = (float) Math.sin(progress.getCurrent() * Math.PI * 0.5);

        accessor.leftArmPitch(-1.1f + progress.getCurrentPitch() * 0.25f);
        accessor.leftArmYaw(delta * 0.6f);

        accessor.rightArmPitch(-1.6f);
        accessor.rightArmRoll((float) Math.cos(time * 0.25f) * 0.05f * progress.getCurrentVolume());
        accessor.rightArmYaw(-0.5f);
    }
}
