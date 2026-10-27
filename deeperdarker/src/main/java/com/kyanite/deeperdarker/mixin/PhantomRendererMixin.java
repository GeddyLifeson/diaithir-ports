package com.kyanite.deeperdarker.mixin;

import com.kyanite.deeperdarker.DeeperDarker;
import com.kyanite.deeperdarker.world.otherside.OthersideDimension;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(PhantomRenderer.class)
public class PhantomRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/Phantom;Lnet/minecraft/client/renderer/entity/state/PhantomRenderState;F)V", at = @At("TAIL"))
    private void deeperdarker$extract(Phantom entity, PhantomRenderState state, float partialTicks, CallbackInfo ci) {
        ((com.kyanite.deeperdarker.util.SculkPhantomFlag) state).deeperdarker$setSculk(entity.level().dimension() == OthersideDimension.OTHERSIDE_LEVEL);
    }

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/PhantomRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void deeperdarker$texture(PhantomRenderState state, CallbackInfoReturnable<Identifier> cir) {
        if(((com.kyanite.deeperdarker.util.SculkPhantomFlag) state).deeperdarker$isSculk()) {
            cir.setReturnValue(DeeperDarker.rl("textures/entity/sculk_phantom.png"));
        }
    }
}
