package org.valkyrienskies.mod.forge.mixin.feature.water_in_ships_entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3d;
import org.joml.primitives.AABBd;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

/**
 * Port of the pre-26.1 "water in ships" entity mixin. 26.1 tracks entity fluid interaction (heights, eyes in fluid,
 * currents) in {@link EntityFluidInteraction#update}, so:
 * <ul>
 *   <li>after the vanilla (world space) pass we re-run the pass once per intersecting ship, with the entity's
 *   box / eye position transformed into ship space, accumulating into the same trackers;</li>
 *   <li>entities in a sealed area of a ship (see mixin.entity.MixinEntity) do not interact with fluids at all.</li>
 * </ul>
 */
@Mixin(EntityFluidInteraction.class)
public abstract class MixinEntityFluidInteraction {

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private Map<?, ?> trackerByFluid;

    @Shadow
    public abstract void update(Entity entity, Predicate<FluidType> typePushPredicate);

    @Unique
    private Ship vs$ship = null;

    @Unique
    private Vector3d vs$eyeInShip = null;

    @Unique
    private final AABBd vs$tmpAABB = new AABBd();

    @Inject(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void vs$sealedAreaNoFluid(final Entity entity, final Predicate<FluidType> typePushPredicate,
        final CallbackInfo ci) {
        if (entity instanceof final IEntityDraggingInformationProvider provider && provider.vs$isInSealedArea()
            && ValkyrienSkies.isConnectivityEnabled(entity.level().isClientSide())) {
            this.trackerByFluid.clear();
            ci.cancel();
        }
    }

    @Inject(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        at = @At("TAIL")
    )
    private void vs$updateWithShips(final Entity entity, final Predicate<FluidType> typePushPredicate,
        final CallbackInfo ci) {
        if (this.vs$ship != null) {
            return; // already inside a ship pass
        }
        final AABB box = entity.getBoundingBox();
        for (final Ship ship : VSGameUtilsKt.getShipsIntersecting(entity.level(), box)) {
            this.vs$ship = ship;
            this.vs$eyeInShip = ship.getWorldToShip()
                .transformPosition(VectorConversionsMCKt.toJOML(entity.getEyePosition()), new Vector3d());
            try {
                this.update(entity, typePushPredicate);
            } finally {
                this.vs$ship = null;
                this.vs$eyeInShip = null;
            }
        }
    }

    // Do not wipe the trackers filled by the previous passes
    @WrapOperation(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Collection;removeIf(Ljava/util/function/Predicate;)Z")
    )
    private boolean vs$keepTrackersDuringShipPass(final java.util.Collection<?> instance,
        final Predicate<?> predicate, final Operation<Boolean> original) {
        if (this.vs$ship != null) {
            return false;
        }
        return original.call(instance, predicate);
    }

    @WrapOperation(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getFluidInteractionBox()Lnet/minecraft/world/phys/AABB;")
    )
    private AABB vs$shipSpaceInteractionBox(final Entity instance, final Operation<AABB> original) {
        final AABB box = original.call(instance);
        if (this.vs$ship == null || box == null) {
            return box;
        }
        return VectorConversionsMCKt.toMinecraft(
            VectorConversionsMCKt.set(this.vs$tmpAABB, box).transform(this.vs$ship.getWorldToShip()));
    }

    @WrapOperation(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;")
    )
    private AABB vs$shipSpaceBoundingBox(final Entity instance, final Operation<AABB> original) {
        final AABB box = original.call(instance);
        if (this.vs$ship == null) {
            return box;
        }
        return VectorConversionsMCKt.toMinecraft(
            VectorConversionsMCKt.set(this.vs$tmpAABB, box).transform(this.vs$ship.getWorldToShip()));
    }

    @WrapOperation(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBlockX()I")
    )
    private int vs$shipSpaceBlockX(final Entity instance, final Operation<Integer> original) {
        return this.vs$eyeInShip == null ? original.call(instance) : Mth.floor(this.vs$eyeInShip.x);
    }

    @WrapOperation(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBlockZ()I")
    )
    private int vs$shipSpaceBlockZ(final Entity instance, final Operation<Integer> original) {
        return this.vs$eyeInShip == null ? original.call(instance) : Mth.floor(this.vs$eyeInShip.z);
    }

    @WrapOperation(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeY()D")
    )
    private double vs$shipSpaceEyeY(final Entity instance, final Operation<Double> original) {
        return this.vs$eyeInShip == null ? original.call(instance) : this.vs$eyeInShip.y;
    }
}
