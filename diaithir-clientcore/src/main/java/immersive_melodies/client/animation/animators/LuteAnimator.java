package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import net.minecraft.world.entity.Entity;

public class LuteAnimator implements Animator {
    @Override
    public <T extends Entity> void setAngles(ModelAccessor<T> accessor, MelodyProgress progress, float time) {
        accessor.leftArmPitch(-0.5f);
        accessor.leftArmRoll(-0.2f);
        accessor.leftArmYaw((progress.getCurrentPitch() - 0.5f) - 0.4f);

        accessor.rightArmPitch(-0.75f);
        accessor.rightArmYaw(0.0f);
        accessor.rightArmRoll(accessor.rightArmRoll() * 0.25f - 0.2f);
    }
}
