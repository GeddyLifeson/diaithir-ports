package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import net.minecraft.world.entity.Entity;

public class EnderBassAnimator implements Animator {
    @Override
    public <T extends Entity> void setAngles(ModelAccessor<T> accessor, MelodyProgress progress, float time) {
        accessor.leftArmPitch(-0.3f);
        accessor.leftArmRoll(0.0f);
        accessor.leftArmYaw(0.0f);

        accessor.rightArmPitch(-0.3f);
        accessor.rightArmRoll(0.0f);
        accessor.rightArmYaw(0.0f);
    }
}
