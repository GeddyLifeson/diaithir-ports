package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import net.minecraft.world.entity.Entity;

public class VielleAnimator implements Animator {
    @Override
    public <T extends Entity> void setAngles(ModelAccessor<T> accessor, MelodyProgress progress, float time) {
        accessor.leftArmPitch(-1.25f);
        accessor.leftArmYaw((float) (Math.cos(time * 0.2f) * (0.25 + progress.getCurrentVolume() * 0.1f) + 0.2f));
        accessor.leftArmRoll(-0.05f);

        accessor.rightArmPitch(-0.4f + (float) (-0.5f + Math.cos(time * 0.15f) * 0.05f));
        accessor.rightArmYaw((progress.getCurrentPitch() - 1.0f) * 0.5f);
        accessor.rightArmRoll(0.2f);
    }
}
