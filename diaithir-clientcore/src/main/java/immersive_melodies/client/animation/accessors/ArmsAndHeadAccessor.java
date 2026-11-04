package immersive_melodies.client.animation.accessors;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public class ArmsAndHeadAccessor<T extends Entity> implements ModelAccessor<T> {
    private final T entity;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public ArmsAndHeadAccessor(T entity, ModelPart head, ModelPart hat, ModelPart leftArm, ModelPart rightArm) {
        this.entity = entity;
        this.head = head;
        this.hat = hat;
        this.leftArm = leftArm;
        this.rightArm = rightArm;
    }

    @Override
    public T getEntity() {
        return entity;
    }

    @Override
    public Optional<ModelPart> getHead() {
        return Optional.ofNullable(head);
    }

    @Override
    public Optional<ModelPart> getHat() {
        return Optional.ofNullable(hat);
    }

    @Override
    public Optional<ModelPart> getLeftArm() {
        return Optional.ofNullable(leftArm);
    }

    @Override
    public Optional<ModelPart> getRightArm() {
        return Optional.ofNullable(rightArm);
    }
}
