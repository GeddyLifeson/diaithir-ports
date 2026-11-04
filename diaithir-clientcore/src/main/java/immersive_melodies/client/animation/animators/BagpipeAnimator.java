package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import net.minecraft.world.entity.Entity;

public class BagpipeAnimator implements Animator {
    @Override
    public <T extends Entity> void setAngles(ModelAccessor<T> accessor, MelodyProgress progress, float time) {
        accessor.headYaw(0.0f);
        accessor.headPitch(0.25f);

        accessor.leftArmPitch(-0.4f);
        accessor.leftArmYaw(-0.5f);

        accessor.rightArmPitch(-0.75f);
        accessor.rightArmYaw(-0.15f);
        accessor.rightArmRoll(0.0f);
    }
}
