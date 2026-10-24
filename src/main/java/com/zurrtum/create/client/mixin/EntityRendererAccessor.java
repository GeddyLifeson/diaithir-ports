package com.zurrtum.create.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor<T extends Entity> {
    @Invoker("affectedByCulling")
    boolean create$affectedByCulling(T entity);

    @Invoker("getBoundingBoxForCulling")
    AABB create$getBoundingBoxForCulling(T entity);
}
