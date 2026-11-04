package immersive_melodies.client.animation.accessors;

import immersive_melodies.item.InstrumentItem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

@SuppressWarnings("unused")
public interface ModelAccessor<T extends Entity> {
    T getEntity();

    private boolean isInMainHand() {
        if (getEntity() instanceof LivingEntity livingEntity) {
            return livingEntity.getMainHandItem().getItem() instanceof InstrumentItem;
        } else {
            return true;
        }
    }

    default boolean flipHands() {
        return isInMainHand() == isLeftHanded();
    }

    default boolean isLeftHanded() {
        return getEntity() instanceof LivingEntity livingEntity && livingEntity.getMainArm() == HumanoidArm.LEFT;
    }

    default Optional<ModelPart> getHead() {
        return Optional.empty();
    }

    default Optional<ModelPart> getHat() {
        return Optional.empty();
    }

    default Optional<ModelPart> getFlippedLeftArm() {
        return flipHands() ? getRightArm() : getLeftArm();
    }

    default Optional<ModelPart> getFlippedRightArm() {
        return flipHands() ? getLeftArm() : getRightArm();
    }

    default Optional<ModelPart> getLeftArm() {
        return Optional.empty();
    }

    default Optional<ModelPart> getRightArm() {
        return Optional.empty();
    }

    default float headYaw() {
        return getHead().map(h -> h.yRot).orElse(0.0f);
    }

    default void headYaw(float yaw) {
        getHead().ifPresent(h -> h.yRot = (flipHands() ? -yaw : yaw));
        getHat().ifPresent(h -> h.yRot = (flipHands() ? -yaw : yaw));
    }

    default float headPitch() {
        return getHead().map(h -> h.xRot).orElse(0.0f);
    }

    default void headPitch(float pitch) {
        getHead().ifPresent(h -> h.xRot = pitch);
        getHat().ifPresent(h -> h.xRot = pitch);
    }

    default float leftArmYaw() {
        return getFlippedLeftArm().map(l -> l.yRot).orElse(0.0f);
    }

    default void leftArmYaw(float yaw) {
        getFlippedLeftArm().ifPresent(l -> l.yRot = (flipHands() ? -yaw : yaw));
    }

    default float leftArmPitch() {
        return getFlippedLeftArm().map(l -> l.xRot).orElse(0.0f);
    }

    default void leftArmPitch(float pitch) {
        getFlippedLeftArm().ifPresent(l -> l.xRot = pitch);
    }

    default float leftArmRoll() {
        return getFlippedLeftArm().map(l -> l.zRot).orElse(0.0f);
    }

    default void leftArmRoll(float roll) {
        getFlippedLeftArm().ifPresent(l -> l.zRot = (flipHands() ? -roll : roll));
    }

    default float rightArmYaw() {
        return getFlippedRightArm().map(r -> r.yRot).orElse(0.0f);
    }

    default void rightArmYaw(float yaw) {
        getFlippedRightArm().ifPresent(r -> r.yRot = (flipHands() ? -yaw : yaw));
    }

    default float rightArmPitch() {
        return getFlippedRightArm().map(r -> r.xRot).orElse(0.0f);
    }

    default void rightArmPitch(float pitch) {
        getFlippedRightArm().ifPresent(r -> r.xRot = pitch);
    }

    default float rightArmRoll() {
        return getFlippedRightArm().map(r -> r.zRot).orElse(0.0f);
    }

    default void rightArmRoll(float roll) {
        getFlippedRightArm().ifPresent(r -> r.zRot = (flipHands() ? -roll : roll));
    }
}
