package net.diaithir.carts.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.diaithir.carts.RailCargo;
import net.diaithir.carts.client.CargoDraw;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Draws a wagon's cargo with the cart (client only): the loads are worked out when the render state is filled and drawn in the cart's own frame. */
@Mixin(AbstractMinecartRenderer.class)
abstract class MinecartCargoRenderMixin {

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private BlockModelResolver blockModelResolver;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;F)V", at = @At("TAIL"), require = 0)
    private void diaithircarts$extractCargo(AbstractMinecart cart, MinecartRenderState state, float partial, CallbackInfo ci) {
        String view = cart.getData(RailCargo.VIEW);
        if (view.isEmpty()) {
            CargoDraw.PIECES.remove(state);
            return;
        }
        List<CargoDraw.Piece> pcs = CargoDraw.pieces(view, !state.isNewRender, this.blockModelResolver);
        CargoDraw.PIECES.put(state, pcs);
    }

    // just before the cart model is flipped upright: the pose is in the frame the display block is drawn in
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", ordinal = 1), require = 0)
    private void diaithircarts$submitCargo(MinecartRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        List<CargoDraw.Piece> pieces = CargoDraw.PIECES.get(state);
        if (pieces != null && !pieces.isEmpty()) CargoDraw.submit(pieces, pose, collector, state.lightCoords, state.outlineColor);
    }
}
