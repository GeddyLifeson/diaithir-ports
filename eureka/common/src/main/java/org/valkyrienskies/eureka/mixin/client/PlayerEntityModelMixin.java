package org.valkyrienskies.eureka.mixin.client;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.entity.ShipMountingEntity;

/**
 * Suppress the riding (bent-leg) pose while seated on a helm seat that is aligned to a block
 * center, so the player appears to stand at the wheel.
 *
 * <p>26.2: models no longer see the entity - {@code PlayerModel.setupAnim(LivingEntity, ...)}
 * became {@code setupAnim(AvatarRenderState)} and the riding flag moved from the model
 * ({@code HumanoidModel.riding}) into the render state ({@code HumanoidRenderState.isPassenger}),
 * which is extracted by the renderer. So this hook moved from the model's setupAnim to the tail of
 * {@code AvatarRenderer.extractRenderState}, where the live entity is still available.
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityModelMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("TAIL")
    )
    private void vs_eureka$noRidePoseOnAlignedHelmSeat(final Avatar player, final AvatarRenderState state,
        final float partialTick, final CallbackInfo ci) {
        final Entity vehicle = player.getVehicle();
        if (vehicle instanceof ShipMountingEntity) {
            final var pos = vehicle.position();
            if (Math.abs((pos.x + .5) % 1.0) < .05 && Math.abs((pos.z + .5) % 1.0) < .05) {
                state.isPassenger = false;
            }
        }
    }
}
